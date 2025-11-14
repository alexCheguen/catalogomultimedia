package com.catalogomultimedia.bean;

import com.catalogomultimedia.dtos.MediaFileDTO;
import com.catalogomultimedia.enums.FileType;
import com.catalogomultimedia.enums.TitleType;
import com.catalogomultimedia.entity.MovieGenre;
import com.catalogomultimedia.service.AzureBlobStorageService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.InputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



import com.catalogomultimedia.entity.MediaFile;
import com.catalogomultimedia.entity.MediaTitle;
import com.catalogomultimedia.service.MediaFileService;
import com.catalogomultimedia.service.MediaTitleService;
import com.catalogomultimedia.service.MovieGenreService;






@Named("mediaTitleBean")
@ViewScoped
public class MediaTitleBean implements Serializable {

    @Inject
    private MediaTitleService mediaTitleService;
    @Inject
    private MovieGenreService movieGenresService;
    @Inject
    private MediaFileService mediaFileService;
    @Inject
    private AzureBlobStorageService azureBlobStorageService;

    private MediaTitle selectedMediaTitle;
    private MediaFile selectedMediaFile;

    private FileType uploadingFileType = FileType.POSTER;





    public List<MediaTitle> getMediaTitles() {return mediaTitleService.findAll();}

    public List<MovieGenre> getAllGenres() {return movieGenresService.findAll();}

    public TitleType[] getTitleTypes(){ return TitleType.values();}

    public FileType[] getFileTypes(){ return FileType.values();}

    public List<MediaFile> getMediaFilesOfSelected(){
        if (selectedMediaTitle == null   || selectedMediaTitle.getMediaFiles() == null) return List.of();
        return selectedMediaTitle.getMediaFiles().stream()
                .sorted(Comparator.comparing(MediaFile::getUploadedAt).reversed())
                .collect(Collectors.toList());
    }


    /*--Actions--*/
    public void openNew(){selectedMediaTitle = new MediaTitle();}

    public void save(){
        boolean isNew = selectedMediaTitle.getMediaTitleId()==null;
        mediaTitleService.save(selectedMediaTitle);

        FacesMessage msg = new FacesMessage(isNew ?
                "Registro agregado con éxito!" : "Registro actualizado con éxito!");
        FacesContext.getCurrentInstance().addMessage(null, msg);

        PrimeFaces.current().executeScript("PD('manageMediaTitleDialog').hide()");
        PrimeFaces.current().ajax().update("form-media-title-dialog:message-media-titles",
                "form-media-title-dialogs:dt-media-titles");
    }

    public void delete() {
        mediaTitleService.delete(selectedMediaTitle);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Registro eliminado con éxito!"));
        PrimeFaces.current().ajax().update("form-media-title-dialogs:messages-media-titles",
                "form-media-title-dialogs:dt-media-titles");
    }

