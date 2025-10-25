package com.catalogomultimedia.repository;

import com.catalogomultimedia.entity.MediaFile;
import com.catalogomultimedia.entity.MediaFile.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {

    // Buscar archivos por título
    List<MediaFile> findByMediaTitle_MediaTitleIdAndIsActiveTrue(Long mediaTitleId);

    // Buscar póster activo de un título
    Optional<MediaFile> findByMediaTitle_MediaTitleIdAndFileTypeAndIsActiveTrue(
            Long mediaTitleId, FileType fileType);

    // JPQL: Contar total de archivos en Azure Blob
    @Query("SELECT COUNT(mf) FROM MediaFile mf WHERE mf.isActive = true")
    Long countTotalFiles();

    // JPQL: Contar archivos por tipo
    @Query("SELECT mf.fileType, COUNT(mf) FROM MediaFile mf " +
            "WHERE mf.isActive = true GROUP BY mf.fileType")
    List<Object[]> countFilesByType();

    // JPQL: Tamaño total almacenado
    @Query("SELECT SUM(mf.sizeBytes) FROM MediaFile mf WHERE mf.isActive = true")
    Long calculateTotalStorageSize();

    // JPQL: Archivos recientes
    @Query("SELECT mf FROM MediaFile mf WHERE mf.isActive = true " +
            "ORDER BY mf.uploadedAt DESC")
    List<MediaFile> findRecentFiles();
}