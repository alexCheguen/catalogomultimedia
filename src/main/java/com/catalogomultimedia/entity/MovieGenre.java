package com.catalogomultimedia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movie_genres")
public class MovieGenre implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_genre_id")
    private Long movieGenreId;

    @NotNull(message = "El nombre del género es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre del género debe tener entre 3 y 50 caracteres")
    @Column(name = "genre_name", length = 50, nullable = false, unique = true)
    private String genreName;

    @ManyToMany(mappedBy = "genres")
    private Set<MediaTitle> mediaTitles = new HashSet<>();

    public MovieGenre() {}

    public MovieGenre(String genreName) {
        this.genreName = genreName;
    }

    // Getters y Setters
    public Long getMovieGenreId() { return movieGenreId; }
    public void setMovieGenreId(Long movieGenreId) { this.movieGenreId = movieGenreId; }

    public String getGenreName() { return genreName; }
    public void setGenreName(String genreName) { this.genreName = genreName; }

    public Set<MediaTitle> getMediaTitles() { return mediaTitles; }
    public void setMediaTitles(Set<MediaTitle> mediaTitles) { this.mediaTitles = mediaTitles; }
}