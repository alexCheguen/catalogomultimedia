package com.catalogomultimedia.repository;

import com.catalogomultimedia.entity.MediaTitle;
import com.catalogomultimedia.entity.MediaTitle.TitleType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MediaTitleRepository extends JpaRepository<MediaTitle, Long> {

    // Búsqueda por nombre
    List<MediaTitle> findByTitleNameContainingIgnoreCase(String titleName);

    // Búsqueda por tipo
    List<MediaTitle> findByTitleType(TitleType titleType);

    // Búsqueda por año
    List<MediaTitle> findByReleaseYear(Integer year);

    // JPQL: Contar total de títulos
    @Query("SELECT COUNT(mt) FROM MediaTitle mt")
    Long countTotalTitles();

    // JPQL: Contar películas
    @Query("SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.titleType = 'MOVIE'")
    Long countMovies();

    // JPQL: Contar series
    @Query("SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.titleType = 'SERIES'")
    Long countSeries();

    // JPQL: Títulos con póster asignado
    @Query("SELECT COUNT(DISTINCT mt) FROM MediaTitle mt " +
            "JOIN mt.mediaFiles mf WHERE mf.fileType = 'POSTER' AND mf.isActive = true")
    Long countTitlesWithPoster();

    // JPQL: Títulos registrados en el último mes
    @Query("SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.createdAt >= :dateFrom")
    Long countTitlesFromLastMonth(@Param("dateFrom") LocalDateTime dateFrom);

    // JPQL: Títulos por género
    @Query("SELECT mt FROM MediaTitle mt JOIN mt.genres g WHERE g.movieGenreId = :genreId")
    List<MediaTitle> findByGenreId(@Param("genreId") Long genreId);

    // JPQL: Títulos sin póster
    @Query("SELECT mt FROM MediaTitle mt WHERE mt.mediaTitleId NOT IN " +
            "(SELECT DISTINCT mf.mediaTitle.mediaTitleId FROM MediaFile mf " +
            "WHERE mf.fileType = 'POSTER' AND mf.isActive = true)")
    List<MediaTitle> findTitlesWithoutPoster();
}

