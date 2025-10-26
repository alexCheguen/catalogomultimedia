package com.catalogomultimedia.service;

import com.catalogomultimedia.entity.MediaTitle;
import com.catalogomultimedia.entity.MovieGenre;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class MediaTitleService {

    @PersistenceContext(unitName = "CatalogoMultimediaPU")
    private EntityManager em;

    public MediaTitle save(MediaTitle mediaTitle) {
        // Validar año no sea futuro
        int currentYear = LocalDateTime.now().getYear();
        if (mediaTitle.getReleaseYear() != null && mediaTitle.getReleaseYear() > currentYear) {
            throw new IllegalArgumentException("El año de lanzamiento no puede ser futuro");
        }

        // Validar al menos un género
        if (mediaTitle.getGenres() == null || mediaTitle.getGenres().isEmpty()) {
            throw new IllegalArgumentException("Debe asignar al menos un género");
        }

        if (mediaTitle.getMediaTitleId() == null) {
            em.persist(mediaTitle);
            return mediaTitle;
        } else {
            return em.merge(mediaTitle);
        }
    }

    public MediaTitle findById(Long id) {
        MediaTitle title = em.find(MediaTitle.class, id);
        if (title == null) {
            throw new IllegalArgumentException("Título no encontrado");
        }
        return title;
    }

    public List<MediaTitle> findAll() {
        TypedQuery<MediaTitle> query = em.createQuery(
                "SELECT mt FROM MediaTitle mt ORDER BY mt.createdAt DESC", MediaTitle.class);
        return query.getResultList();
    }

    public void delete(Long id) {
        MediaTitle mediaTitle = findById(id);
        em.remove(mediaTitle);
    }

    public List<MediaTitle> search(String titleName, MediaTitle.TitleType type,
                                   Integer year, Long genreId) {
        StringBuilder jpql = new StringBuilder("SELECT mt FROM MediaTitle mt ");

        if (genreId != null) {
            jpql.append("JOIN mt.genres g ");
        }

        jpql.append("WHERE 1=1 ");

        if (titleName != null && !titleName.isEmpty()) {
            jpql.append("AND LOWER(mt.titleName) LIKE LOWER(:titleName) ");
        }
        if (type != null) {
            jpql.append("AND mt.titleType = :type ");
        }
        if (year != null) {
            jpql.append("AND mt.releaseYear = :year ");
        }
        if (genreId != null) {
            jpql.append("AND g.movieGenreId = :genreId ");
        }

        jpql.append("ORDER BY mt.createdAt DESC");

        TypedQuery<MediaTitle> query = em.createQuery(jpql.toString(), MediaTitle.class);

        if (titleName != null && !titleName.isEmpty()) {
            query.setParameter("titleName", "%" + titleName + "%");
        }
        if (type != null) {
            query.setParameter("type", type);
        }
        if (year != null) {
            query.setParameter("year", year);
        }
        if (genreId != null) {
            query.setParameter("genreId", genreId);
        }

        return query.getResultList();
    }

    // Consultas JPQL para Dashboard
    public Long countTotalTitles() {
        return em.createQuery("SELECT COUNT(mt) FROM MediaTitle mt", Long.class)
                .getSingleResult();
    }

    public Long countMovies() {
        return em.createQuery(
                        "SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.titleType = 'MOVIE'", Long.class)
                .getSingleResult();
    }

    public Long countSeries() {
        return em.createQuery(
                        "SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.titleType = 'SERIES'", Long.class)
                .getSingleResult();
    }

    public Long countTitlesWithPoster() {
        return em.createQuery(
                "SELECT COUNT(DISTINCT mt) FROM MediaTitle mt " +
                        "JOIN mt.mediaFiles mf WHERE mf.fileType = 'POSTER' AND mf.isActive = true",
                Long.class).getSingleResult();
    }

    public Long countTitlesFromLastMonth() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        return em.createQuery(
                        "SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.createdAt >= :dateFrom",
                        Long.class)
                .setParameter("dateFrom", oneMonthAgo)
                .getSingleResult();
    }
}