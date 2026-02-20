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

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public CampaignSection getCampaign() {
        return campaign;
    }

    public void setCampaign(CampaignSection campaign) {
        this.campaign = campaign;
    }

    public List<OptimizerSection> getOptimizers() {
        return optimizers;
    }

    public void setOptimizers(List<OptimizerSection> optimizers) {
        this.optimizers = optimizers;
    }

    /**
     * Global campaign settings.
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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSuite() {
            return suite;
        }

        public void setSuite(String suite) {
            this.suite = suite;
        }

        public List<Integer> getFunctions() {
            return functions;
        }

        public void setFunctions(List<Integer> functions) {
            this.functions = functions;
        }

        public List<Integer> getDimensions() {
            return dimensions;
        }

        public void setDimensions(List<Integer> dimensions) {
            this.dimensions = dimensions;
        }

        public List<Integer> getInstances() {
            return instances;
        }

        public void setInstances(List<Integer> instances) {
            this.instances = instances;
        }

        public int getRepetitions() {
            return repetitions;
        }

        public void setRepetitions(int repetitions) {
            this.repetitions = repetitions;
        }

        public int getMaxEvaluationsMultiplier() {
            return maxEvaluationsMultiplier;
        }

        public void setMaxEvaluationsMultiplier(int maxEvaluationsMultiplier) {
            this.maxEvaluationsMultiplier = maxEvaluationsMultiplier;
        }

        public double getTargetFitness() {
            return targetFitness;
        }

        public void setTargetFitness(double targetFitness) {
            this.targetFitness = targetFitness;
        }

        public String getDatabaseUrl() {
            return databaseUrl;
        }

        public void setDatabaseUrl(String databaseUrl) {
            this.databaseUrl = databaseUrl;
        }

        public String getDatabaseUser() {
            return databaseUser;
        }

        public void setDatabaseUser(String databaseUser) {
            this.databaseUser = databaseUser;
        }

        public String getDatabasePassword() {
            return databasePassword;
        }

        public void setDatabasePassword(String databasePassword) {
            this.databasePassword = databasePassword;
        }

        public String getOutputDirectory() {
            return outputDirectory;
        }

        public void setOutputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        public String getReportDirectory() {
            return reportDirectory;
        }

        public void setReportDirectory(String reportDirectory) {
            this.reportDirectory = reportDirectory;
        }

        public String getReferenceMode() {
            return referenceMode;
        }

        public void setReferenceMode(String referenceMode) {
            this.referenceMode = referenceMode;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }

    /**
     * One optimizer entry participating in the campaign.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static final class OptimizerSection {

        @NotBlank(message = "optimizers[].id is required")
        private String id;

        @NotBlank(message = "optimizers[].config is required")
        private String config;

        private String displayName = "";

        private final Map<String, Object> overrides = new LinkedHashMap<>();

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Map<String, Object> getOverrides() {
            return overrides;
        }

        @JsonAnySetter
        public void addOverride(String key, Object value) {
            if (!"id".equals(key) && !"config".equals(key) && !"displayName".equals(key)) {
                overrides.put(key, value);
            }
        }
    }
}
