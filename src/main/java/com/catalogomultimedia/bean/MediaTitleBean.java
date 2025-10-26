package com.catalogomultimedia.bean;

import com.catalogomultimedia.entity.MediaFile;
import com.catalogomultimedia.entity.MediaTitle;
import com.catalogomultimedia.entity.MovieGenre;
import com.catalogomultimedia.service.AzureBlobStorageService;
import com.catalogomultimedia.service.MediaFileService;
import com.catalogomultimedia.service.MediaTitleService;
import com.catalogomultimedia.service.MovieGenreService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Named("mediaTitleBean")
@ViewScoped
public class MediaTitleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MediaTitleService mediaTitleService;

    @EJB
    private MovieGenreService movieGenreService;

    @EJB
    private MediaFileService mediaFileService;

    @EJB
    private AzureBlobStorageService azureBlobStorageService;

    private List<MediaTitle> mediaTitles;
    private List<MediaTitle> filteredTitles;
    private MediaTitle selectedTitle;
    private MediaTitle newTitle;

    private List<MovieGenre> availableGenres;
    private List<Long> selectedGenreIds;

    private String searchTitleName;
    private MediaTitle.TitleType searchType;
    private Integer searchYear;
    private Long searchGenreId;

    @PostConstruct
    public void init() {
        loadMediaTitles();
        loadGenres();
        newTitle = new MediaTitle();
        selectedGenreIds = new ArrayList<>();
    }

    public void loadMediaTitles() {
        mediaTitles = mediaTitleService.findAll();
    }

    public void loadGenres() {
        availableGenres = movieGenreService.findAll();
    }

    public void prepareNewTitle() {
        newTitle = new MediaTitle();
        selectedGenreIds = new ArrayList<>();
    }

    public void saveTitle() {
        try {
            Set<MovieGenre> genres = new HashSet<>();
            for (Long genreId : selectedGenreIds) {
                MovieGenre genre = movieGenreService.findById(genreId);
                genres.add(genre);
            }
            newTitle.setGenres(genres);

            mediaTitleService.save(newTitle);
            loadMediaTitles();

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Título guardado correctamente");
            prepareNewTitle();

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void prepareEdit(MediaTitle title) {
        selectedTitle = title;
        selectedGenreIds = title.getGenres().stream()
                .map(MovieGenre::getMovieGenreId)
                .collect(Collectors.toList());
    }

    public void updateTitle() {
        try {
            Set<MovieGenre> genres = new HashSet<>();
            for (Long genreId : selectedGenreIds) {
                MovieGenre genre = movieGenreService.findById(genreId);
                genres.add(genre);
            }
            selectedTitle.setGenres(genres);

            mediaTitleService.save(selectedTitle);
            loadMediaTitles();

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Título actualizado correctamente");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void deleteTitle(Long id) {
        try {
            mediaTitleService.delete(id);
            loadMediaTitles();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Título eliminado correctamente");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void handlePosterUpload(FileUploadEvent event) {
        try {
            if (selectedTitle == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                        "Debe seleccionar un título primero");
                return;
            }

            UploadedFile uploadedFile = event.getFile();

            // Desactivar póster anterior
            MediaFile oldPoster = mediaFileService.findActivePosterByTitleId(
                    selectedTitle.getMediaTitleId());
            if (oldPoster != null) {
                oldPoster.setIsActive(false);
                mediaFileService.save(oldPoster);
            }

            // Subir nuevo póster a Azure
            AzureBlobStorageService.BlobUploadResult result =
                    azureBlobStorageService.uploadFile(
                            uploadedFile.getInputStream(),
                            uploadedFile.getSize(),
                            uploadedFile.getContentType(),
                            selectedTitle.getTitleName(),
                            MediaFile.FileType.POSTER,
                            uploadedFile.getFileName()
                    );

            // Crear registro del archivo
            MediaFile mediaFile = new MediaFile();
            mediaFile.setMediaTitle(selectedTitle);
            mediaFile.setFileType(MediaFile.FileType.POSTER);
            mediaFile.setBlobUrl(result.getBlobUrl());
            mediaFile.setEtag(result.getEtag());
            mediaFile.setContentType(result.getContentType());
            mediaFile.setSizeBytes(result.getSizeBytes());
            mediaFile.setUploadedBy("admin");

            mediaFileService.save(mediaFile);

            loadMediaTitles();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Póster subido correctamente");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void handleTechnicalSheetUpload(FileUploadEvent event) {
        try {
            if (selectedTitle == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                        "Debe seleccionar un título primero");
                return;
            }

            UploadedFile uploadedFile = event.getFile();

            AzureBlobStorageService.BlobUploadResult result =
                    azureBlobStorageService.uploadFile(
                            uploadedFile.getInputStream(),
                            uploadedFile.getSize(),
                            uploadedFile.getContentType(),
                            selectedTitle.getTitleName(),
                            MediaFile.FileType.TECHNICAL_SHEET,
                            uploadedFile.getFileName()
                    );

            MediaFile mediaFile = new MediaFile();
            mediaFile.setMediaTitle(selectedTitle);
            mediaFile.setFileType(MediaFile.FileType.TECHNICAL_SHEET);
            mediaFile.setBlobUrl(result.getBlobUrl());
            mediaFile.setEtag(result.getEtag());
            mediaFile.setContentType(result.getContentType());
            mediaFile.setSizeBytes(result.getSizeBytes());
            mediaFile.setUploadedBy("admin");

            mediaFileService.save(mediaFile);

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Ficha técnica subida correctamente");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void searchTitles() {
        mediaTitles = mediaTitleService.search(searchTitleName, searchType,
                searchYear, searchGenreId);
    }

    public void clearSearch() {
        searchTitleName = null;
        searchType = null;
        searchYear = null;
        searchGenreId = null;
        loadMediaTitles();
    }

    public MediaTitle.TitleType[] getTitleTypes() {
        return MediaTitle.TitleType.values();
    }

    public boolean hasPoster(MediaTitle title) {
        return title.hasPoster();
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    public List<MediaTitle> getMediaTitles() { return mediaTitles; }
    public void setMediaTitles(List<MediaTitle> mediaTitles) { this.mediaTitles = mediaTitles; }

    public List<MediaTitle> getFilteredTitles() { return filteredTitles; }
    public void setFilteredTitles(List<MediaTitle> filteredTitles) { this.filteredTitles = filteredTitles; }

    public MediaTitle getSelectedTitle() { return selectedTitle; }
    public void setSelectedTitle(MediaTitle selectedTitle) { this.selectedTitle = selectedTitle; }

    public MediaTitle getNewTitle() { return newTitle; }
    public void setNewTitle(MediaTitle newTitle) { this.newTitle = newTitle; }

    public List<MovieGenre> getAvailableGenres() { return availableGenres; }
    public void setAvailableGenres(List<MovieGenre> availableGenres) { this.availableGenres = availableGenres; }

    public List<Long> getSelectedGenreIds() { return selectedGenreIds; }
    public void setSelectedGenreIds(List<Long> selectedGenreIds) { this.selectedGenreIds = selectedGenreIds; }

    public String getSearchTitleName() { return searchTitleName; }
    public void setSearchTitleName(String searchTitleName) { this.searchTitleName = searchTitleName; }

    public MediaTitle.TitleType getSearchType() { return searchType; }
    public void setSearchType(MediaTitle.TitleType searchType) { this.searchType = searchType; }

    public Integer getSearchYear() { return searchYear; }
    public void setSearchYear(Integer searchYear) { this.searchYear = searchYear; }

    public Long getSearchGenreId() { return searchGenreId; }
    public void setSearchGenreId(Long searchGenreId) { this.searchGenreId = searchGenreId; }
}