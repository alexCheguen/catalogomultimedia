package com.catalogomultimedia.service;

import com.catalogomultimedia.entity.MediaFile;
import com.catalogomultimedia.entity.MediaFile.FileType;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class MediaFileService {

    @PersistenceContext(unitName = "CatalogoMultimediaPU")
    private EntityManager em;

    public MediaFile save(MediaFile mediaFile) {
        if (mediaFile.getMediaFileId() == null) {
            em.persist(mediaFile);
            return mediaFile;
        } else {
            return em.merge(mediaFile);
        }
    }

    public MediaFile findById(Long id) {
        MediaFile file = em.find(MediaFile.class, id);
        if (file == null) {
            throw new IllegalArgumentException("Archivo no encontrado");
        }
        return file;
    }

    public List<MediaFile> findByMediaTitleId(Long mediaTitleId) {
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

    public List<Object[]> countFilesByType() {
        TypedQuery<Object[]> query = em.createQuery(
                "SELECT mf.fileType, COUNT(mf) FROM MediaFile mf " +
                        "WHERE mf.isActive = true GROUP BY mf.fileType",
                Object[].class);
        return query.getResultList();
    }

    public Long calculateTotalStorageSize() {
        Long result = em.createQuery(
                "SELECT SUM(mf.sizeBytes) FROM MediaFile mf WHERE mf.isActive = true",
                Long.class).getSingleResult();
        return result != null ? result : 0L;
    }
}