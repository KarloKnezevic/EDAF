/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

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
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class ExperimentConfig {

    @NotBlank(message = "schema is required")
    private String schema = "3.0";

    @Valid
    @NotNull(message = "run section is required")
    private RunSection run = new RunSection();

    @Valid
    @NotNull(message = "grammar section is required")
    private GrammarSection grammar = new GrammarSection();

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
     * Executes get run.
     *
     * @return the run
     */
    public RunSection getRun() {
        return run;
    }

    /**
     * Executes set run.
     *
     * @param run the run argument
     */
    public void setRun(RunSection run) {
        this.run = run;
    }

    /**
     * Executes get grammar.
     *
     * @return the grammar
     */
    public GrammarSection getGrammar() {
        return grammar;
    }

    /**
     * Executes set grammar.
     *
     * @param grammar the grammar argument
     */
    public void setGrammar(GrammarSection grammar) {
        this.grammar = grammar;
    }

    /**
     * Executes get representation.
     *
     * @return the representation
     */
    public TypedSection getRepresentation() {
        return representation;
    }

    /**
     * Executes set representation.
     *
     * @param representation genotype representation
     */
    public void setRepresentation(TypedSection representation) {
        this.representation = representation;
    }

    /**
     * Executes get problem.
     *
     * @return the problem
     */
    public TypedSection getProblem() {
        return problem;
    }

    /**
     * Executes set problem.
     *
     * @param problem optimization problem
     */
    public void setProblem(TypedSection problem) {
        this.problem = problem;
    }

    /**
     * Executes get algorithm.
     *
     * @return the algorithm
     */
    public TypedSection getAlgorithm() {
        return algorithm;
    }

    /**
     * Executes set algorithm.
     *
     * @param algorithm the algorithm argument
     */
    public void setAlgorithm(TypedSection algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Executes get model.
     *
     * @return the model
     */
    public TypedSection getModel() {
        return model;
    }

    /**
     * Executes set model.
     *
     * @param model the model argument
     */
    public void setModel(TypedSection model) {
        this.model = model;
    }

    /**
     * Executes get selection.
     *
     * @return the selection
     */
    public TypedSection getSelection() {
        return selection;
    }

    /**
     * Executes set selection.
     *
     * @param selection the selection argument
     */
    public void setSelection(TypedSection selection) {
        this.selection = selection;
    }

    /**
     * Executes get replacement.
     *
     * @return the replacement
     */
    public TypedSection getReplacement() {
        return replacement;
    }

    /**
     * Executes set replacement.
     *
     * @param replacement the replacement argument
     */
    public void setReplacement(TypedSection replacement) {
        this.replacement = replacement;
    }

    /**
     * Executes get stopping.
     *
     * @return the stopping
     */
    public StoppingSection getStopping() {
        return stopping;
    }

    /**
     * Executes set stopping.
     *
     * @param stopping the stopping argument
     */
    public void setStopping(StoppingSection stopping) {
        this.stopping = stopping;
    }

    /**
     * Executes get constraints.
     *
     * @return the constraints
     */
    public TypedSection getConstraints() {
        return constraints;
    }

    /**
     * Executes set constraints.
     *
     * @param constraints the constraints argument
     */
    public void setConstraints(TypedSection constraints) {
        this.constraints = constraints;
    }

    /**
     * Executes get local search.
     *
     * @return the local search
     */
    public TypedSection getLocalSearch() {
        return localSearch;
    }

    /**
     * Executes set local search.
     *
     * @param localSearch local search strategy
     */
    public void setLocalSearch(TypedSection localSearch) {
        this.localSearch = localSearch;
    }

    /**
     * Executes get restart.
     *
     * @return the restart
     */
    public TypedSection getRestart() {
        return restart;
    }

    /**
     * Executes set restart.
     *
     * @param restart the restart argument
     */
    public void setRestart(TypedSection restart) {
        this.restart = restart;
    }

    /**
     * Executes get niching.
     *
     * @return the niching
     */
    public TypedSection getNiching() {
        return niching;
    }

    /**
     * Executes set niching.
     *
     * @param niching the niching argument
     */
    public void setNiching(TypedSection niching) {
        this.niching = niching;
    }

    /**
     * Executes get observability.
     *
     * @return the observability
     */
    public ObservabilitySection getObservability() {
        return observability;
    }

    /**
     * Executes set observability.
     *
     * @param observability the observability argument
     */
    public void setObservability(ObservabilitySection observability) {
        this.observability = observability;
    }

    /**
     * Executes get persistence.
     *
     * @return the persistence
     */
    public PersistenceSection getPersistence() {
        return persistence;
    }

    /**
     * Executes set persistence.
     *
     * @param persistence the persistence argument
     */
    public void setPersistence(PersistenceSection persistence) {
        this.persistence = persistence;
    }

    /**
     * Executes get reporting.
     *
     * @return the reporting
     */
    public ReportingSection getReporting() {
        return reporting;
    }

    /**
     * Executes set reporting.
     *
     * @param reporting the reporting argument
     */
    public void setReporting(ReportingSection reporting) {
        this.reporting = reporting;
    }

    /**
     * Executes get web.
     *
     * @return the web
     */
    public WebSection getWeb() {
        return web;
    }

    /**
     * Executes set web.
     *
     * @param web the web argument
     */
    public void setWeb(WebSection web) {
        this.web = web;
    }

    /**
     * Executes get logging.
     *
     * @return the logging
     */
    public LoggingSection getLogging() {
        return logging;
    }

    /**
     * Executes set logging.
     *
     * @param logging the logging argument
     */
    public void setLogging(LoggingSection logging) {
        this.logging = logging;
    }

    /**
     * Generic typed plugin section with free-form params.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class TypedSection {
        @NotBlank(message = "type is required")
        private String type;
        private final Map<String, Object> params = new LinkedHashMap<>();

        /**
         * Executes of.
         *
         * @param type the type argument
         * @return the of
         */
        public static TypedSection of(String type) {
            TypedSection section = new TypedSection();
            section.setType(type);
            return section;
        }

        /**
         * Executes get type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Executes set type.
         *
         * @param type the type argument
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Executes get params.
         *
         * @return the params
         */
        public Map<String, Object> getParams() {
            return params;
        }

        @JsonAnySetter
        /**
         * Executes add param.
         *
         * @param key the key argument
         * @param value the value argument
         */
        public void addParam(String key, Object value) {
            if (!"type".equals(key)) {
                params.put(key, value);
            }
        }
    }

    /**
     * Run identity and reproducibility section.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
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
        @Min(value = 1, message = "run.runCount must be >= 1")
        private int runCount = 1;

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
         * @param name identifier name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Executes get master seed.
         *
         * @return the master seed
         */
        public long getMasterSeed() {
            return masterSeed;
        }

        /**
         * Executes set master seed.
         *
         * @param masterSeed the masterSeed argument
         */
        public void setMasterSeed(long masterSeed) {
            this.masterSeed = masterSeed;
        }

        /**
         * Checks whether deterministic streams.
         *
         * @return true if deterministic streams; otherwise false
         */
        public boolean isDeterministicStreams() {
            return deterministicStreams;
        }

        /**
         * Executes set deterministic streams.
         *
         * @param deterministicStreams the deterministicStreams argument
         */
        public void setDeterministicStreams(boolean deterministicStreams) {
            this.deterministicStreams = deterministicStreams;
        }

        /**
         * Executes get checkpoint every iterations.
         *
         * @return the checkpoint every iterations
         */
        public int getCheckpointEveryIterations() {
            return checkpointEveryIterations;
        }

        /**
         * Executes set checkpoint every iterations.
         *
         * @param checkpointEveryIterations the checkpointEveryIterations argument
         */
        public void setCheckpointEveryIterations(int checkpointEveryIterations) {
            this.checkpointEveryIterations = checkpointEveryIterations;
        }

        /**
         * Executes get run count.
         *
         * @return the run count
         */
        public int getRunCount() {
            return runCount;
        }

        /**
         * Executes set run count.
         *
         * @param runCount the runCount argument
         */
        public void setRunCount(int runCount) {
            this.runCount = runCount;
        }
    }

    /**
     * Optional top-level grammar section injected into representation/problem params.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class GrammarSection {
        private final Map<String, Object> options = new LinkedHashMap<>();

        @JsonAnySetter
        /**
         * Executes add option.
         *
         * @param key the key argument
         * @param value the value argument
         */
        public void addOption(String key, Object value) {
            options.put(key, value);
        }

        /**
         * Executes get options.
         *
         * @return the options
         */
        public Map<String, Object> getOptions() {
            return options;
        }
    }

    /**
     * Stopping policy config.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
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

        /**
         * Executes get type.
         *
         * @return the type
         */
        public String getType() {
            return type;
        }

        /**
         * Executes set type.
         *
         * @param type the type argument
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Executes get max iterations.
         *
         * @return the max iterations
         */
        public int getMaxIterations() {
            return maxIterations;
        }

        /**
         * Executes set max iterations.
         *
         * @param maxIterations the maxIterations argument
         */
        public void setMaxIterations(int maxIterations) {
            this.maxIterations = maxIterations;
        }

        /**
         * Executes get max evaluations.
         *
         * @return the max evaluations
         */
        public Long getMaxEvaluations() {
            return maxEvaluations;
        }

        /**
         * Executes set max evaluations.
         *
         * @param maxEvaluations the maxEvaluations argument
         */
        public void setMaxEvaluations(Long maxEvaluations) {
            this.maxEvaluations = maxEvaluations;
        }

        /**
         * Executes get target fitness.
         *
         * @return the target fitness
         */
        public Double getTargetFitness() {
            return targetFitness;
        }

        /**
         * Executes set target fitness.
         *
         * @param targetFitness the targetFitness argument
         */
        public void setTargetFitness(Double targetFitness) {
            this.targetFitness = targetFitness;
        }
    }

    /**
     * Observability toggles.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class ObservabilitySection {
        @Min(value = 1, message = "observability.metricsEveryIterations must be >= 1")
        private int metricsEveryIterations = 1;
        private boolean emitModelDiagnostics = true;

        /**
         * Executes get metrics every iterations.
         *
         * @return the metrics every iterations
         */
        public int getMetricsEveryIterations() {
            return metricsEveryIterations;
        }

        /**
         * Executes set metrics every iterations.
         *
         * @param metricsEveryIterations the metricsEveryIterations argument
         */
        public void setMetricsEveryIterations(int metricsEveryIterations) {
            this.metricsEveryIterations = metricsEveryIterations;
        }

        /**
         * Checks whether emit model diagnostics.
         *
         * @return true if emit model diagnostics; otherwise false
         */
        public boolean isEmitModelDiagnostics() {
            return emitModelDiagnostics;
        }

        /**
         * Executes set emit model diagnostics.
         *
         * @param emitModelDiagnostics the emitModelDiagnostics argument
         */
        public void setEmitModelDiagnostics(boolean emitModelDiagnostics) {
            this.emitModelDiagnostics = emitModelDiagnostics;
        }
    }

    /**
     * Persistence targets and DB settings.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class PersistenceSection {
        private boolean enabled = true;
        private boolean bundleArtifacts = true;
        private List<String> sinks = List.of("console", "csv", "jsonl");
        private String outputDirectory = "./results";
        @Valid
        private DatabaseSection database = new DatabaseSection();

        /**
         * Checks whether enabled.
         *
         * @return true if enabled; otherwise false
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Executes set enabled.
         *
         * @param enabled the enabled argument
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Checks whether bundle artifacts.
         *
         * @return true if bundle artifacts; otherwise false
         */
        public boolean isBundleArtifacts() {
            return bundleArtifacts;
        }

        /**
         * Executes set bundle artifacts.
         *
         * @param bundleArtifacts the bundleArtifacts argument
         */
        public void setBundleArtifacts(boolean bundleArtifacts) {
            this.bundleArtifacts = bundleArtifacts;
        }

        /**
         * Executes get sinks.
         *
         * @return the sinks
         */
        public List<String> getSinks() {
            return sinks;
        }

        /**
         * Executes set sinks.
         *
         * @param sinks the sinks argument
         */
        public void setSinks(List<String> sinks) {
            this.sinks = sinks;
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
         * Executes get database.
         *
         * @return the database
         */
        public DatabaseSection getDatabase() {
            return database;
        }

        /**
         * Executes set database.
         *
         * @param database the database argument
         */
        public void setDatabase(DatabaseSection database) {
            this.database = database;
        }
    }

    /**
     * Relational database sink settings.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class DatabaseSection {
        private boolean enabled = false;
        private String url = "jdbc:sqlite:edaf-v3.db";
        private String user = "";
        private String password = "";

        /**
         * Checks whether enabled.
         *
         * @return true if enabled; otherwise false
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Executes set enabled.
         *
         * @param enabled the enabled argument
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Executes get url.
         *
         * @return the url
         */
        public String getUrl() {
            return url;
        }

        /**
         * Executes set url.
         *
         * @param url the url argument
         */
        public void setUrl(String url) {
            this.url = url;
        }

        /**
         * Executes get user.
         *
         * @return the user
         */
        public String getUser() {
            return user;
        }

        /**
         * Executes set user.
         *
         * @param user the user argument
         */
        public void setUser(String user) {
            this.user = user;
        }

        /**
         * Executes get password.
         *
         * @return the password
         */
        public String getPassword() {
            return password;
        }

        /**
         * Executes set password.
         *
         * @param password the password argument
         */
        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * Report generation settings.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class ReportingSection {
        private boolean enabled = true;
        private List<String> formats = List.of("html");
        private String outputDirectory = "./reports";

        /**
         * Checks whether enabled.
         *
         * @return true if enabled; otherwise false
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Executes set enabled.
         *
         * @param enabled the enabled argument
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Executes get formats.
         *
         * @return the formats
         */
        public List<String> getFormats() {
            return formats;
        }

        /**
         * Executes set formats.
         *
         * @param formats the formats argument
         */
        public void setFormats(List<String> formats) {
            this.formats = formats;
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
    }

    /**
     * Web dashboard settings.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class WebSection {
        private boolean enabled = false;
        private int port = 7070;
        @Min(value = 1, message = "web.pollSeconds must be >= 1")
        private int pollSeconds = 3;

        /**
         * Checks whether enabled.
         *
         * @return true if enabled; otherwise false
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Executes set enabled.
         *
         * @param enabled the enabled argument
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Executes get port.
         *
         * @return the port
         */
        public int getPort() {
            return port;
        }

        /**
         * Executes set port.
         *
         * @param port the port argument
         */
        public void setPort(int port) {
            this.port = port;
        }

        /**
         * Executes get poll seconds.
         *
         * @return the poll seconds
         */
        public int getPollSeconds() {
            return pollSeconds;
        }

        /**
         * Executes set poll seconds.
         *
         * @param pollSeconds the pollSeconds argument
         */
        public void setPollSeconds(int pollSeconds) {
            this.pollSeconds = pollSeconds;
        }
    }

    /**
     * Logging modes and verbosity settings.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class LoggingSection {
        private List<String> modes = List.of("console");
        @NotBlank(message = "logging.verbosity is required")
        private String verbosity = "normal";
        private String jsonlFile = "./results/run-events.jsonl";
        private String logFile = "./results/logs/edaf-v3.log";

        /**
         * Executes get modes.
         *
         * @return the modes
         */
        public List<String> getModes() {
            return modes;
        }

        /**
         * Executes set modes.
         *
         * @param modes the modes argument
         */
        public void setModes(List<String> modes) {
            this.modes = modes;
        }

        /**
         * Executes get verbosity.
         *
         * @return the verbosity
         */
        public String getVerbosity() {
            return verbosity;
        }

        /**
         * Executes set verbosity.
         *
         * @param verbosity the verbosity argument
         */
        public void setVerbosity(String verbosity) {
            this.verbosity = verbosity;
        }

        /**
         * Executes get jsonl file.
         *
         * @return the jsonl file
         */
        public String getJsonlFile() {
            return jsonlFile;
        }

        /**
         * Executes set jsonl file.
         *
         * @param jsonlFile the jsonlFile argument
         */
        public void setJsonlFile(String jsonlFile) {
            this.jsonlFile = jsonlFile;
        }

        /**
         * Executes get log file.
         *
         * @return the log file
         */
        public String getLogFile() {
            return logFile;
        }

        /**
         * Executes set log file.
         *
         * @param logFile the logFile argument
         */
        public void setLogFile(String logFile) {
            this.logFile = logFile;
        }
    }
}
