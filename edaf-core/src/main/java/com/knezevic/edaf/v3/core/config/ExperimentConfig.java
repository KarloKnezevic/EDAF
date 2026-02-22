package com.knezevic.edaf.v3.core.config;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Unified v3 experiment configuration model.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class ExperimentConfig {

    @NotBlank(message = "schema is required")
    private String schema = "3.0";

    @Valid
    @NotNull(message = "run section is required")
    private RunSection run = new RunSection();

    @Valid
    @NotNull(message = "representation section is required")
    private TypedSection representation = new TypedSection();

    @Valid
    @NotNull(message = "problem section is required")
    private TypedSection problem = new TypedSection();

    @Valid
    @NotNull(message = "algorithm section is required")
    private TypedSection algorithm = new TypedSection();

    @Valid
    @NotNull(message = "model section is required")
    private TypedSection model = new TypedSection();

    @Valid
    @NotNull(message = "selection section is required")
    private TypedSection selection = new TypedSection();

    @Valid
    @NotNull(message = "replacement section is required")
    private TypedSection replacement = TypedSection.of("elitist");

    @Valid
    @NotNull(message = "stopping section is required")
    private StoppingSection stopping = new StoppingSection();

    @Valid
    @NotNull(message = "constraints section is required")
    private TypedSection constraints = TypedSection.of("identity");

    @Valid
    @NotNull(message = "localSearch section is required")
    private TypedSection localSearch = TypedSection.of("none");

    @Valid
    @NotNull(message = "restart section is required")
    private TypedSection restart = TypedSection.of("none");

    @Valid
    @NotNull(message = "niching section is required")
    private TypedSection niching = TypedSection.of("none");

    @Valid
    @NotNull(message = "observability section is required")
    private ObservabilitySection observability = new ObservabilitySection();

    @Valid
    @NotNull(message = "persistence section is required")
    private PersistenceSection persistence = new PersistenceSection();

    @Valid
    @NotNull(message = "reporting section is required")
    private ReportingSection reporting = new ReportingSection();

    @Valid
    @NotNull(message = "web section is required")
    private WebSection web = new WebSection();

    @Valid
    @NotNull(message = "logging section is required")
    private LoggingSection logging = new LoggingSection();

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public RunSection getRun() {
        return run;
    }

    public void setRun(RunSection run) {
        this.run = run;
    }

    public TypedSection getRepresentation() {
        return representation;
    }

    public void setRepresentation(TypedSection representation) {
        this.representation = representation;
    }

    public TypedSection getProblem() {
        return problem;
    }

    public void setProblem(TypedSection problem) {
        this.problem = problem;
    }

    public TypedSection getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(TypedSection algorithm) {
        this.algorithm = algorithm;
    }

    public TypedSection getModel() {
        return model;
    }

    public void setModel(TypedSection model) {
        this.model = model;
    }

    public TypedSection getSelection() {
        return selection;
    }

    public void setSelection(TypedSection selection) {
        this.selection = selection;
    }

    public TypedSection getReplacement() {
        return replacement;
    }

    public void setReplacement(TypedSection replacement) {
        this.replacement = replacement;
    }

    public StoppingSection getStopping() {
        return stopping;
    }

    public void setStopping(StoppingSection stopping) {
        this.stopping = stopping;
    }

    public TypedSection getConstraints() {
        return constraints;
    }

    public void setConstraints(TypedSection constraints) {
        this.constraints = constraints;
    }

    public TypedSection getLocalSearch() {
        return localSearch;
    }

    public void setLocalSearch(TypedSection localSearch) {
        this.localSearch = localSearch;
    }

    public TypedSection getRestart() {
        return restart;
    }

    public void setRestart(TypedSection restart) {
        this.restart = restart;
    }

    public TypedSection getNiching() {
        return niching;
    }

    public void setNiching(TypedSection niching) {
        this.niching = niching;
    }

    public ObservabilitySection getObservability() {
        return observability;
    }

    public void setObservability(ObservabilitySection observability) {
        this.observability = observability;
    }

    public PersistenceSection getPersistence() {
        return persistence;
    }

    public void setPersistence(PersistenceSection persistence) {
        this.persistence = persistence;
    }

    public ReportingSection getReporting() {
        return reporting;
    }

    public void setReporting(ReportingSection reporting) {
        this.reporting = reporting;
    }

    public WebSection getWeb() {
        return web;
    }

    public void setWeb(WebSection web) {
        this.web = web;
    }

    public LoggingSection getLogging() {
        return logging;
    }

    public void setLogging(LoggingSection logging) {
        this.logging = logging;
    }

    /**
     * Generic typed plugin section with free-form params.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class TypedSection {
        @NotBlank(message = "type is required")
        private String type;
        private final Map<String, Object> params = new LinkedHashMap<>();

        public static TypedSection of(String type) {
            TypedSection section = new TypedSection();
            section.setType(type);
            return section;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        @JsonAnySetter
        public void addParam(String key, Object value) {
            if (!"type".equals(key)) {
                params.put(key, value);
            }
        }
    }

    /**
     * Run identity and reproducibility section.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class RunSection {
        @NotBlank(message = "run.id is required")
        private String id = "run-" + UUID.randomUUID();
        private String name = "EDAF v3 run";
        private long masterSeed = 12345L;
        private boolean deterministicStreams = true;
        @Min(value = 0, message = "run.checkpointEveryIterations must be >= 0")
        private int checkpointEveryIterations = 0;

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

        public long getMasterSeed() {
            return masterSeed;
        }

        public void setMasterSeed(long masterSeed) {
            this.masterSeed = masterSeed;
        }

        public boolean isDeterministicStreams() {
            return deterministicStreams;
        }

        public void setDeterministicStreams(boolean deterministicStreams) {
            this.deterministicStreams = deterministicStreams;
        }

        public int getCheckpointEveryIterations() {
            return checkpointEveryIterations;
        }

        public void setCheckpointEveryIterations(int checkpointEveryIterations) {
            this.checkpointEveryIterations = checkpointEveryIterations;
        }
    }

    /**
     * Stopping policy config.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class StoppingSection {
        @NotBlank(message = "stopping.type is required")
        private String type = "max-iterations";
        @Min(value = 1, message = "stopping.maxIterations must be >= 1")
        private int maxIterations = 100;
        @Min(value = 1, message = "stopping.maxEvaluations must be >= 1 when set")
        private Long maxEvaluations;
        private Double targetFitness;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getMaxIterations() {
            return maxIterations;
        }

        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }

        public Long getMaxEvaluations() {
            return maxEvaluations;
        }

        public void setMaxEvaluations(Long maxEvaluations) {
            this.maxEvaluations = maxEvaluations;
        }

        public Double getTargetFitness() {
            return targetFitness;
        }

        public void setTargetFitness(Double targetFitness) {
            this.targetFitness = targetFitness;
        }
    }

    /**
     * Observability toggles.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class ObservabilitySection {
        @Min(value = 1, message = "observability.metricsEveryIterations must be >= 1")
        private int metricsEveryIterations = 1;
        private boolean emitModelDiagnostics = true;

        public int getMetricsEveryIterations() {
            return metricsEveryIterations;
        }

        public void setMetricsEveryIterations(int metricsEveryIterations) {
            this.metricsEveryIterations = metricsEveryIterations;
        }

        public boolean isEmitModelDiagnostics() {
            return emitModelDiagnostics;
        }

        public void setEmitModelDiagnostics(boolean emitModelDiagnostics) {
            this.emitModelDiagnostics = emitModelDiagnostics;
        }
    }

    /**
     * Persistence targets and DB settings.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class PersistenceSection {
        private boolean enabled = true;
        private boolean bundleArtifacts = true;
        private List<String> sinks = List.of("console", "csv", "jsonl");
        private String outputDirectory = "./results";
        @Valid
        private DatabaseSection database = new DatabaseSection();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isBundleArtifacts() {
            return bundleArtifacts;
        }

        public void setBundleArtifacts(boolean bundleArtifacts) {
            this.bundleArtifacts = bundleArtifacts;
        }

        public List<String> getSinks() {
            return sinks;
        }

        public void setSinks(List<String> sinks) {
            this.sinks = sinks;
        }

        public String getOutputDirectory() {
            return outputDirectory;
        }

        public void setOutputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        public DatabaseSection getDatabase() {
            return database;
        }

        public void setDatabase(DatabaseSection database) {
            this.database = database;
        }
    }

    /**
     * Relational database sink settings.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class DatabaseSection {
        private boolean enabled = false;
        private String url = "jdbc:sqlite:edaf-v3.db";
        private String user = "";
        private String password = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Report generation settings.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class ReportingSection {
        private boolean enabled = true;
        private List<String> formats = List.of("html");
        private String outputDirectory = "./reports";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getFormats() {
            return formats;
        }

        public void setFormats(List<String> formats) {
            this.formats = formats;
        }

        public String getOutputDirectory() {
            return outputDirectory;
        }

        public void setOutputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
        }
    }

    /**
     * Web dashboard settings.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class WebSection {
        private boolean enabled = false;
        private int port = 7070;
        @Min(value = 1, message = "web.pollSeconds must be >= 1")
        private int pollSeconds = 3;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getPollSeconds() {
            return pollSeconds;
        }

        public void setPollSeconds(int pollSeconds) {
            this.pollSeconds = pollSeconds;
        }
    }

    /**
     * Logging modes and verbosity settings.
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class LoggingSection {
        private List<String> modes = List.of("console");
        @NotBlank(message = "logging.verbosity is required")
        private String verbosity = "normal";
        private String jsonlFile = "./results/run-events.jsonl";
        private String logFile = "./edaf-v3.log";

        public List<String> getModes() {
            return modes;
        }

        public void setModes(List<String> modes) {
            this.modes = modes;
        }

        public String getVerbosity() {
            return verbosity;
        }

        public void setVerbosity(String verbosity) {
            this.verbosity = verbosity;
        }

        public String getJsonlFile() {
            return jsonlFile;
        }

        public void setJsonlFile(String jsonlFile) {
            this.jsonlFile = jsonlFile;
        }

        public String getLogFile() {
            return logFile;
        }

        public void setLogFile(String logFile) {
            this.logFile = logFile;
        }
    }
}
