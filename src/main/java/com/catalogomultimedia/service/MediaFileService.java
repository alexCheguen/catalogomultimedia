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

    public void guardar(MediaFile mediaFile) {
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

    public MediaFile buscarPorId(Long id) {
        MediaFile file = em.find(MediaFile.class, id);
        if (file == null) {
            throw new IllegalArgumentException("Archivo no encontrado");
        }
        return file;
    }

    public List<MediaFile> buscarPorTituloId(Long mediaTitleId) {
        TypedQuery<MediaFile> query = em.createQuery(
                "SELECT mf FROM MediaFile mf WHERE mf.mediaTitle.mediaTitleId = :titleId " +
                        "AND mf.isActive = true ORDER BY mf.uploadedAt DESC",
                MediaFile.class);
        query.setParameter("titleId", mediaTitleId);
        return query.getResultList();
    }

    public MediaFile findActivePosterByTitleId(Long mediaTitleId) {
        TypedQuery<MediaFile> query = em.createQuery(
                "SELECT mf FROM MediaFile mf WHERE mf.mediaTitle.mediaTitleId = :titleId " +
                        "AND mf.fileType = :fileType AND mf.isActive = true",
                MediaFile.class);
        query.setParameter("titleId", mediaTitleId);
        query.setParameter("fileType", FileType.POSTER);

        List<MediaFile> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public Long countTotalFiles() {
        return em.createQuery(
                "SELECT COUNT(mf) FROM MediaFile mf WHERE mf.isActive = true",
                Long.class).getSingleResult();
    }

    public List<Object[]> contarArchivosPorTipo() {
        TypedQuery<Object[]> query = em.createQuery(
                "SELECT mf.fileType, COUNT(mf) FROM MediaFile mf " +
                        "WHERE mf.isActive = true GROUP BY mf.fileType",
                Object[].class);
        return query.getResultList();
    }

    public Long calcularTamanoTotalAlmacenamiento() {
        Long result = em.createQuery(
                "SELECT SUM(mf.sizeBytes) FROM MediaFile mf WHERE mf.isActive = true",
                Long.class).getSingleResult();
        return result != null ? result : 0L;
    }
}
