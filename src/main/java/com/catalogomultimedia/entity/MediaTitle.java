package com.catalogomultimedia.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "media_titles")
public class MediaTitle implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_title_id")
    private Long id;

    @NotBlank(message = "El nombre del título es obligatorio")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    @Column(name = "title_name", nullable = false, length = 150)
    private String titleName;

    @NotNull(message = "El tipo de título es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "title_type", nullable = false, length = 20)
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "media_title_genres",
            joinColumns = @JoinColumn(name = "media_title_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_genre_id")
    )
    private Set<MovieGenre> genres = new HashSet<>();

    @OneToMany(mappedBy = "mediaTitle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaFile> mediaFiles = new ArrayList<>();

    // 🔹 Enumeración para tipos de título
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

    // 🔹 Constructores
    public MediaTitle() {
    }

    public MediaTitle(String titleName, TitleType titleType, Integer releaseYear) {
        this.titleName = titleName;
        this.titleType = titleType;
        this.releaseYear = releaseYear;
    }

    // 🔹 Lifecycle callback
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // 🔹 Getters y Setters
    public Long getMediaTitleId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public TitleType getTitleType() {
        return titleType;
    }

    public void setTitleType(TitleType titleType) {
        this.titleType = titleType;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<MovieGenre> getGenres() {
        return genres;
    }

    public void setGenres(Set<MovieGenre> genres) {
        this.genres = genres;
    }

    public List<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(List<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    // 🔹 Métodos utilitarios
    public void addGenre(MovieGenre genre) {
        this.genres.add(genre);
    }

    public void removeGenre(MovieGenre genre) {
        this.genres.remove(genre);
    }

    public void addMediaFile(MediaFile mediaFile) {
        this.mediaFiles.add(mediaFile);
        mediaFile.setMediaTitle(this);
    }

    public void removeMediaFile(MediaFile mediaFile) {
        this.mediaFiles.remove(mediaFile);
        mediaFile.setMediaTitle(null);
    }

    public boolean hasPoster() {
        return mediaFiles.stream()
                .anyMatch(file -> file.getFileType() == MediaFile.FileType.POSTER
                        && Boolean.TRUE.equals(file.getIsActive()));
    }

    public boolean hasTechnicalSheet() {
        return mediaFiles.stream()
                .anyMatch(file -> file.getFileType() == MediaFile.FileType.TECHNICAL_SHEET
                        && Boolean.TRUE.equals(file.getIsActive()));
    }

    public MediaFile getActivePoster() {
        return mediaFiles.stream()
                .filter(file -> file.getFileType() == MediaFile.FileType.POSTER
                        && Boolean.TRUE.equals(file.getIsActive()))
                .findFirst()
                .orElse(null);
    }

    public String getGenresAsString() {
        if (genres == null || genres.isEmpty()) {
            return "Sin géneros";
        }
        return genres.stream()
                .map(MovieGenre::getGenreName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    public boolean isMovie() {
        return TitleType.MOVIE.equals(titleType);
    }

    public boolean isSeries() {
        return TitleType.SERIES.equals(titleType);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MediaTitle{");
        sb.append("id=").append(id);
        sb.append(", titleName='").append(titleName).append('\'');
        sb.append(", titleType=").append(titleType);
        sb.append(", releaseYear=").append(releaseYear);
        sb.append(", averageRating=").append(averageRating);
        sb.append(", createdAt=").append(createdAt);
        sb.append('}');
        return sb.toString();
    }
}