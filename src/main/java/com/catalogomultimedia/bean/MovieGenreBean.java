package com.catalogomultimedia.bean;

import com.catalogomultimedia.entity.MovieGenre;
import com.catalogomultimedia.service.MovieGenreService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("movieGenreBean")
@ViewScoped
public class MovieGenreBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private MovieGenreService movieGenreService;

    private List<MovieGenre> genres;
    private MovieGenre selectedGenre;
    private MovieGenre newGenre;

    @PostConstruct
    public void init() {
        loadGenres();
        newGenre = new MovieGenre();
    }

    public void loadGenres() {
        genres = movieGenreService.findAll();
    }

    public void prepareNewGenre() {
        newGenre = new MovieGenre();
    }

    public void saveGenre() {
        try {
            movieGenreService.save(newGenre);
            loadGenres();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Género guardado correctamente");
            prepareNewGenre();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void prepareEdit(MovieGenre genre) {
        selectedGenre = genre;
    }

    public void updateGenre() {
        try {
            movieGenreService.save(selectedGenre);
            loadGenres();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Género actualizado correctamente");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void deleteGenre(Long id) {
        try {
            movieGenreService.delete(id);
            loadGenres();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Género eliminado correctamente");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    public List<MovieGenre> getGenres() { return genres; }
    public void setGenres(List<MovieGenre> genres) { this.genres = genres; }

    public MovieGenre getSelectedGenre() { return selectedGenre; }
    public void setSelectedGenre(MovieGenre selectedGenre) { this.selectedGenre = selectedGenre; }

    public MovieGenre getNewGenre() { return newGenre; }
    public void setNewGenre(MovieGenre newGenre) { this.newGenre = newGenre; }
}