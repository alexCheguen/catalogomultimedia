package com.catalogomultimedia.bean;

import com.catalogomultimedia.service.DashboardService;
import com.catalogomultimedia.service.DashboardService.DashboardStatistics;
import com.catalogomultimedia.service.MediaTitleService;
import com.catalogomultimedia.service.MovieGenreService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Named
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MediaTitleService mediaTitleService;

    @Inject
    private MovieGenreService movieGenreService;

    private Long totalTitles;
    private Long totalMovies;
    private Long totalSeries;
    private Long totalGenres;
    private Long titlesWithPoster;
    private Long titlesLastMonth;
    private Long totalFiles;
    private Double totalStorageMB;
    private Map<String, Long> filesByType;
    private Map<String, Long> topGenres;

    @PostConstruct
    public void Init(){
        cargarEstadisticas();
        refresh();
    }

    public void refresh(){
        totalTitles = mediaTitleService.countTotalTitles();
        totalMovies = mediaTitleService.countMovies();
        totalMovies = mediaTitleService.countSeries();
        totalGenres = movieGenreService.countTotalGenres();

        titlesWithPoster = mediaTitleService.countTitlesWithPoster();
        titlesLastMonth = mediaTitleService.countTitlesFromLastMonth();

        totalFiles = mediaTitleService.countTotalTitles();
    }
    public Long getTotalTitles() { return totalTitles; }
    public Long getTotalMovies() { return totalMovies; }
    public Long getTotalSeries() { return totalSeries; }
    public Long getTotalGenres() { return totalGenres; }
    public Long getTitlesWithPoster() { return titlesWithPoster; }
    public Long getTitlesLastMonth() { return titlesLastMonth; }
    public Long getTotalFiles() { return totalFiles; }
    public Double getTotalStorageMB() { return totalStorageMB; }
    public Map<String, Long> getFilesByType() { return filesByType; }
    public Map<String, Long> getTopGenres() { return topGenres; }

    @Inject
    private DashboardService dashboardService;

    private DashboardStatistics estadisticas;



    // 📊 Cargar estadísticas del dashboard
    public void cargarEstadisticas() {
        estadisticas = dashboardService.getDashboardStatistics();
    }

    // 🔄 Refrescar estadísticas
    public void refrescar() {
        cargarEstadisticas();
    }

    // 🧭 Getters y Setters
    public DashboardStatistics getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(DashboardStatistics estadisticas) {
        this.estadisticas = estadisticas;
    }
}