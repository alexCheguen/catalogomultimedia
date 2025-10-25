package com.catalogomultimedia.service;

import com.catalogomultimedia.repository.MediaFileRepository;
import com.catalogomultimedia.repository.MediaTitleRepository;
import com.catalogomultimedia.repository.MovieGenreRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private MediaTitleRepository mediaTitleRepository;

    @Autowired
    private MovieGenreRepository movieGenreRepository;

    @Autowired
    private MediaFileRepository mediaFileRepository;

    /**
     * Obtiene todas las estadísticas del dashboard
     */
    public DashboardStatistics getDashboardStatistics() {
        DashboardStatistics stats = new DashboardStatistics();

        // Consultas JPQL
        stats.setTotalTitles(mediaTitleRepository.countTotalTitles());
        stats.setTotalMovies(mediaTitleRepository.countMovies());
        stats.setTotalSeries(mediaTitleRepository.countSeries());
        stats.setTotalGenres(movieGenreRepository.countTotalGenres());
        stats.setTotalFiles(mediaFileRepository.countTotalFiles());
        stats.setTitlesWithPoster(mediaTitleRepository.countTitlesWithPoster());

        // Títulos del último mes
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        stats.setTitlesLastMonth(mediaTitleRepository.countTitlesFromLastMonth(oneMonthAgo));

        // Archivos por tipo
        List<Object[]> filesByType = mediaFileRepository.countFilesByType();
        Map<String, Long> fileTypeMap = new HashMap<>();
        for (Object[] row : filesByType) {
            fileTypeMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.setFilesByType(fileTypeMap);

        // Tamaño total de almacenamiento
        Long totalSize = mediaFileRepository.calculateTotalStorageSize();
        stats.setTotalStorageMB(totalSize != null ? totalSize / (1024.0 * 1024.0) : 0.0);

        // Géneros más utilizados
        List<Object[]> topGenres = movieGenreRepository.findMostUsedGenres();
        Map<String, Long> genreMap = new HashMap<>();
        for (Object[] row : topGenres) {
            genreMap.put((String) row[0], (Long) row[1]);
        }
        stats.setTopGenres(genreMap);

        return stats;
    }

    /**
     * Clase DTO para las estadísticas del dashboard
     */
    @Data
    public static class DashboardStatistics {
        private Long totalTitles;
        private Long totalMovies;
        private Long totalSeries;
        private Long totalGenres;
        private Long totalFiles;
        private Long titlesWithPoster;
        private Long titlesLastMonth;
        private Double totalStorageMB;
        private Map<String, Long> filesByType;
        private Map<String, Long> topGenres;
    }
}