    /*-- FileUpload / delete --*/
    public void handleFileUpload(FileUploadEvent event){

        UploadedFile uf = event.getFile();
        if (uf == null || uf.getSize() == 0 ) return;

        String ct = uf.getContentType();
        long size = uf.getSize();
        boolean isImage = "image/jpeg".equals(ct) || "image/png".equals(ct);
        boolean isPdf = "application/pdf".equals(ct);

        if (!(isImage || isPdf)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Tipo no permitido", ct));
            return;
        }
        if (isImage && size > 2L * 1024 * 1024){
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "La imagen supera 2MB", uf.getFileName()));
            return;
        }
        if (isPdf && size > 5L * 1024 * 1024){
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "El PDF supera 5MB", uf.getFileName()));
            return;
        }


        try (InputStream in = uf.getInputStream()) {
            MediaFileDTO dto = azureBlobStorageService.uploadCatalogFile(
                    uploadingFileType,
                    selectedMediaTitle.getTitleName(),
                    uf.getFileName(),
                    uf.getContentType(),
                    uf.getInputStream(),
                    uf.getSize(),
                    "ui",
                    Duration.ofMinutes(30),
                    true
            );

            MediaFile mf = new MediaFile();
            mf.setMediaTitle(selectedMediaTitle);
            mf.setFileType(
                    com.catalogomultimedia.entity.MediaFile.FileType.valueOf(dto.getFileType().name())
            );
            mf.setBlobUrl(dto.getSignedUrl());
            mf.setEtag(dto.getEtag());
            mf.setContentType(dto.getContentType());
            mf.setSizeBytes(dto.getSizeBytes());
            mf.setUploadedBy(dto.getUploadedBy());
            selectedMediaTitle.getMediaFiles().add(mf);
            if (dto.getUploadedAt() != null) {
                mf.setUploadedAt(dto.getUploadedAt().toLocalDateTime());
            } else {
                mf.setUploadedAt(java.time.LocalDateTime.now());
            }

            selectedMediaTitle.getMediaFiles().add(mf);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("Archivo cargado: " + uf.getFileName()));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al subir archivo", ex.getMessage()));
        }

        PrimeFaces.current().ajax().update("form-media-title-dialogs:media-files-table",
                "form-media-title-dialogs:msgsMediaTitles");
    }

    public void deleteMediaFile() {
        if (selectedMediaFile == null) return;

        if (selectedMediaFile.getMediaFileId() != null) {
            mediaFileService.delete(selectedMediaFile);
        }

        // Actualizar la lista en el título seleccionado
        List<MediaFile> files = mediaFileService.findAll(); // Si quieres refrescar desde BD
        selectedMediaTitle.setMediaFiles(files.stream()
                .filter(mf -> !mf.getBlobUrl().equals(selectedMediaFile.getBlobUrl()))
                .collect(Collectors.toList()));

        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Archivo eliminado"));
        PrimeFaces.current().ajax().update("form-media-title-dialogs:media-files-table",
                "form-media-title-dialogs:msgsMediaTitles");
    }

    /* --------- Helpers --------- */
    public String joinGenres(MediaTitle mt) {
        if (mt == null || mt.getGenres() == null || mt.getGenres().isEmpty()) return "";
        return mt.getGenres().stream()
                .map(MovieGenre::getGenreName)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    /* --------- Getters/Setters --------- */
    public MediaTitle getSelectedMediaTitle() {
        return selectedMediaTitle;
    }

    public void setSelectedMediaTitle(MediaTitle selectedMediaTitle) {
        this.selectedMediaTitle = selectedMediaTitle;
    }

    public MediaFile getSelectedMediaFile() {
        return selectedMediaFile;
    }

    public void setSelectedMediaFile(MediaFile selectedMediaFile) {
        this.selectedMediaFile = selectedMediaFile;
    }

    public FileType getUploadingFileType() {
        return uploadingFileType;
    }

    public void setUploadingFileType(FileType uploadingFileType) {
        this.uploadingFileType = uploadingFileType;
    }

    // Exponer listas/enums a la vista con nombres cómodos
    public List<MovieGenre> getAllGenresWrapped() {
        return getAllGenres();
    }

    public TitleType[] getTitleTypesWrapped() {
        return getTitleTypes();
    }

    public FileType[] getFileTypesWrapped() {
        return getFileTypes();
    }

    // ======= Propiedades de búsqueda =======
    private String searchTitleName;
    private TitleType searchType;
    private Integer searchYear;
    private Long searchGenreId;

    private List<MovieGenre> availableGenres;
    private List<MediaTitle> filteredTitles;

    // ======= Métodos de búsqueda =======
    @PostConstruct
    public void init() {
        selectedMediaTitle = new MediaTitle();
        availableGenres = movieGenresService.findAll();
    }

    // Acción del botón "Buscar"
    public void searchTitles() {
        List<MediaTitle> all = mediaTitleService.findAll();

        filteredTitles = all.stream()
                .filter(t -> (searchTitleName == null || t.getTitleName().toLowerCase().contains(searchTitleName.toLowerCase())))
                .filter(t -> (searchType == null || t.getTitleType() == searchType))
                .filter(t -> (searchYear == null || (t.getReleaseYear() != null && t.getReleaseYear().equals(searchYear))))
                .filter(t -> (searchGenreId == null ||
                        (t.getGenres() != null && t.getGenres().stream()
                                .anyMatch(g -> g.getMovieGenreId().equals(searchGenreId)))))
                .collect(Collectors.toList());
    }

    // Acción del botón "Limpiar"
    public void clearSearch() {
        searchTitleName = null;
        searchType = null;
        searchYear = null;
        searchGenreId = null;
        filteredTitles = null;
    }

    // ======= Getters/Setters =======
    public String getSearchTitleName() {
        return searchTitleName;
    }

    public void setSearchTitleName(String searchTitleName) {
        this.searchTitleName = searchTitleName;
    }

    public TitleType getSearchType() {
        return searchType;
    }

    public void setSearchType(TitleType searchType) {
        this.searchType = searchType;
    }

    public Integer getSearchYear() {
        return searchYear;
    }

    public void setSearchYear(Integer searchYear) {
        this.searchYear = searchYear;
    }

    public Long getSearchGenreId() {
        return searchGenreId;
    }

    public void setSearchGenreId(Long searchGenreId) {
        this.searchGenreId = searchGenreId;
    }

    public List<MovieGenre> getAvailableGenres() {
        return availableGenres;
    }

    public void setAvailableGenres(List<MovieGenre> availableGenres) {
        this.availableGenres = availableGenres;
    }

    public List<MediaTitle> getFilteredTitles() {
        return filteredTitles;
    }

    public void setFilteredTitles(List<MediaTitle> filteredTitles) {
        this.filteredTitles = filteredTitles;
    }

}
