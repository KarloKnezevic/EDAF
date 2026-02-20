package com.knezevic.edaf.v3.coco.runner;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.knezevic.edaf.v3.coco.config.CocoCampaignConfig;
import com.knezevic.edaf.v3.coco.config.CocoConfigLoader;
import com.knezevic.edaf.v3.coco.model.CocoCampaignResult;
import com.knezevic.edaf.v3.coco.model.CocoTrialOutcome;
import com.knezevic.edaf.v3.coco.persistence.CocoJdbcStore;
import com.knezevic.edaf.v3.coco.report.CocoHtmlReportGenerator;
import com.knezevic.edaf.v3.core.config.ConfigLoader;
import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.experiments.runner.ExperimentRunner;
import com.knezevic.edaf.v3.experiments.runner.RunExecution;
import com.knezevic.edaf.v3.persistence.jdbc.DataSourceFactory;
import com.knezevic.edaf.v3.persistence.jdbc.SchemaInitializer;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Runs a COCO/BBOB campaign by expanding one config into many EDAF runs.
 */
public final class CocoCampaignRunner {

    private final CocoConfigLoader cocoConfigLoader;
    private final ConfigLoader experimentConfigLoader;
    private final ExperimentRunner experimentRunner;
    private final ObjectMapper mapper;
    private final ObjectMapper canonicalYamlMapper;

