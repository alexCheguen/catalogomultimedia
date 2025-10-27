package com.catalogomultimedia.bean;

import com.catalogomultimedia.entity.MovieGenre;
import com.catalogomultimedia.service.MovieGenreService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.io.Serializable;
import java.util.*;

@Named
@ViewScoped
public class MovieGenreBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MovieGenreService movieGenreService;

    @Inject
    private Validator validator;

    private MovieGenre genero;
    private List<MovieGenre> generos;
    private boolean dialogVisible;

    @PostConstruct
    public void init() {
        genero = new MovieGenre();
        dialogVisible = false;
        cargarGeneros();
    }

    // 📦 Cargar lista de géneros
    public void cargarGeneros() {
        generos = movieGenreService.findAll();
    }

    // 🆕 Nuevo género
    public void nuevo() {
        clearFacesMessages();
        genero = new MovieGenre();
        dialogVisible = true;
    }

    // ✏️ Editar género existente
    public void editar(MovieGenre g) {
        clearFacesMessages();
        this.genero = g;
        dialogVisible = true;
    }

    // 💾 Guardar género (crear o actualizar)
    public void guardar() {
        clearFacesMessages();

        // Validar con Bean Validation
        Set<ConstraintViolation<MovieGenre>> violations = validator.validate(genero);
        if (!violations.isEmpty()) {
            for (ConstraintViolation<MovieGenre> violation : violations) {
                String field = violation.getPropertyPath().toString();
                String message = violation.getMessage();
                String label = getFieldLabel(field);

                FacesContext.getCurrentInstance().addMessage("frmGenero",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                label + ": " + message, null));
            }
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }

        try {
            if (genero.getMovieGenreId() == null) {
                movieGenreService.save(genero);
                addMessage("Género creado exitosamente", FacesMessage.SEVERITY_INFO);
            } else {
                movieGenreService.save(genero);
                addMessage("Género actualizado exitosamente", FacesMessage.SEVERITY_INFO);
            }

            genero = new MovieGenre();
            dialogVisible = false;
            cargarGeneros();

        } catch (Exception e) {
            addMessage("Error al guardar género: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    // 🗑️ Eliminar género
    public void eliminar(MovieGenre g) {
        try {
            movieGenreService.delete(g.getMovieGenreId());
            addMessage("Género eliminado exitosamente", FacesMessage.SEVERITY_INFO);
            cargarGeneros();
        } catch (Exception e) {
            addMessage("Error al eliminar género: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    // 🧹 Limpiar mensajes de FacesContext
    private void clearFacesMessages() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx == null) return;
        for (Iterator<FacesMessage> it = ctx.getMessages(); it.hasNext(); ) {
            it.next();
            it.remove();
        }
    }

    // 📢 Agregar mensaje global
    private void addMessage(String mensaje, FacesMessage.Severity severity) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, mensaje, null));
    }

    // 🏷️ Etiquetas legibles para validaciones
    private String getFieldLabel(String fieldName) {
        Map<String, String> labels = new HashMap<>();
        labels.put("genreName", "Nombre del género");
        return labels.getOrDefault(fieldName, fieldName);
    }

    // 🧭 Getters y Setters
    public MovieGenre getGenero() {
        return genero;
    }

    public void setGenero(MovieGenre genero) {
        this.genero = genero;
    }

    public List<MovieGenre> getGeneros() {
        return generos;
    }

    public void setGeneros(List<MovieGenre> generos) {
        this.generos = generos;
    }

    public boolean isDialogVisible() {
        return dialogVisible;
    }

    public void setDialogVisible(boolean dialogVisible) {
        this.dialogVisible = dialogVisible;
    }
}