package com.catalogomultimedia.bean;

import com.catalogomultimedia.service.DashboardService;
import com.catalogomultimedia.service.DashboardService.DashboardStatistics;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private DashboardService dashboardService;

    private DashboardStatistics estadisticas;

    @PostConstruct
    public void init() {
        cargarEstadisticas();
    }

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