package com.catalogomultimedia.repository;

import com.catalogomultimedia.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {

    Optional<MovieGenre> findByGenreName(String genreName);

    boolean existsByGenreName(String genreName);

    // JPQL: Contar géneros disponibles
    @Query("SELECT COUNT(g) FROM MovieGenre g")
    Long countTotalGenres();

    // JPQL: Géneros más utilizados
    @Query("SELECT g.genreName, COUNT(mt) FROM MovieGenre g " +
            "JOIN g.mediaTitles mt GROUP BY g.genreName ORDER BY COUNT(mt) DESC")
    List<Object[]> findMostUsedGenres();
}
