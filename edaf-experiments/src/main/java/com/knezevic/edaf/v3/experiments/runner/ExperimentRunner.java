package com.knezevic.edaf.v3.experiments.runner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.knezevic.edaf.v3.core.api.AbstractEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.api.AlgorithmContext;
import com.knezevic.edaf.v3.core.api.AlgorithmState;
import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.core.events.CheckpointSavedEvent;
import com.knezevic.edaf.v3.core.events.EventBus;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.core.events.RunFailedEvent;
import com.knezevic.edaf.v3.core.events.RunResumedEvent;
import com.knezevic.edaf.v3.core.metrics.DefaultMetricCollector;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.Plugin;
import com.knezevic.edaf.v3.core.rng.RngManager;
import com.knezevic.edaf.v3.core.rng.RngSnapshot;
import com.knezevic.edaf.v3.experiments.factory.ComponentCatalog;
import com.knezevic.edaf.v3.experiments.factory.PolicyFactory;
import com.knezevic.edaf.v3.models.continuous.DiagonalGaussianModel;
import com.knezevic.edaf.v3.models.discrete.BernoulliUmdaModel;
import com.knezevic.edaf.v3.models.permutation.EdgeHistogramModel;
import com.knezevic.edaf.v3.persistence.checkpoint.CheckpointStore;
import com.knezevic.edaf.v3.persistence.jdbc.DataSourceFactory;
import com.knezevic.edaf.v3.persistence.jdbc.JdbcEventSink;
import com.knezevic.edaf.v3.persistence.jdbc.SchemaInitializer;
import com.knezevic.edaf.v3.persistence.sink.CsvMetricsSink;
import com.knezevic.edaf.v3.persistence.sink.JsonLinesEventSink;
import com.knezevic.edaf.v3.persistence.sink.RotatingFileEventSink;
import com.knezevic.edaf.v3.repr.types.BitString;
import com.knezevic.edaf.v3.repr.types.CategoricalVector;
import com.knezevic.edaf.v3.repr.types.IntVector;
import com.knezevic.edaf.v3.repr.types.MixedDiscreteVector;
import com.knezevic.edaf.v3.repr.types.MixedRealDiscreteVector;
import com.knezevic.edaf.v3.repr.types.PermutationVector;
import com.knezevic.edaf.v3.repr.types.RealVector;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * High-level experiment runner with checkpoint/resume support.
 */
public final class ExperimentRunner {

    private final ComponentCatalog catalog;
    private final CheckpointStore checkpointStore;
    private final ObjectMapper mapper;
    private final ObjectMapper canonicalJsonMapper;
    private final ObjectMapper canonicalYamlMapper;

