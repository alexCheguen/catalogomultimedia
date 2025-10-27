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
    private Long id;

    @NotBlank(message = "El nombre del género es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre del género debe tener entre 3 y 50 caracteres")
    @Column(name = "genre_name", unique = true, nullable = false, length = 50)
    private String genreName;

    @ManyToMany(mappedBy = "genres")
    private Set<MediaTitle> mediaTitles = new HashSet<>();

    // 🔹 Constructores
    public MovieGenre() {
        // Constructor sin argumentos requerido por JPA
    }

    public MovieGenre(String genreName) {
        this.genreName = genreName;
    }

    // 🔹 Getters y Setters
    public Long getMovieGenreId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public Set<MediaTitle> getMediaTitles() {
        return mediaTitles;
    }

    public void setMediaTitles(Set<MediaTitle> mediaTitles) {
        this.mediaTitles = mediaTitles;
    }

    // 🔹 Métodos utilitarios
    public int getTitleCount() {
        return mediaTitles != null ? mediaTitles.size() : 0;
    }

    public boolean hasAssociatedTitles() {
        return mediaTitles != null && !mediaTitles.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MovieGenre)) return false;
        MovieGenre that = (MovieGenre) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MovieGenre{");
        sb.append("id=").append(id);
        sb.append(", genreName='").append(genreName).append('\'');
        sb.append(", titleCount=").append(getTitleCount());
        sb.append('}');
        return sb.toString();
    }
}
