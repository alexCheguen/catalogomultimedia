package com.catalogomultimedia.service;

import com.catalogomultimedia.entity.MovieGenre;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class MovieGenreService {

    @PersistenceContext(unitName = "CatalogoMultimediaPU")
    private EntityManager em;

    public MovieGenre save(MovieGenre genre) {
        // Validar nombre único para nuevos géneros
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
            return genre;
        } else {
            return em.merge(genre);
        }
    }

    public MovieGenre findById(Long id) {
        MovieGenre genre = em.find(MovieGenre.class, id);
        if (genre == null) {
            throw new IllegalArgumentException("Género no encontrado");
        }
        return genre;
    }

    public List<MovieGenre> findAll() {
        TypedQuery<MovieGenre> query = em.createQuery(
                "SELECT g FROM MovieGenre g ORDER BY g.genreName", MovieGenre.class);
        return query.getResultList();
    }

    public void delete(Long id) {
        MovieGenre genre = findById(id);

        if (!genre.getMediaTitles().isEmpty()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el género porque tiene títulos asociados");
        }

        em.remove(genre);
    }

    public Long countTotalGenres() {
        return em.createQuery("SELECT COUNT(g) FROM MovieGenre g", Long.class)
                .getSingleResult();
    }

    public List<Object[]> findMostUsedGenres() {
        TypedQuery<Object[]> query = em.createQuery(
                "SELECT g.genreName, COUNT(mt) FROM MovieGenre g " +
                        "JOIN g.mediaTitles mt GROUP BY g.genreName ORDER BY COUNT(mt) DESC",
                Object[].class);
        return query.getResultList();
    }
}