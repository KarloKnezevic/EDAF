package com.knezevic.edaf.v3.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Batch runner configuration listing experiment config files.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class BatchConfig {

    @NotEmpty(message = "batch.experiments must not be empty")
    private List<String> experiments;

    public List<String> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<String> experiments) {
        this.experiments = experiments;
    }
}
