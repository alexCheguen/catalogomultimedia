package com.catalogomultimedia.bean;

import com.catalogomultimedia.entity.MediaTitle;
import com.catalogomultimedia.entity.MovieGenre;
import com.catalogomultimedia.service.MediaTitleService;
import com.catalogomultimedia.repository.MovieGenreRepository;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import lombok.Data;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("mediaTitleBean")
@ViewScoped
@Data
public class MediaTitleBean implements Serializable {

    @Autowired
    private MediaTitleService mediaTitleService;

    @Autowired
    private MovieGenreRepository movieGenreRepository;

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

    private UploadedFile posterFile;
    private UploadedFile technicalSheetFile;

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
        availableGenres = movieGenreRepository.findAll();
    }

    /**
     * Preparar nuevo título
     */
    public void prepareNewTitle() {
        newTitle = new MediaTitle();
        selectedGenreIds = new ArrayList<>();
    }

    /**
     * Guardar título
     */
    public void saveTitle() {
        try {
            // Asignar géneros seleccionados
            Set<MovieGenre> genres = new HashSet<>();
            for (Long genreId : selectedGenreIds) {
                MovieGenre genre = movieGenreRepository.findById(genreId).orElse(null);
                if (genre != null) {
                    genres.add(genre);
                }
            }
            newTitle.setGenres(genres);

            // Guardar título
            mediaTitleService.save(newTitle);

            // Recargar lista
            loadMediaTitles();

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Título guardado correctamente");

            // Limpiar formulario
            prepareNewTitle();

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    /**
     * Preparar edición
     */
    public void prepareEdit(MediaTitle title) {
        selectedTitle = title;
        selectedGenreIds = title.getGenres().stream()
                .map(MovieGenre::getMovieGenreId)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar título
     */
    public void updateTitle() {
        try {
            // Asignar géneros
            mediaTitleService.assignGenres(selectedTitle.getMediaTitleId(),
                    new HashSet<>(selectedGenreIds));

            // Actualizar título
            mediaTitleService.save(selectedTitle);

            loadMediaTitles();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Título actualizado correctamente");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    /**
     * Eliminar título
     */
    public void deleteTitle(Long id) {
        try {
            mediaTitleService.delete(id);
            loadMediaTitles();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Título eliminado correctamente");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    /**
     * Subir póster
     */
    public void handlePosterUpload(FileUploadEvent event) {
        try {
            if (selectedTitle == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                        "Debe seleccionar un título primero");
                return;
            }

            UploadedFile file = event.getFile();

            // Convertir UploadedFile a MultipartFile
            org.springframework.web.multipart.MultipartFile multipartFile =
                    new PrimeFacesMultipartFile(file);

            mediaTitleService.uploadPoster(selectedTitle.getMediaTitleId(),
                    multipartFile, getCurrentUser());

            loadMediaTitles();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Póster subido correctamente");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    /**
     * Subir ficha técnica
     */
    public void handleTechnicalSheetUpload(FileUploadEvent event) {
        try {
            if (selectedTitle == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                        "Debe seleccionar un título primero");
                return;
            }

            UploadedFile file = event.getFile();

            org.springframework.web.multipart.MultipartFile multipartFile =
                    new PrimeFacesMultipartFile(file);

            mediaTitleService.uploadTechnicalSheet(selectedTitle.getMediaTitleId(),
                    multipartFile, getCurrentUser());

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Ficha técnica subida correctamente");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    /**
     * Buscar títulos
     */
    public void searchTitles() {
        mediaTitles = mediaTitleService.search(searchTitleName, searchType,
                searchYear, searchGenreId);
    }

    /**
     * Limpiar búsqueda
     */
    public void clearSearch() {
        searchTitleName = null;
        searchType = null;
        searchYear = null;
        searchGenreId = null;
        loadMediaTitles();
    }

    /**
     * Obtener tipos de título para dropdown
     */
    public MediaTitle.TitleType[] getTitleTypes() {
        return MediaTitle.TitleType.values();
    }

    /**
     * Verificar si un título tiene póster
     */
    public boolean hasPoster(MediaTitle title) {
        return title.hasPoster();
    }

    /**
     * Obtener usuario actual (simplificado)
     */
    private String getCurrentUser() {
        // En producción, obtener del contexto de seguridad
        return "admin";
    }

    /**
     * Agregar mensaje JSF
     */
    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, detail));
    }

    /**
     * Clase auxiliar para convertir UploadedFile a MultipartFile
     */
    private static class PrimeFacesMultipartFile implements org.springframework.web.multipart.MultipartFile {
        private final UploadedFile uploadedFile;

        public PrimeFacesMultipartFile(UploadedFile uploadedFile) {
            this.uploadedFile = uploadedFile;
        }

        @Override
        public String getName() {
            return uploadedFile.getFileName();
        }

        @Override
        public String getOriginalFilename() {
            return uploadedFile.getFileName();
        }

        @Override
        public String getContentType() {
            return uploadedFile.getContentType();
        }

        @Override
        public boolean isEmpty() {
            return uploadedFile.getSize() == 0;
        }

        @Override
        public long getSize() {
            return uploadedFile.getSize();
        }

        @Override
        public byte[] getBytes() throws java.io.IOException {
            return uploadedFile.getContent();
        }

        @Override
        public java.io.InputStream getInputStream() throws java.io.IOException {
            return uploadedFile.getInputStream();
        }

        @Override
        public void transferTo(java.io.File dest) throws java.io.IOException {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}