    public ExperimentRunner() {
        this.catalog = new ComponentCatalog();
        this.checkpointStore = new CheckpointStore();
        this.mapper = new ObjectMapper();
        this.canonicalJsonMapper = new ObjectMapper()
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
        this.canonicalYamlMapper = new ObjectMapper(new YAMLFactory())
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public List<Plugin> listAlgorithms() {
        return catalog.listAlgorithms();
    }

    public List<Plugin> listModels() {
        return catalog.listModels();
    }

    public List<Plugin> listProblems() {
        return catalog.listProblems();
    }

    public RunExecution run(ExperimentConfig config, List<EventSink> additionalSinks) {
        SinkSetup sinkSetup = buildSinks(config, additionalSinks);
        EventBus eventBus = new EventBus();
        sinkSetup.sinks().forEach(eventBus::register);

        RngManager rng = new RngManager(config.getRun().getMasterSeed());

        try {
            RuntimeBundle bundle = createBundle(config, eventBus, rng);
            Algorithm<Object> algorithm = bundle.algorithm();
            AlgorithmContext<Object> context = bundle.context();

            algorithm.initialize(context);
            Path lastCheckpoint = runIterationsWithCheckpointing(config, algorithm, context, bundle.model(), rng, false);

            Map<String, String> artifacts = new LinkedHashMap<>(sinkSetup.artifacts());
            if (lastCheckpoint != null) {
                artifacts.put("checkpoint", lastCheckpoint.toString());
            }

            RunExecution execution = finalizeExecution(algorithm, context, artifacts, List.of());
            return execution;
        } catch (RuntimeException e) {
            publishRunFailed(eventBus, config, null, e);
            throw e;
        } finally {
            eventBus.close();
        }
    }

    public RunExecution resume(Path checkpointPath, List<EventSink> additionalSinks) {
        JsonNode payload = checkpointStore.load(checkpointPath);
        ExperimentConfig config;
        try {
            config = mapper.treeToValue(payload.path("config"), ExperimentConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed decoding checkpoint config", e);
        }

        SinkSetup sinkSetup = buildSinks(config, additionalSinks);
        EventBus eventBus = new EventBus();
        sinkSetup.sinks().forEach(eventBus::register);

        RngManager rng = new RngManager(payload.path("rng").path("masterSeed").asLong(config.getRun().getMasterSeed()));

        try {
            RuntimeBundle bundle = createBundle(config, eventBus, rng);
            restoreModelState(bundle.model(), payload.path("modelState"));
            restoreRng(rng, payload.path("rng"));

            Population<Object> population = deserializePopulation(
                    payload.path("population"),
                    config.getRepresentation().getType(),
                    bundle.problem().objectiveSense()
            );
            population.sortByFitness();

            AlgorithmState<Object> restoredState = new AlgorithmState<>(
                    payload.path("runId").asText(config.getRun().getId()),
                    bundle.algorithm().id(),
                    payload.path("iteration").asInt(0),
                    payload.path("evaluations").asLong(population.size()),
                    Instant.parse(payload.path("startedAt").asText(Instant.now().toString())),
                    population,
                    population.best()
            );

            if (bundle.algorithm() instanceof AbstractEdaAlgorithm<Object> resumable) {
                resumable.restoreState(restoredState);
            } else {
                throw new IllegalStateException("Algorithm does not support resume: " + bundle.algorithm().id());
            }

            eventBus.publish(new RunResumedEvent(
                    restoredState.runId(),
                    Instant.now(),
                    restoredState.iteration(),
                    checkpointPath.toString()
            ));

            Path lastCheckpoint = runIterationsWithCheckpointing(config, bundle.algorithm(), bundle.context(), bundle.model(), rng, true);

            Map<String, String> artifacts = new LinkedHashMap<>(sinkSetup.artifacts());
            artifacts.put("resumedFrom", checkpointPath.toString());
            if (lastCheckpoint != null) {
                artifacts.put("checkpoint", lastCheckpoint.toString());
            }

            return finalizeExecution(bundle.algorithm(), bundle.context(), artifacts,
                    List.of("Run resumed from checkpoint: " + checkpointPath));
        } catch (RuntimeException e) {
            publishRunFailed(eventBus, config, checkpointPath.toString(), e);
            throw e;
        } finally {
            eventBus.close();
        }
    }

    @SuppressWarnings("unchecked")
    private RuntimeBundle createBundle(ExperimentConfig config, EventBus eventBus, RngManager rng) {
        Representation<Object> representation = (Representation<Object>) catalog.createRepresentation(config);
        Problem<Object> problem = (Problem<Object>) catalog.createProblem(config);
        Model<Object> model = (Model<Object>) catalog.createModel(config);

        var selection = PolicyFactory.<Object>createSelection(config);
        var replacement = PolicyFactory.<Object>createReplacement(config);
        var stopping = PolicyFactory.<Object>createStopping(config);
        var constraint = PolicyFactory.<Object>createConstraintHandling(config);
        var localSearch = PolicyFactory.<Object>createLocalSearch(config);
        var restart = PolicyFactory.<Object>createRestartPolicy(config);
        var niching = PolicyFactory.<Object>createNichingPolicy(config);

        AlgorithmDependencies<Object> dependencies = new AlgorithmDependencies<>(
                representation,
                problem,
                model,
                selection,
                replacement,
                stopping,
                constraint
        );

        Algorithm<Object> algorithm = (Algorithm<Object>) catalog.createAlgorithm(config, dependencies);

        int populationSize = intParam(config.getAlgorithm().getParams(), "populationSize", 100);
        int elitism = intParam(config.getAlgorithm().getParams(), "elitism", 1);

        AlgorithmContext<Object> context = new AlgorithmContext.Builder<Object>()
                .runId(config.getRun().getId())
                .representation(representation)
                .problem(problem)
                .model(model)
                .selectionPolicy(selection)
                .replacementPolicy(replacement)
                .stoppingCondition(stopping)
                .constraintHandling(constraint)
                .localSearch(localSearch)
                .restartPolicy(restart)
                .nichingPolicy(niching)
                .metricCollectors(List.of(new DefaultMetricCollector<>()))
                .eventBus(eventBus)
                .rngManager(rng)
                .populationSize(populationSize)
                .elitism(elitism)
                .parameters(config.getAlgorithm().getParams())
                .build();

        return new RuntimeBundle(representation, problem, model, algorithm, context);
    }

    private Path runIterationsWithCheckpointing(ExperimentConfig config,
                                                Algorithm<Object> algorithm,
                                                AlgorithmContext<Object> context,
                                                Model<?> model,
                                                RngManager rng,
                                                boolean resumed) {
        Path lastCheckpoint = null;
        int checkpointEvery = config.getRun().getCheckpointEveryIterations();

        while (!context.stoppingCondition().shouldStop(algorithm.state())) {
            algorithm.iterate(context);
            if (checkpointEvery > 0 && algorithm.state().iteration() > 0
                    && algorithm.state().iteration() % checkpointEvery == 0) {
                lastCheckpoint = checkpointPath(config, algorithm.state());
                saveCheckpoint(lastCheckpoint, config, algorithm.state(), model, rng, context.representation().type());
                context.eventBus().publish(new CheckpointSavedEvent(
                        algorithm.state().runId(),
                        Instant.now(),
                        algorithm.state().iteration(),
                        lastCheckpoint.toString()
                ));
            }
        }
        return lastCheckpoint;
    }

    private RunExecution finalizeExecution(Algorithm<Object> algorithm,
                                           AlgorithmContext<Object> context,
                                           Map<String, String> artifacts,
                                           List<String> warnings) {
        if (algorithm instanceof AbstractEdaAlgorithm<Object> concrete) {
            concrete.complete(context, artifacts);
        }
        return new RunExecution(algorithm.result(), artifacts, warnings);
    }

    private SinkSetup buildSinks(ExperimentConfig config, List<EventSink> additionalSinks) {
        List<EventSink> sinks = new ArrayList<>();
        Map<String, String> artifacts = new LinkedHashMap<>();
        String canonicalJson = toCanonicalJson(config);
        String canonicalYaml = toCanonicalYaml(config);

        sinks.addAll(additionalSinks);

        String outputDirectory = config.getPersistence().getOutputDirectory();
        Path outputDir = Path.of(outputDirectory);

        List<String> sinkNames = new ArrayList<>(config.getPersistence().getSinks());
        if (config.getLogging().getModes() != null) {
            for (String mode : config.getLogging().getModes()) {
                if (!sinkNames.contains(mode)) {
                    sinkNames.add(mode);
                }
            }
        }

        for (String sinkNameRaw : sinkNames) {
            String sinkName = sinkNameRaw.toLowerCase(Locale.ROOT);
            switch (sinkName) {
                case "csv" -> {
                    Path csv = outputDir.resolve(config.getRun().getId() + ".csv");
                    sinks.add(new CsvMetricsSink(csv));
                    artifacts.put("csv", csv.toString());
                }
                case "jsonl" -> {
                    Path jsonl = outputDir.resolve(config.getRun().getId() + ".jsonl");
                    sinks.add(new JsonLinesEventSink(jsonl));
                    artifacts.put("jsonl", jsonl.toString());
                }
                case "file" -> {
                    Path file = Path.of(config.getLogging().getLogFile());
                    sinks.add(new RotatingFileEventSink(file, 10_000_000L));
                    artifacts.put("log", file.toString());
                }
                case "db" -> {
                    if (config.getPersistence().getDatabase().isEnabled()) {
                        var ds = DataSourceFactory.create(
                                config.getPersistence().getDatabase().getUrl(),
                                config.getPersistence().getDatabase().getUser(),
                                config.getPersistence().getDatabase().getPassword()
                        );
                        SchemaInitializer.initialize(ds);
                        sinks.add(new JdbcEventSink(ds, config, canonicalYaml, canonicalJson));
                        artifacts.put("database", config.getPersistence().getDatabase().getUrl());
                    }
                }
                default -> {
                    // console sink is handled by CLI; unknown sinks are ignored here.
                }
            }
        }

        return new SinkSetup(sinks, artifacts);
    }

    private Path checkpointPath(ExperimentConfig config, AlgorithmState<Object> state) {
        Path checkpoints = Path.of(config.getPersistence().getOutputDirectory(), "checkpoints");
        return checkpoints.resolve(state.runId() + "-iter-" + state.iteration() + ".ckpt.yaml");
    }

    private void saveCheckpoint(Path path,
                                ExperimentConfig config,
                                AlgorithmState<Object> state,
                                Model<?> model,
                                RngManager rng,
                                String representationType) {
        ObjectNode root = mapper.createObjectNode();
        root.set("config", mapper.valueToTree(config));
        root.put("runId", state.runId());
        root.put("algorithmId", state.algorithmId());
        root.put("iteration", state.iteration());
        root.put("evaluations", state.evaluations());
        root.put("startedAt", state.startedAt().toString());
        root.put("representationType", representationType);
        root.set("rng", mapper.valueToTree(rng.snapshot()));
        root.set("modelState", serializeModelState(model));
        root.set("population", serializePopulation(state.population(), representationType));
        checkpointStore.save(path, root);
    }

    private ArrayNode serializePopulation(Population<Object> population, String representationType) {
        ArrayNode array = mapper.createArrayNode();
        for (Individual<Object> individual : population) {
            ObjectNode node = array.addObject();
            node.put("fitness", individual.fitness().scalar());
            node.set("genotype", serializeGenotype(individual.genotype(), representationType));
        }
        return array;
    }

    private JsonNode serializeGenotype(Object genotype, String representationType) {
        String type = normalize(representationType);
        switch (type) {
            case "bitstring" -> {
                BitString value = (BitString) genotype;
                return mapper.valueToTree(value.genes());
            }
            case "real-vector" -> {
                RealVector value = (RealVector) genotype;
                return mapper.valueToTree(value.values());
            }
            case "permutation-vector" -> {
                PermutationVector value = (PermutationVector) genotype;
                return mapper.valueToTree(value.order());
            }
            case "int-vector" -> {
                return mapper.valueToTree(((IntVector) genotype).values());
            }
            case "categorical-vector" -> {
                return mapper.valueToTree(((CategoricalVector) genotype).categories());
            }
            case "mixed-discrete-vector" -> {
                return mapper.valueToTree(((MixedDiscreteVector) genotype).encodedValues());
            }
            case "mixed-real-discrete-vector" -> {
                return mapper.valueToTree(genotype);
            }
            case "variable-length-vector" -> {
                return mapper.valueToTree(((VariableLengthVector<?>) genotype).values());
            }
            default -> throw new IllegalArgumentException("Unsupported checkpoint genotype type: " + representationType);
        }
    }

    private Population<Object> deserializePopulation(JsonNode node, String representationType, ObjectiveSense sense) {
        Population<Object> population = new Population<>(sense);
        for (JsonNode row : node) {
            Object genotype = deserializeGenotype(row.path("genotype"), representationType);
            Fitness fitness = new ScalarFitness(row.path("fitness").asDouble());
            population.add(new Individual<>(genotype, fitness));
        }
        return population;
    }

    private Object deserializeGenotype(JsonNode node, String representationType) {
        String type = normalize(representationType);
        return switch (type) {
            case "bitstring" -> new BitString(mapper.convertValue(node, boolean[].class));
            case "real-vector" -> new RealVector(mapper.convertValue(node, double[].class));
            case "permutation-vector" -> new PermutationVector(mapper.convertValue(node, int[].class));
            case "int-vector" -> new IntVector(mapper.convertValue(node, int[].class));
            case "categorical-vector" -> new CategoricalVector(mapper.convertValue(node, String[].class));
            case "mixed-discrete-vector" -> new MixedDiscreteVector(mapper.convertValue(node, int[].class));
            case "mixed-real-discrete-vector" -> mapper.convertValue(node, MixedRealDiscreteVector.class);
            case "variable-length-vector" -> new VariableLengthVector<>(mapper.convertValue(node, List.class));
            default -> throw new IllegalArgumentException("Unsupported checkpoint genotype type: " + representationType);
        };
    }

    private ObjectNode serializeModelState(Model<?> model) {
        ObjectNode state = mapper.createObjectNode();
        if (model instanceof BernoulliUmdaModel bernoulli) {
            state.put("type", "umda-bernoulli");
            state.set("probabilities", mapper.valueToTree(bernoulli.probabilities()));
        } else if (model instanceof DiagonalGaussianModel gaussian) {
            state.put("type", "gaussian-diag");
            state.set("mean", mapper.valueToTree(gaussian.mean()));
            state.set("sigma", mapper.valueToTree(gaussian.sigma()));
        } else if (model instanceof EdgeHistogramModel ehm) {
            state.put("type", "ehm");
            state.set("transitions", mapper.valueToTree(ehm.transitions()));
        } else {
            state.put("type", model.name());
        }
        return state;
    }

    private void restoreModelState(Model<?> model, JsonNode state) {
        String type = normalize(state.path("type").asText(model.name()));
        if (model instanceof BernoulliUmdaModel bernoulli && "umda-bernoulli".equals(type)) {
            bernoulli.restore(mapper.convertValue(state.path("probabilities"), double[].class));
        } else if (model instanceof DiagonalGaussianModel gaussian && "gaussian-diag".equals(type)) {
            gaussian.restore(
                    mapper.convertValue(state.path("mean"), double[].class),
                    mapper.convertValue(state.path("sigma"), double[].class)
            );
        } else if (model instanceof EdgeHistogramModel ehm && "ehm".equals(type)) {
            ehm.restore(mapper.convertValue(state.path("transitions"), double[][].class));
        }
    }

    private void restoreRng(RngManager rng, JsonNode node) {
        RngSnapshot snapshot = mapper.convertValue(node, RngSnapshot.class);
        rng.restore(snapshot);
    }

    private void publishRunFailed(EventBus eventBus,
                                  ExperimentConfig config,
                                  String resumedFrom,
                                  RuntimeException error) {
        eventBus.publish(new RunFailedEvent(
                config.getRun().getId(),
                Instant.now(),
                config.getAlgorithm().getType(),
                config.getModel().getType(),
                config.getProblem().getType(),
                config.getRun().getMasterSeed(),
                errorMessage(error),
                resumedFrom
        ));
    }

    private String toCanonicalJson(ExperimentConfig config) {
        try {
            JsonNode tree = canonicalJsonMapper.valueToTree(config);
            return canonicalJsonMapper.writeValueAsString(tree);
        } catch (Exception e) {
            throw new RuntimeException("Failed serializing canonical JSON config", e);
        }
    }

    private String toCanonicalYaml(ExperimentConfig config) {
        try {
            JsonNode tree = canonicalJsonMapper.valueToTree(config);
            return canonicalYamlMapper.writeValueAsString(tree);
        } catch (Exception e) {
            throw new RuntimeException("Failed serializing canonical YAML config", e);
        }
    }

    private static String errorMessage(Throwable error) {
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return error.getClass().getSimpleName();
        }
        return message;
    }

    private static int intParam(Map<String, Object> params, String key, int defaultValue) {
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record SinkSetup(List<EventSink> sinks, Map<String, String> artifacts) {
    }

    private record RuntimeBundle(Representation<Object> representation,
                                 Problem<Object> problem,
                                 Model<Object> model,
                                 Algorithm<Object> algorithm,
                                 AlgorithmContext<Object> context) {
    }
}
