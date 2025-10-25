package com.catalogomultimedia.bean;

import com.catalogomultimedia.service.DashboardService;
import com.catalogomultimedia.service.DashboardService.DashboardStatistics;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component("dashboardBean")
@ViewScoped
@Data
public class DashboardBean implements Serializable {

    @Autowired
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
}