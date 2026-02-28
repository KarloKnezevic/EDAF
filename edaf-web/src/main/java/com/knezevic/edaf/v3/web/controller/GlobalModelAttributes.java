/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.web.controller;

import com.knezevic.edaf.v3.web.service.DashboardStatsService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Provides shared model attributes for all Thymeleaf pages.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
@ControllerAdvice
public final class GlobalModelAttributes {

    private final DashboardStatsService dashboardStatsService;
    private final String frameworkVersion;

    public GlobalModelAttributes(
            DashboardStatsService dashboardStatsService,
            ObjectProvider<BuildProperties> buildPropertiesProvider,
            @Value("${edaf.framework.version:}") String frameworkVersion
    ) {
        this.dashboardStatsService = dashboardStatsService;
        this.frameworkVersion = resolveFrameworkVersion(frameworkVersion, buildPropertiesProvider.getIfAvailable());
    }

    @ModelAttribute("dashboardSummary")
    /**
     * Executes dashboard summary.
     *
     * @return the dashboard summary
     */
    public DashboardStatsService.DashboardSummary dashboardSummary() {
        return dashboardStatsService.readSummary();
    }

    @ModelAttribute("frameworkVersion")
    /**
     * Executes framework version.
     *
     * @return the framework version
     */
    public String frameworkVersion() {
        return frameworkVersion;
    }

    private static String resolveFrameworkVersion(String configuredVersion, BuildProperties buildProperties) {
        if (configuredVersion != null && !configuredVersion.isBlank()) {
            return configuredVersion.trim();
        }
        if (buildProperties != null && buildProperties.getVersion() != null && !buildProperties.getVersion().isBlank()) {
            return buildProperties.getVersion();
        }
        return "unknown";
    }
}
