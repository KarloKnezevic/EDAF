/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.coco.config;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Top-level configuration for one COCO/BBOB campaign.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public final class CocoCampaignConfig {

    @NotBlank(message = "schema is required")
    private String schema = "3.0-coco";

    @Valid
    @NotNull(message = "campaign section is required")
    private CampaignSection campaign = new CampaignSection();

    @Valid
    @NotEmpty(message = "optimizers section must not be empty")
    private List<OptimizerSection> optimizers = new ArrayList<>();

    /**
     * Executes get schema.
     *
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Executes set schema.
     *
     * @param schema the schema argument
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Executes get campaign.
     *
     * @return the campaign
     */
    public CampaignSection getCampaign() {
        return campaign;
    }

    /**
     * Executes set campaign.
     *
     * @param campaign the campaign argument
     */
    public void setCampaign(CampaignSection campaign) {
        this.campaign = campaign;
    }

    /**
     * Executes get optimizers.
     *
     * @return the optimizers
     */
    public List<OptimizerSection> getOptimizers() {
        return optimizers;
    }

    /**
     * Executes set optimizers.
     *
     * @param optimizers the optimizers argument
     */
    public void setOptimizers(List<OptimizerSection> optimizers) {
        this.optimizers = optimizers;
    }

    /**
     * Global campaign settings.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static final class CampaignSection {

        @NotBlank(message = "campaign.id is required")
        private String id = "coco-campaign";

        @NotBlank(message = "campaign.name is required")
        private String name = "COCO/BBOB campaign";

        @NotBlank(message = "campaign.suite is required")
        private String suite = "bbob";

        @NotEmpty(message = "campaign.functions must not be empty")
        private List<Integer> functions = new ArrayList<>(List.of(1, 2, 3, 8, 15));

        @NotEmpty(message = "campaign.dimensions must not be empty")
        private List<Integer> dimensions = new ArrayList<>(List.of(2, 5, 10, 20));

        @NotEmpty(message = "campaign.instances must not be empty")
        private List<Integer> instances = new ArrayList<>(List.of(1, 2, 3));

        @Min(value = 1, message = "campaign.repetitions must be >= 1")
        private int repetitions = 3;

        @Min(value = 1, message = "campaign.maxEvaluationsMultiplier must be >= 1")
        private int maxEvaluationsMultiplier = 5000;

        @DecimalMin(value = "0.0", inclusive = false, message = "campaign.targetFitness must be > 0")
        @DecimalMax(value = "1.0", inclusive = true, message = "campaign.targetFitness must be <= 1")
        private double targetFitness = 1.0e-8;

        @NotBlank(message = "campaign.databaseUrl is required")
        private String databaseUrl = "jdbc:sqlite:edaf-v3.db";

        private String databaseUser = "";

        private String databasePassword = "";

        @NotBlank(message = "campaign.outputDirectory is required")
        private String outputDirectory = "./results/coco";

        @NotBlank(message = "campaign.reportDirectory is required")
        private String reportDirectory = "./reports/coco";

        @NotBlank(message = "campaign.referenceMode is required")
        private String referenceMode = "best-online";

        private String notes = "";

        /**
         * Executes get id.
         *
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * Executes set id.
         *
         * @param id the id argument
         */
        public void setId(String id) {
            this.id = id;
        }

        /**
         * Executes get name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Executes set name.
         *
         * @param name the name argument
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Executes get suite.
         *
         * @return the suite
         */
        public String getSuite() {
            return suite;
        }

        /**
         * Executes set suite.
         *
         * @param suite the suite argument
         */
        public void setSuite(String suite) {
            this.suite = suite;
        }

        /**
         * Executes get functions.
         *
         * @return the functions
         */
        public List<Integer> getFunctions() {
            return functions;
        }

        /**
         * Executes set functions.
         *
         * @param functions the functions argument
         */
        public void setFunctions(List<Integer> functions) {
            this.functions = functions;
        }

        /**
         * Executes get dimensions.
         *
         * @return the dimensions
         */
        public List<Integer> getDimensions() {
            return dimensions;
        }

        /**
         * Executes set dimensions.
         *
         * @param dimensions the dimensions argument
         */
        public void setDimensions(List<Integer> dimensions) {
            this.dimensions = dimensions;
        }

        /**
         * Executes get instances.
         *
         * @return the instances
         */
        public List<Integer> getInstances() {
            return instances;
        }

        /**
         * Executes set instances.
         *
         * @param instances the instances argument
         */
        public void setInstances(List<Integer> instances) {
            this.instances = instances;
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
         * Executes get max evaluations multiplier.
         *
         * @return the max evaluations multiplier
         */
        public int getMaxEvaluationsMultiplier() {
            return maxEvaluationsMultiplier;
        }

        /**
         * Executes set max evaluations multiplier.
         *
         * @param maxEvaluationsMultiplier the maxEvaluationsMultiplier argument
         */
        public void setMaxEvaluationsMultiplier(int maxEvaluationsMultiplier) {
            this.maxEvaluationsMultiplier = maxEvaluationsMultiplier;
        }

        /**
         * Executes get target fitness.
         *
         * @return the target fitness
         */
        public double getTargetFitness() {
            return targetFitness;
        }

        /**
         * Executes set target fitness.
         *
         * @param targetFitness the targetFitness argument
         */
        public void setTargetFitness(double targetFitness) {
            this.targetFitness = targetFitness;
        }

        /**
         * Executes get database url.
         *
         * @return the database url
         */
        public String getDatabaseUrl() {
            return databaseUrl;
        }

        /**
         * Executes set database url.
         *
         * @param databaseUrl the databaseUrl argument
         */
        public void setDatabaseUrl(String databaseUrl) {
            this.databaseUrl = databaseUrl;
        }

        /**
         * Executes get database user.
         *
         * @return the database user
         */
        public String getDatabaseUser() {
            return databaseUser;
        }

        /**
         * Executes set database user.
         *
         * @param databaseUser the databaseUser argument
         */
        public void setDatabaseUser(String databaseUser) {
            this.databaseUser = databaseUser;
        }

        /**
         * Executes get database password.
         *
         * @return the database password
         */
        public String getDatabasePassword() {
            return databasePassword;
        }

        /**
         * Executes set database password.
         *
         * @param databasePassword the databasePassword argument
         */
        public void setDatabasePassword(String databasePassword) {
            this.databasePassword = databasePassword;
        }

        /**
         * Executes get output directory.
         *
         * @return the output directory
         */
        public String getOutputDirectory() {
            return outputDirectory;
        }

        /**
         * Executes set output directory.
         *
         * @param outputDirectory the outputDirectory argument
         */
        public void setOutputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        /**
         * Executes get report directory.
         *
         * @return the report directory
         */
        public String getReportDirectory() {
            return reportDirectory;
        }

        /**
         * Executes set report directory.
         *
         * @param reportDirectory the reportDirectory argument
         */
        public void setReportDirectory(String reportDirectory) {
            this.reportDirectory = reportDirectory;
        }

        /**
         * Executes get reference mode.
         *
         * @return the reference mode
         */
        public String getReferenceMode() {
            return referenceMode;
        }

        /**
         * Executes set reference mode.
         *
         * @param referenceMode the referenceMode argument
         */
        public void setReferenceMode(String referenceMode) {
            this.referenceMode = referenceMode;
        }

        /**
         * Executes get notes.
         *
         * @return the notes
         */
        public String getNotes() {
            return notes;
        }

        /**
         * Executes set notes.
         *
         * @param notes the notes argument
         */
        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    /**
     * One optimizer entry participating in the campaign.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static final class OptimizerSection {

        @NotBlank(message = "optimizers[].id is required")
        private String id;

        @NotBlank(message = "optimizers[].config is required")
        private String config;

        private String displayName = "";

        private final Map<String, Object> overrides = new LinkedHashMap<>();

        /**
         * Executes get id.
         *
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * Executes set id.
         *
         * @param id the id argument
         */
        public void setId(String id) {
            this.id = id;
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
         * Executes get display name.
         *
         * @return the display name
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Executes set display name.
         *
         * @param displayName the displayName argument
         */
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Executes get overrides.
         *
         * @return the overrides
         */
        public Map<String, Object> getOverrides() {
            return overrides;
        }

        @JsonAnySetter
        /**
         * Executes add override.
         *
         * @param key the key argument
         * @param value the value argument
         */
        public void addOverride(String key, Object value) {
            if (!"id".equals(key) && !"config".equals(key) && !"displayName".equals(key)) {
                overrides.put(key, value);
            }
        }
    }
}
