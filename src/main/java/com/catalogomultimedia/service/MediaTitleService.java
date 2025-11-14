package com.catalogomultimedia.service;

import com.catalogomultimedia.entity.MediaTitle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class MediaTitleService {

    @Inject
    private EntityManager em;

    public void save(MediaTitle mediaTitle) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            int currentYear = LocalDateTime.now().getYear();
            if (mediaTitle.getReleaseYear() != null && mediaTitle.getReleaseYear() > currentYear) {
                throw new IllegalArgumentException("El año de lanzamiento no puede ser futuro");
            }

            if (mediaTitle.getGenres() == null || mediaTitle.getGenres().isEmpty()) {
                throw new IllegalArgumentException("Debe asignar al menos un género");
            }

            if (mediaTitle.getMediaTitleId() == null) {
                em.persist(mediaTitle);
            } else {
                em.merge(mediaTitle);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al guardar título multimedia", e);
        }
    }

    public MediaTitle buscarPorId(Long id) {
        MediaTitle title = em.find(MediaTitle.class, id);
        if (title == null) {
            throw new IllegalArgumentException("Título no encontrado");
        }
        return title;
    }

    public List<MediaTitle> findAll() {
        return em.createQuery(
                        "SELECT mt FROM MediaTitle mt ORDER BY mt.createdAt DESC", MediaTitle.class)
                .getResultList();
    }

    public void editar(MediaTitle mediaTitle) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Verificar que el registro existe antes de actualizar
            MediaTitle existente = em.find(MediaTitle.class, mediaTitle.getMediaTitleId());
            if (existente == null) {
                throw new RuntimeException("No se encontró el título multimedia con ID: " + mediaTitle.getMediaTitleId());
            }

            em.merge(mediaTitle);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al actualizar título multimedia", e);
        }
    }

    public void delete(MediaTitle mediaTitle) {

        MediaTitle managed = em.find(MediaTitle.class, mediaTitle.getMediaTitleId());
        if (managed != null) {
            em.remove(managed);
        } else {
            throw new IllegalArgumentException("El registro no existe o ya fue eliminado.");
        }
    }



    // Consultas para dashboard
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
        LocalDateTime haceUnMes = LocalDateTime.now().minusMonths(1);
        return em.createQuery(
                        "SELECT COUNT(mt) FROM MediaTitle mt WHERE mt.createdAt >= :fechaDesde",
                        Long.class)
                .setParameter("fechaDesde", haceUnMes)
                .getSingleResult();
    }


}
