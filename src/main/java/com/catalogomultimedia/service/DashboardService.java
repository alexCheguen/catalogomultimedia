package com.catalogomultimedia.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DashboardService {

    @Inject
    private MediaTitleService mediaTitleService;

    @Inject
    private MovieGenreService movieGenreService;

    @Inject
    private MediaFileService mediaFileService;

    @Inject
    private EntityManager em;

    public DashboardStatistics getDashboardStatistics() {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            DashboardStatistics stats = new DashboardStatistics();

            stats.setTotalTitles(mediaTitleService.countTotalTitles());
            stats.setTotalMovies(mediaTitleService.countMovies());
            stats.setTotalSeries(mediaTitleService.countSeries());
            stats.setTotalGenres(movieGenreService.countTotalGenres());
            stats.setTotalFiles(mediaFileService.countTotalFiles());
            stats.setTitlesWithPoster(mediaTitleService.countTitlesWithPoster());
            stats.setTitlesLastMonth(mediaTitleService.countTitlesFromLastMonth());



            tx.commit();
            return stats;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al obtener estadísticas del dashboard", e);
        }
    }

    // 📊 Clase interna que representa las estadísticas del panel
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

        // Getters y Setters
        public Long getTotalTitles() { return totalTitles; }
        public void setTotalTitles(Long totalTitles) { this.totalTitles = totalTitles; }

        public Long getTotalMovies() { return totalMovies; }
        public void setTotalMovies(Long totalMovies) { this.totalMovies = totalMovies; }

        public Long getTotalSeries() { return totalSeries; }
        public void setTotalSeries(Long totalSeries) { this.totalSeries = totalSeries; }

        public Long getTotalGenres() { return totalGenres; }
        public void setTotalGenres(Long totalGenres) { this.totalGenres = totalGenres; }

        public Long getTotalFiles() { return totalFiles; }
        public void setTotalFiles(Long totalFiles) { this.totalFiles = totalFiles; }

        public Long getTitlesWithPoster() { return titlesWithPoster; }
        public void setTitlesWithPoster(Long titlesWithPoster) { this.titlesWithPoster = titlesWithPoster; }

        public Long getTitlesLastMonth() { return titlesLastMonth; }
        public void setTitlesLastMonth(Long titlesLastMonth) { this.titlesLastMonth = titlesLastMonth; }

        public Double getTotalStorageMB() { return totalStorageMB; }
        public void setTotalStorageMB(Double totalStorageMB) { this.totalStorageMB = totalStorageMB; }

        public Map<String, Long> getFilesByType() { return filesByType; }
        public void setFilesByType(Map<String, Long> filesByType) { this.filesByType = filesByType; }

        public Map<String, Long> getTopGenres() { return topGenres; }
        public void setTopGenres(Map<String, Long> topGenres) { this.topGenres = topGenres; }
    }
}
