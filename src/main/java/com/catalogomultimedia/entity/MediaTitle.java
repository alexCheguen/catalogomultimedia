package com.catalogomultimedia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "media_titles")
@Data
public class MediaTitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_title_id")
    private Long mediaTitleId;

    @NotNull(message = "El nombre del título es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    @Column(name = "title_name", length = 150, nullable = false)
    private String titleName;

    @NotNull(message = "El tipo de título es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "title_type", nullable = false)
    private TitleType titleType;

    @NotNull(message = "El año de lanzamiento es obligatorio")
    @Min(value = 1900, message = "El año debe ser mayor o igual a 1900")
    @Max(value = 2100, message = "El año no puede ser mayor a 2100")
    @Column(name = "release_year", nullable = false)
    private Integer releaseYear;

    @Size(max = 1000, message = "La sinopsis no puede exceder 1000 caracteres")
    @Column(name = "synopsis", length = 1000)
    private String synopsis;

    @DecimalMin(value = "0.0", message = "La calificación mínima es 0.0")
    @DecimalMax(value = "10.0", message = "La calificación máxima es 10.0")
    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "media_title_genres",
            joinColumns = @JoinColumn(name = "media_title_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_genre_id")
    )
    private Set<MovieGenre> genres = new HashSet<>();

    @OneToMany(mappedBy = "mediaTitle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MediaFile> mediaFiles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TitleType {
        MOVIE("Película"),
        SERIES("Serie");

        private final String displayName;

        TitleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public void addGenre(MovieGenre genre) {
        genres.add(genre);
        genre.getMediaTitles().add(this);
    }

    public void removeGenre(MovieGenre genre) {
        genres.remove(genre);
        genre.getMediaTitles().remove(this);
    }

    public boolean hasPoster() {
        return mediaFiles.stream()
                .anyMatch(file -> file.getFileType() == MediaFile.FileType.POSTER);
    }
}