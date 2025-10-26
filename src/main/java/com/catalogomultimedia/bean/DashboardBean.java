package com.catalogomultimedia.bean;

import com.catalogomultimedia.service.DashboardService;
import com.catalogomultimedia.service.DashboardService.DashboardStatistics;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("dashboardBean")
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private DashboardService dashboardService;

    private DashboardStatistics statistics;

    @PostConstruct
    public void init() {
        loadStatistics();
    }

    public void loadStatistics() {
        statistics = dashboardService.getDashboardStatistics();
    }

    public void refresh() {
        loadStatistics();
    }

    public DashboardStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(DashboardStatistics statistics) {
        this.statistics = statistics;
    }
}