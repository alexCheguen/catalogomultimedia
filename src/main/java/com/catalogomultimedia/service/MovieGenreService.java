package com.catalogomultimedia.service;

import com.catalogomultimedia.entity.MovieGenre;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.List;

@ApplicationScoped
public class MovieGenreService {

    @Inject
    private EntityManager em;

    public void save(MovieGenre genre) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Si es un nuevo registro
            if (genre.getMovieGenreId() == null) {
                TypedQuery<Long> query = em.createQuery(
                        "SELECT COUNT(g) FROM MovieGenre g WHERE g.genreName = :name", Long.class);
                query.setParameter("name", genre.getGenreName());
                Long count = query.getSingleResult();

                if (count > 0) {
                    throw new IllegalArgumentException(
                            "Ya existe un género con el nombre: " + genre.getGenreName());
                }

                em.persist(genre);
            } else {
                em.merge(genre);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al guardar género", e);
        }
    }


    public MovieGenre getMovieGenreId(Long genreId) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            MovieGenre genre = em.find(MovieGenre.class, genreId);
            tx.commit();

            if (genre == null) {
                throw new IllegalArgumentException("Género no encontrado con ID: " + genreId);
            }

            return genre;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al obtener el género con ID: " + genreId, e);
        }
    }
    public MovieGenre buscarPorId(Long id) {
        MovieGenre genre = em.find(MovieGenre.class, id);
        if (genre == null) {
            throw new IllegalArgumentException("Género no encontrado");
        }
        return genre;
    }

    public List<MovieGenre> listAll() {
        return em.createQuery("SELECT g FROM MovieGenre g ORDER BY g.genreName", MovieGenre.class)
                .getResultList();
    }

    public void delete(Long id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            MovieGenre genre = em.find(MovieGenre.class, id);
            if (genre == null) {
                throw new IllegalArgumentException("Género no encontrado");
            }

            if (!genre.getMediaTitles().isEmpty()) {
                throw new IllegalArgumentException(
                        "No se puede eliminar el género porque tiene títulos asociados");
            }

            em.remove(genre);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al eliminar género", e);
        }
    }

    public Long countTotalGenres() {
        return em.createQuery("SELECT COUNT(g) FROM MovieGenre g", Long.class)
                .getSingleResult();
    }

    public List<Object[]> buscarGenerosMasUsados() {
        TypedQuery<Object[]> query = em.createQuery(
                "SELECT g.genreName, COUNT(mt) FROM MovieGenre g " +
                        "JOIN g.mediaTitles mt GROUP BY g.genreName ORDER BY COUNT(mt) DESC",
                Object[].class);
        return query.getResultList();
    }

    public MovieGenre findById(Long genreId) {
        MovieGenre genre = em.find(MovieGenre.class, genreId);
        if (genre == null) {
            throw new IllegalArgumentException("Género no encontrado con ID: " + genreId);
        }
        return genre;
    }

    public List<MovieGenre> findAll() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<MovieGenre> genres = em.createQuery(
                            "SELECT g FROM MovieGenre g ORDER BY g.genreName", MovieGenre.class)
                    .getResultList();
            tx.commit();
            return genres;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al listar los géneros", e);
        }
    }

}