    public CocoCampaignRunner() {
        this.cocoConfigLoader = new CocoConfigLoader();
        this.experimentConfigLoader = new ConfigLoader();
        this.experimentRunner = new ExperimentRunner();
        this.mapper = new ObjectMapper();
        this.canonicalYamlMapper = new ObjectMapper(new YAMLFactory())
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    /**
     * Executes campaign config and writes DB + HTML outputs.
     */
    public CocoCampaignResult run(Path campaignConfigPath, List<EventSink> additionalSinks) {
        CocoCampaignConfig campaignConfig = cocoConfigLoader.load(campaignConfigPath);
        CocoCampaignConfig.CampaignSection campaign = campaignConfig.getCampaign();

        DataSource dataSource = DataSourceFactory.create(
                campaign.getDatabaseUrl(),
                campaign.getDatabaseUser(),
                campaign.getDatabasePassword()
        );
        SchemaInitializer.initialize(dataSource);
        CocoJdbcStore store = new CocoJdbcStore(dataSource);
        store.upsertCampaignStarted(campaignConfig);

        int totalTrials = 0;
        int successfulTrials = 0;
        int failedTrials = 0;

        try {
            for (CocoCampaignConfig.OptimizerSection optimizer : campaignConfig.getOptimizers()) {
                Path optimizerConfigPath = resolveConfigPath(campaignConfigPath, optimizer.getConfig());
                ExperimentConfig baseConfig = experimentConfigLoader.load(optimizerConfigPath).config();

                String optimizerYaml;
                try {
                    optimizerYaml = canonicalYamlMapper.writeValueAsString(baseConfig);
                } catch (Exception e) {
                    optimizerYaml = "# failed to serialize optimizer config: " + e.getMessage();
                }
                store.upsertOptimizer(campaign.getId(), optimizer, baseConfig, optimizerYaml);

                for (int functionId : campaign.getFunctions()) {
                    for (int dimension : campaign.getDimensions()) {
                        for (int instanceId : campaign.getInstances()) {
                            for (int repetition = 1; repetition <= campaign.getRepetitions(); repetition++) {
                                totalTrials++;
                                String runId = buildRunId(campaign.getId(), optimizer.getId(), functionId, dimension, instanceId, repetition);
                                long budgetEvals = (long) campaign.getMaxEvaluationsMultiplier() * dimension;

                                ExperimentConfig trialConfig = cloneConfig(baseConfig);
                                applyCocoOverrides(
                                        trialConfig,
                                        campaign,
                                        optimizer,
                                        functionId,
                                        dimension,
                                        instanceId,
                                        repetition,
                                        runId,
                                        budgetEvals
                                );

                                try {
                                    RunExecution execution = experimentRunner.run(trialConfig, additionalSinks);
                                    Double best = execution.result().best().fitness().scalar();
                                    Long evalsToTarget = store.findEvaluationsToTarget(runId, campaign.getTargetFitness());
                                    boolean reachedTarget = best != null && best <= campaign.getTargetFitness();
                                    if (reachedTarget && evalsToTarget == null) {
                                        evalsToTarget = execution.result().evaluations();
                                    }

                                    if (reachedTarget) {
                                        successfulTrials++;
                                    }

                                    store.upsertTrial(new CocoTrialOutcome(
                                            campaign.getId(),
                                            optimizer.getId(),
                                            runId,
                                            functionId,
                                            instanceId,
                                            dimension,
                                            repetition,
                                            budgetEvals,
                                            execution.result().evaluations(),
                                            best,
                                            execution.result().runtime().toMillis(),
                                            "COMPLETED",
                                            reachedTarget,
                                            evalsToTarget,
                                            campaign.getTargetFitness()
                                    ));
                                } catch (RuntimeException e) {
                                    failedTrials++;
                                    store.upsertTrial(new CocoTrialOutcome(
                                            campaign.getId(),
                                            optimizer.getId(),
                                            runId,
                                            functionId,
                                            instanceId,
                                            dimension,
                                            repetition,
                                            budgetEvals,
                                            null,
                                            null,
                                            null,
                                            "FAILED",
                                            false,
                                            null,
                                            campaign.getTargetFitness()
                                    ));
                                }
                            }
                        }
                    }
                }
            }

            store.rebuildAggregates(
                    campaign.getId(),
                    campaign.getSuite(),
                    campaign.getTargetFitness(),
                    campaign.getReferenceMode()
            );

            Path reportDirectory = Path.of(campaign.getReportDirectory());
            CocoHtmlReportGenerator reportGenerator = new CocoHtmlReportGenerator();
            Path report = reportGenerator.generate(store.loadSnapshot(campaign.getId()), reportDirectory);

            String status = failedTrials == 0 ? "COMPLETED" : "COMPLETED_WITH_ERRORS";
            store.updateCampaignStatus(
                    campaign.getId(),
                    status,
                    "trials=" + totalTrials + ", success=" + successfulTrials + ", failed=" + failedTrials
            );

            Map<String, String> artifacts = new LinkedHashMap<>();
            artifacts.put("database", campaign.getDatabaseUrl());
            artifacts.put("html", report.toString());
            artifacts.put("campaignId", campaign.getId());

            return new CocoCampaignResult(
                    campaign.getId(),
                    totalTrials,
                    successfulTrials,
                    report,
                    artifacts
            );
        } catch (RuntimeException e) {
            store.updateCampaignStatus(campaign.getId(), "FAILED", e.getMessage());
            throw e;
        }
    }

    /**
     * Imports reference benchmark rows and returns imported count.
     */
    public int importReference(Path csvPath,
                               String dbUrl,
                               String dbUser,
                               String dbPassword,
                               String suite,
                               String sourceUrl) {
        DataSource dataSource = DataSourceFactory.create(dbUrl, dbUser, dbPassword);
        SchemaInitializer.initialize(dataSource);
        CocoJdbcStore store = new CocoJdbcStore(dataSource);
        return store.importReferenceCsv(csvPath, suite, sourceUrl);
    }

    /**
     * Generates campaign report from already persisted DB rows.
     */
    public Path generateReport(String campaignId,
                               String dbUrl,
                               String dbUser,
                               String dbPassword,
                               Path outputDir) {
        DataSource dataSource = DataSourceFactory.create(dbUrl, dbUser, dbPassword);
        SchemaInitializer.initialize(dataSource);
        CocoJdbcStore store = new CocoJdbcStore(dataSource);
        var snapshot = store.loadSnapshot(campaignId);
        if (snapshot == null) {
            throw new IllegalArgumentException("COCO campaign not found: " + campaignId);
        }
        CocoHtmlReportGenerator generator = new CocoHtmlReportGenerator();
        return generator.generate(snapshot, outputDir);
    }

    private static Path resolveConfigPath(Path campaignPath, String optimizerConfigPath) {
        Path raw = Path.of(optimizerConfigPath);
        if (raw.isAbsolute()) {
            return raw;
        }
        Path parent = campaignPath.getParent();
        if (parent == null) {
            return raw.normalize();
        }
        return parent.resolve(raw).normalize();
    }

    private ExperimentConfig cloneConfig(ExperimentConfig config) {
        return mapper.convertValue(config, ExperimentConfig.class);
    }

    private static void applyCocoOverrides(ExperimentConfig config,
                                           CocoCampaignConfig.CampaignSection campaign,
                                           CocoCampaignConfig.OptimizerSection optimizer,
                                           int functionId,
                                           int dimension,
                                           int instanceId,
                                           int repetition,
                                           String runId,
                                           long budgetEvaluations) {
        config.getRun().setId(runId);
        config.getRun().setName("COCO " + campaign.getSuite() + " " + optimizer.getId() + " f" + functionId
                + " d" + dimension + " i" + instanceId + " r" + repetition);

        long baseSeed = config.getRun().getMasterSeed();
        config.getRun().setMasterSeed(deriveSeed(baseSeed, runId, functionId, dimension, instanceId, repetition));

        config.getRepresentation().setType("real-vector");
        config.getRepresentation().getParams().clear();
        config.getRepresentation().getParams().put("length", dimension);
        config.getRepresentation().getParams().put("lower", -5.0);
        config.getRepresentation().getParams().put("upper", 5.0);

        config.getProblem().setType("coco-bbob");
        config.getProblem().getParams().clear();
        config.getProblem().getParams().put("suite", campaign.getSuite());
        config.getProblem().getParams().put("functionId", functionId);
        config.getProblem().getParams().put("dimension", dimension);
        config.getProblem().getParams().put("instanceId", instanceId);

        autoTuneCmaPopulation(config, dimension);

        int populationSize = intParam(config.getAlgorithm().getParams(), "populationSize", 100);
        int maxIterations = Math.max(1, (int) Math.ceil(budgetEvaluations / (double) Math.max(1, populationSize)));
        config.getStopping().setType("max-iterations");
        config.getStopping().setMaxIterations(maxIterations);

        Path outputDir = Path.of(campaign.getOutputDirectory(), campaign.getId(), optimizer.getId(),
                "f" + functionId + "-d" + dimension + "-i" + instanceId);
        config.getPersistence().setEnabled(true);
        config.getPersistence().setOutputDirectory(outputDir.toString());
        ensureToken(config.getPersistence().getSinks(), "csv");
        ensureToken(config.getPersistence().getSinks(), "jsonl");
        ensureToken(config.getPersistence().getSinks(), "db");
        config.getPersistence().getDatabase().setEnabled(true);
        config.getPersistence().getDatabase().setUrl(campaign.getDatabaseUrl());
        config.getPersistence().getDatabase().setUser(campaign.getDatabaseUser());
        config.getPersistence().getDatabase().setPassword(campaign.getDatabasePassword());

        ensureToken(config.getLogging().getModes(), "jsonl");
        ensureToken(config.getLogging().getModes(), "db");
        config.getLogging().setJsonlFile(outputDir.resolve(runId + "-events.jsonl").toString());
        config.getLogging().setLogFile(Path.of(campaign.getOutputDirectory(), campaign.getId(), "campaign.log").toString());
        if (config.getLogging().getVerbosity() == null || config.getLogging().getVerbosity().isBlank()) {
            config.getLogging().setVerbosity("normal");
        }

        applyOptimizerOverrides(config, optimizer.getOverrides());
    }

    private static void applyOptimizerOverrides(ExperimentConfig config, Map<String, Object> overrides) {
        if (overrides == null || overrides.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : overrides.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || key.isBlank()) {
                continue;
            }
            String normalized = key.trim().toLowerCase(Locale.ROOT);
            if ("masterseed".equals(normalized) && value instanceof Number n) {
                config.getRun().setMasterSeed(n.longValue());
                continue;
            }
            int dot = normalized.indexOf('.');
            if (dot < 0) {
                continue;
            }
            String section = normalized.substring(0, dot);
            String param = key.substring(dot + 1);
            switch (section) {
                case "algorithm" -> config.getAlgorithm().getParams().put(param, value);
                case "model" -> config.getModel().getParams().put(param, value);
                case "selection" -> config.getSelection().getParams().put(param, value);
                case "replacement" -> config.getReplacement().getParams().put(param, value);
                case "constraints" -> config.getConstraints().getParams().put(param, value);
                case "representation" -> config.getRepresentation().getParams().put(param, value);
                case "stopping" -> {
                    if ("maxiterations".equals(param.toLowerCase(Locale.ROOT)) && value instanceof Number n) {
                        config.getStopping().setMaxIterations(n.intValue());
                    }
                }
                default -> {
                    // ignore unknown override sections
                }
            }
        }
    }

