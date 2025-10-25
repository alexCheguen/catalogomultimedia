package com.catalogomultimedia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movie_genres")
@Data
public class MovieGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_genre_id")
    private Long movieGenreId;

    @NotNull(message = "El nombre del género es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre del género debe tener entre 3 y 50 caracteres")
    @Column(name = "genre_name", length = 50, nullable = false, unique = true)
    private String genreName;

    @ManyToMany(mappedBy = "genres")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<MediaTitle> mediaTitles = new HashSet<>();

    public MovieGenre() {}

    public MovieGenre(String genreName) {
        this.genreName = genreName;
    }
}