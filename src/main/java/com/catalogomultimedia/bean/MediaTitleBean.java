package com.catalogomultimedia.bean;

import com.catalogomultimedia.entity.MediaFile;
import com.catalogomultimedia.entity.MediaTitle;
import com.catalogomultimedia.entity.MovieGenre;
import com.catalogomultimedia.service.AzureBlobStorageService;
import com.catalogomultimedia.service.MediaFileService;
import com.catalogomultimedia.service.MediaTitleService;
import com.catalogomultimedia.service.MovieGenreService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class MediaTitleBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MediaTitleService mediaTitleService;

    @Inject
    private MovieGenreService movieGenreService;

    @Inject
    private MediaFileService mediaFileService;

    @Inject
    private AzureBlobStorageService azureBlobStorageService;

    @Inject
    private Validator validator;

    private MediaTitle titulo;
    private List<MediaTitle> titulos;
    private List<MediaTitle> titulosFiltrados;
    private boolean dialogVisible;
    private boolean dialogPosterVisible;
    private boolean dialogFichaTecnicaVisible;

    private List<MovieGenre> generosDisponibles;
    private List<Long> generosSeleccionados;

    private String buscarNombre;
    private MediaTitle.TitleType buscarTipo;
    private Integer buscarAnio;
    private Long buscarGeneroId;

    @PostConstruct
    public void init() {
        titulo = new MediaTitle();
        dialogVisible = false;
        dialogPosterVisible = false;
        dialogFichaTecnicaVisible = false;
        generosSeleccionados = new ArrayList<>();
        cargarTitulos();
        cargarGeneros();
    }

    public void cargarTitulos() {
        titulos = mediaTitleService.findAll();
    }

    public void cargarGeneros() {
        generosDisponibles = movieGenreService.findAll();
    }

    public void nuevo() {
        clearFacesMessages();
        titulo = new MediaTitle();
        generosSeleccionados = new ArrayList<>();
        dialogVisible = true;
    }

    public void editar(MediaTitle t) {
        clearFacesMessages();
        this.titulo = t;
        generosSeleccionados = t.getGenres().stream()
                .map(MovieGenre::getMovieGenreId) // Asegúrate que tu entidad tenga este getter
                .collect(Collectors.toList());
        dialogVisible = true;
    }

    public void guardar() {
        clearFacesMessages();

        // Validar con Bean Validation
        Set<ConstraintViolation<MediaTitle>> violations = validator.validate(titulo);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<MediaTitle> violation : violations) {
                String field = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                String label = getFieldLabel(field);

                FacesContext.getCurrentInstance().addMessage("frmTitulo",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                label + ": " + message, null));
            }
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }

        if (generosSeleccionados == null || generosSeleccionados.isEmpty()) {
            addMessage("Debe seleccionar al menos un género", FacesMessage.SEVERITY_ERROR);
            return;
        }

        try {
            Set<MovieGenre> generos = new HashSet<>();
            for (Long genreId : generosSeleccionados) {
                MovieGenre genero = movieGenreService.findById(genreId);
                generos.add(genero);
            }
            titulo.setGenres(generos);

            mediaTitleService.guardar(titulo);

            addMessage(
                    titulo.getMediaTitleId() == null ? "Título creado exitosamente" : "Título actualizado exitosamente",
                    FacesMessage.SEVERITY_INFO
            );

            titulo = new MediaTitle();
            generosSeleccionados = new ArrayList<>();
            dialogVisible = false;
            cargarTitulos();

        } catch (Exception e) {
            addMessage("Error al guardar título: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void eliminar(MediaTitle t) {
        try {
            mediaTitleService.eliminar(t.getMediaTitleId());
            addMessage("Título eliminado exitosamente", FacesMessage.SEVERITY_INFO);
            cargarTitulos();
        } catch (Exception e) {
            addMessage("Error al eliminar título: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void prepararSubidaPoster(MediaTitle t) {
        clearFacesMessages();
        this.titulo = t;
        dialogPosterVisible = true;
    }

    public void prepararSubidaFichaTecnica(MediaTitle t) {
        clearFacesMessages();
        this.titulo = t;
        dialogFichaTecnicaVisible = true;
    }

    public void subirPoster(FileUploadEvent event) {
        try {
            if (titulo == null || titulo.getMediaTitleId() == null) {
                addMessage("Debe seleccionar un título primero", FacesMessage.SEVERITY_WARN);
                return;
            }

            UploadedFile archivo = event.getFile();

            MediaFile posterAntiguo = mediaFileService.findActivePosterByTitleId(
                    titulo.getMediaTitleId());
            if (posterAntiguo != null) {
                posterAntiguo.setIsActive(false);
                mediaFileService.guardar(posterAntiguo);
            }

            AzureBlobStorageService.BlobUploadResult resultado =
                    azureBlobStorageService.uploadFile(
                            archivo.getInputStream(),
                            archivo.getSize(),
                            archivo.getContentType(),
                            titulo.getTitleName(),
                            MediaFile.FileType.POSTER,
                            archivo.getFileName()
                    );

            MediaFile archivoMedia = new MediaFile();
            archivoMedia.setMediaTitle(titulo);
            archivoMedia.setFileType(MediaFile.FileType.POSTER);
            archivoMedia.setBlobUrl(resultado.getBlobUrl());
            archivoMedia.setEtag(resultado.getEtag());
            archivoMedia.setContentType(resultado.getContentType());
            archivoMedia.setSizeBytes(resultado.getSizeBytes());
            archivoMedia.setUploadedBy(obtenerUsuarioActual());

            mediaFileService.guardar(archivoMedia);

            cargarTitulos();
            addMessage("Póster subido exitosamente", FacesMessage.SEVERITY_INFO);
            dialogPosterVisible = false;

        } catch (Exception e) {
            addMessage("Error al subir póster: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void subirFichaTecnica(FileUploadEvent event) {
        try {
            if (titulo == null || titulo.getMediaTitleId() == null) {
                addMessage("Debe seleccionar un título primero", FacesMessage.SEVERITY_WARN);
                return;
            }

            UploadedFile archivo = event.getFile();

            AzureBlobStorageService.BlobUploadResult resultado =
                    azureBlobStorageService.uploadFile(
                            archivo.getInputStream(),
                            archivo.getSize(),
                            archivo.getContentType(),
                            titulo.getTitleName(),
                            MediaFile.FileType.TECHNICAL_SHEET,
                            archivo.getFileName()
                    );

            MediaFile archivoMedia = new MediaFile();
            archivoMedia.setMediaTitle(titulo);
            archivoMedia.setFileType(MediaFile.FileType.TECHNICAL_SHEET);
            archivoMedia.setBlobUrl(resultado.getBlobUrl());
            archivoMedia.setEtag(resultado.getEtag());
            archivoMedia.setContentType(resultado.getContentType());
            archivoMedia.setSizeBytes(resultado.getSizeBytes());
            archivoMedia.setUploadedBy(obtenerUsuarioActual());

            mediaFileService.guardar(archivoMedia);

            addMessage("Ficha técnica subida exitosamente", FacesMessage.SEVERITY_INFO);
            dialogFichaTecnicaVisible = false;

        } catch (Exception e) {
            addMessage("Error al subir ficha técnica: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    public void buscar() {
        titulos = mediaTitleService.buscar(buscarNombre, buscarTipo, buscarAnio, buscarGeneroId);
    }

    public void limpiarBusqueda() {
        buscarNombre = null;
        buscarTipo = null;
        buscarAnio = null;
        buscarGeneroId = null;
        cargarTitulos();
    }

    public boolean tienePoster(MediaTitle t) {
        return t != null && t.hasPoster();
    }

    private String obtenerUsuarioActual() {
        return "admin";
    }

    private void clearFacesMessages() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) return;
        for (Iterator<FacesMessage> it = ctx.getMessages(); it.hasNext(); ) {
            it.next();
            it.remove();
        }
    }

    private void addMessage(String mensaje, FacesMessage.Severity severity) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, mensaje, null));
    }

    private String getFieldLabel(String fieldName) {
        Map<String, String> labels = new HashMap<>();
        labels.put("titleName", "Nombre del título");
        labels.put("titleType", "Tipo");
        labels.put("releaseYear", "Año de lanzamiento");
        labels.put("synopsis", "Sinopsis");
        labels.put("averageRating", "Calificación");
        return labels.getOrDefault(fieldName, fieldName);
    }

    public MediaTitle.TitleType[] getTiposTitulo() {
        return MediaTitle.TitleType.values();
    }

    // Getters y Setters
    public MediaTitle getTitulo() { return titulo; }
    public void setTitulo(MediaTitle titulo) { this.titulo = titulo; }

    public List<MediaTitle> getTitulos() { return titulos; }
    public void setTitulos(List<MediaTitle> titulos) { this.titulos = titulos; }

    public List<MediaTitle> getTitulosFiltrados() { return titulosFiltrados; }
    public void setTitulosFiltrados(List<MediaTitle> titulosFiltrados) { this.titulosFiltrados = titulosFiltrados; }

    public boolean isDialogVisible() { return dialogVisible; }
    public void setDialogVisible(boolean dialogVisible) { this.dialogVisible = dialogVisible; }

    public boolean isDialogPosterVisible() { return dialogPosterVisible; }
    public void setDialogPosterVisible(boolean dialogPosterVisible) { this.dialogPosterVisible = dialogPosterVisible; }

    public boolean isDialogFichaTecnicaVisible() { return dialogFichaTecnicaVisible; }
    public void setDialogFichaTecnicaVisible(boolean dialogFichaTecnicaVisible) { this.dialogFichaTecnicaVisible = dialogFichaTecnicaVisible; }

    public List<MovieGenre> getGenerosDisponibles() { return generosDisponibles; }
    public void setGenerosDisponibles(List<MovieGenre> generosDisponibles) { this.generosDisponibles = generosDisponibles; }

    public List<Long> getGenerosSeleccionados() { return generosSeleccionados; }
    public void setGenerosSeleccionados(List<Long> generosSeleccionados) { this.generosSeleccionados = generosSeleccionados; }

    public String getBuscarNombre() { return buscarNombre; }
    public void setBuscarNombre(String buscarNombre) { this.buscarNombre = buscarNombre; }

    public MediaTitle.TitleType getBuscarTipo() { return buscarTipo; }
    public void setBuscarTipo(MediaTitle.TitleType buscarTipo) { this.buscarTipo = buscarTipo; }

    public Integer getBuscarAnio() { return buscarAnio; }
    public void setBuscarAnio(Integer buscarAnio) { this.buscarAnio = buscarAnio; }

    public Long getBuscarGeneroId() { return buscarGeneroId; }
    public void setBuscarGeneroId(Long buscarGeneroId) { this.buscarGeneroId = buscarGeneroId; }
}
