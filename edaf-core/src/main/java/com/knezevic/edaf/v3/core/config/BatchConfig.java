package com.knezevic.edaf.v3.core.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

/**
 * Batch runner configuration listing experiment files and repetition controls.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class BatchConfig {

    @NotEmpty(message = "batch.experiments must not be empty")
    @Valid
    private List<BatchExperimentEntry> experiments = new ArrayList<>();

    @Min(value = 1, message = "batch.defaultRepetitions must be >= 1")
    private int defaultRepetitions = 1;

    private Long defaultSeedStart;

    public List<BatchExperimentEntry> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<BatchExperimentEntry> experiments) {
        this.experiments = experiments;
    }

    public int getDefaultRepetitions() {
        return defaultRepetitions;
    }

    public void setDefaultRepetitions(int defaultRepetitions) {
        this.defaultRepetitions = defaultRepetitions;
    }

    public Long getDefaultSeedStart() {
        return defaultSeedStart;
    }

    public void setDefaultSeedStart(Long defaultSeedStart) {
        this.defaultSeedStart = defaultSeedStart;
    }

    /**
     * One experiment entry in batch files, optionally repeated with deterministic seed progression.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class BatchExperimentEntry {

        @NotBlank(message = "batch.experiments[].config is required")
        private String config;

        @Min(value = 1, message = "batch.experiments[].repetitions must be >= 1")
        private int repetitions = 1;

        private Long seedStart;

        private String runIdPrefix;

        public BatchExperimentEntry() {
        }

        public BatchExperimentEntry(String config, int repetitions, Long seedStart, String runIdPrefix) {
            this.config = config;
            this.repetitions = repetitions;
            this.seedStart = seedStart;
            this.runIdPrefix = runIdPrefix;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public int getRepetitions() {
            return repetitions;
        }

        public void setRepetitions(int repetitions) {
            this.repetitions = repetitions;
        }

        public Long getSeedStart() {
            return seedStart;
        }

        public void setSeedStart(Long seedStart) {
            this.seedStart = seedStart;
        }

        public String getRunIdPrefix() {
            return runIdPrefix;
        }

        public void setRunIdPrefix(String runIdPrefix) {
            this.runIdPrefix = runIdPrefix;
        }
    }
}