    private static void autoTuneCmaPopulation(ExperimentConfig config, int dimension) {
        if (!"cma-es".equalsIgnoreCase(config.getAlgorithm().getType())) {
            return;
        }
        boolean autoPopulation = boolParam(config.getAlgorithm().getParams(), "autoPopulationSize", true);
        if (!autoPopulation) {
            return;
        }

        int tunedPopulation = Math.max(8, 4 + (int) Math.floor(3.0 * Math.log(Math.max(2, dimension))));
        config.getAlgorithm().getParams().put("populationSize", tunedPopulation);
        if (!config.getAlgorithm().getParams().containsKey("selectionRatio")) {
            config.getAlgorithm().getParams().put("selectionRatio", 0.5);
        }
    }

    private static void ensureToken(List<String> values, String token) {
        if (values == null) {
            return;
        }
        boolean present = values.stream().anyMatch(v -> token.equalsIgnoreCase(v));
        if (!present) {
            values.add(token);
        }
    }

    private static int intParam(Map<String, Object> params, String key, int defaultValue) {
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private static boolean boolParam(Map<String, Object> params, String key, boolean defaultValue) {
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            if ("true".equalsIgnoreCase(text)) {
                return true;
            }
            if ("false".equalsIgnoreCase(text)) {
                return false;
            }
        }
        return defaultValue;
    }

    private static String buildRunId(String campaignId,
                                     String optimizerId,
                                     int functionId,
                                     int dimension,
                                     int instanceId,
                                     int repetition) {
        return campaignId + "-" + optimizerId + "-f" + functionId + "-d" + dimension + "-i" + instanceId + "-r" + repetition;
    }

    private static long deriveSeed(long baseSeed,
                                   String runId,
                                   int functionId,
                                   int dimension,
                                   int instanceId,
                                   int repetition) {
        long x = baseSeed;
        x ^= 0x9E3779B97F4A7C15L * (functionId + 11L);
        x ^= 0xC2B2AE3D27D4EB4FL * (dimension + 17L);
        x ^= 0x165667B19E3779F9L * (instanceId + 23L);
        x ^= 0x85EBCA77C2B2AE63L * (repetition + 29L);
        x ^= Objects.hashCode(runId);
        return mix64(x);
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 33)) * 0xff51afd7ed558ccdl;
        z = (z ^ (z >>> 33)) * 0xc4ceb9fe1a85ec53l;
        z = z ^ (z >>> 33);
        return z;
    }
}
