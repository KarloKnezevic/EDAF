/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

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
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class BatchConfig {

    @NotEmpty(message = "batch.experiments must not be empty")
    @Valid
    private List<BatchExperimentEntry> experiments = new ArrayList<>();

    @Min(value = 1, message = "batch.defaultRepetitions must be >= 1")
    private int defaultRepetitions = 1;

    private Long defaultSeedStart;

    /**
     * Executes get experiments.
     *
     * @return the experiments
     */
    public List<BatchExperimentEntry> getExperiments() {
        return experiments;
    }

    /**
     * Executes set experiments.
     *
     * @param experiments the experiments argument
     */
    public void setExperiments(List<BatchExperimentEntry> experiments) {
        this.experiments = experiments;
    }

    /**
     * Executes get default repetitions.
     *
     * @return the default repetitions
     */
    public int getDefaultRepetitions() {
        return defaultRepetitions;
    }

    /**
     * Executes set default repetitions.
     *
     * @param defaultRepetitions the defaultRepetitions argument
     */
    public void setDefaultRepetitions(int defaultRepetitions) {
        this.defaultRepetitions = defaultRepetitions;
    }

    /**
     * Executes get default seed start.
     *
     * @return the default seed start
     */
    public Long getDefaultSeedStart() {
        return defaultSeedStart;
    }

    /**
     * Executes set default seed start.
     *
     * @param defaultSeedStart the defaultSeedStart argument
     */
    public void setDefaultSeedStart(Long defaultSeedStart) {
        this.defaultSeedStart = defaultSeedStart;
    }

    /**
     * One experiment entry in batch files, optionally repeated with deterministic seed progression.
     *
     * @author Karlo Knezevic
     * @version EDAF 3.0.0
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class BatchExperimentEntry {

        @NotBlank(message = "batch.experiments[].config is required")
        private String config;

        @Min(value = 1, message = "batch.experiments[].repetitions must be >= 1")
        private int repetitions = 1;

        private Long seedStart;

        private String runIdPrefix;

        /**
         * Executes batch experiment entry.
         *
         * @return the batch experiment entry
         */
        public BatchExperimentEntry() {
        }

        /**
         * Executes batch experiment entry.
         *
         * @param config configuration object
         * @param repetitions the repetitions argument
         * @param seedStart the seedStart argument
         * @param runIdPrefix the runIdPrefix argument
         * @return the batch experiment entry
         */
        public BatchExperimentEntry(String config, int repetitions, Long seedStart, String runIdPrefix) {
            this.config = config;
            this.repetitions = repetitions;
            this.seedStart = seedStart;
            this.runIdPrefix = runIdPrefix;
        }

        /**
         * Executes get config.
         *
         * @return the config
         */
        public String getConfig() {
            return config;
        }

        /**
         * Executes set config.
         *
         * @param config configuration object
         */
        public void setConfig(String config) {
            this.config = config;
        }

        /**
         * Executes get repetitions.
         *
         * @return the repetitions
         */
        public int getRepetitions() {
            return repetitions;
        }

        /**
         * Executes set repetitions.
         *
         * @param repetitions the repetitions argument
         */
        public void setRepetitions(int repetitions) {
            this.repetitions = repetitions;
        }

        /**
         * Executes get seed start.
         *
         * @return the seed start
         */
        public Long getSeedStart() {
            return seedStart;
        }

        /**
         * Executes set seed start.
         *
         * @param seedStart the seedStart argument
         */
        public void setSeedStart(Long seedStart) {
            this.seedStart = seedStart;
        }

        /**
         * Executes get run id prefix.
         *
         * @return the run id prefix
         */
        public String getRunIdPrefix() {
            return runIdPrefix;
        }

        /**
         * Executes set run id prefix.
         *
         * @param runIdPrefix the runIdPrefix argument
         */
        public void setRunIdPrefix(String runIdPrefix) {
            this.runIdPrefix = runIdPrefix;
        }
    }
}
