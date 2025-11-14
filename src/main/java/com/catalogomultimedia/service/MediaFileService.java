package com.catalogomultimedia.service;

import com.catalogomultimedia.entity.MediaFile;
import com.catalogomultimedia.entity.MediaFile.FileType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.util.List;

@ApplicationScoped
public class MediaFileService {

    @Inject
    private EntityManager em;

    public void delete(MediaFile mediaFile) {
        if (mediaFile == null || mediaFile.getMediaFileId() == null) {
            throw new IllegalArgumentException("Archivo no válido o sin ID.");
        }

        MediaFile managed = em.find(MediaFile.class, mediaFile.getMediaFileId());
        if (managed != null) {
            em.remove(managed);
        } else {
            throw new IllegalArgumentException("El archivo no existe o ya fue eliminado.");
        }
    }

    public void save(MediaFile mediaFile) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            if (mediaFile.getMediaFileId() == null) {
                em.persist(mediaFile);
            } else {
                em.merge(mediaFile);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al guardar archivo multimedia", e);
        }
    }
    public List<MediaFile> findAll() {
        TypedQuery<MediaFile> query = em.createQuery(
                "SELECT mf FROM MediaFile mf WHERE mf.isActive = true",
                MediaFile.class
        );
        return query.getResultList();
    }

    public Long countTotalFiles() {
        return em.createQuery(
                "SELECT COUNT(mf) FROM MediaFile mf WHERE mf.isActive = true",
                Long.class).getSingleResult();
    }
}
