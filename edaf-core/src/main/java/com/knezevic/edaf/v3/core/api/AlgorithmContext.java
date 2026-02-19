package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.events.EventBus;
import com.knezevic.edaf.v3.core.rng.RngManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable runtime context passed to algorithms.
 */
public final class AlgorithmContext<G> {

    private final String runId;
    private final Representation<G> representation;
    private final Problem<G> problem;
    private final Model<G> model;
    private final SelectionPolicy<G> selectionPolicy;
    private final ReplacementPolicy<G> replacementPolicy;
    private final StoppingCondition<G> stoppingCondition;
    private final ConstraintHandling<G> constraintHandling;
    private final LocalSearch<G> localSearch;
    private final RestartPolicy<G> restartPolicy;
    private final NichingPolicy<G> nichingPolicy;
    private final List<MetricCollector<G>> metricCollectors;
    private final EventBus eventBus;
    private final RngManager rngManager;
    private final int populationSize;
    private final int elitism;
    private final Map<String, Object> parameters;

    private AlgorithmContext(Builder<G> builder) {
        this.runId = Objects.requireNonNull(builder.runId, "runId must not be null");
        this.representation = Objects.requireNonNull(builder.representation, "representation must not be null");
        this.problem = Objects.requireNonNull(builder.problem, "problem must not be null");
        this.model = Objects.requireNonNull(builder.model, "model must not be null");
        this.selectionPolicy = Objects.requireNonNull(builder.selectionPolicy, "selectionPolicy must not be null");
        this.replacementPolicy = Objects.requireNonNull(builder.replacementPolicy, "replacementPolicy must not be null");
        this.stoppingCondition = Objects.requireNonNull(builder.stoppingCondition, "stoppingCondition must not be null");
        this.constraintHandling = Objects.requireNonNull(builder.constraintHandling, "constraintHandling must not be null");
        this.localSearch = Objects.requireNonNull(builder.localSearch, "localSearch must not be null");
        this.restartPolicy = Objects.requireNonNull(builder.restartPolicy, "restartPolicy must not be null");
        this.nichingPolicy = Objects.requireNonNull(builder.nichingPolicy, "nichingPolicy must not be null");
        this.metricCollectors = List.copyOf(builder.metricCollectors);
        this.eventBus = Objects.requireNonNull(builder.eventBus, "eventBus must not be null");
        this.rngManager = Objects.requireNonNull(builder.rngManager, "rngManager must not be null");
        this.populationSize = builder.populationSize;
        this.elitism = builder.elitism;
        this.parameters = Collections.unmodifiableMap(builder.parameters);
    }

    public String runId() {
        return runId;
    }

    public Representation<G> representation() {
        return representation;
    }

    public Problem<G> problem() {
        return problem;
    }

    public Model<G> model() {
        return model;
    }

    public SelectionPolicy<G> selectionPolicy() {
        return selectionPolicy;
    }

    public ReplacementPolicy<G> replacementPolicy() {
        return replacementPolicy;
    }

    public StoppingCondition<G> stoppingCondition() {
        return stoppingCondition;
    }

    public ConstraintHandling<G> constraintHandling() {
        return constraintHandling;
    }

    public LocalSearch<G> localSearch() {
        return localSearch;
    }

    public RestartPolicy<G> restartPolicy() {
        return restartPolicy;
    }

    public NichingPolicy<G> nichingPolicy() {
        return nichingPolicy;
    }

    public List<MetricCollector<G>> metricCollectors() {
        return metricCollectors;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    public RngManager rngManager() {
        return rngManager;
    }

    public int populationSize() {
        return populationSize;
    }

    public int elitism() {
        return elitism;
    }

    public Map<String, Object> parameters() {
        return parameters;
    }

    /**
     * Builder to keep algorithm setup explicit and readable.
     */
    public static final class Builder<G> {
        private String runId;
        private Representation<G> representation;
        private Problem<G> problem;
        private Model<G> model;
        private SelectionPolicy<G> selectionPolicy;
        private ReplacementPolicy<G> replacementPolicy;
        private StoppingCondition<G> stoppingCondition;
        private ConstraintHandling<G> constraintHandling;
        private LocalSearch<G> localSearch;
        private RestartPolicy<G> restartPolicy;
        private NichingPolicy<G> nichingPolicy;
        private List<MetricCollector<G>> metricCollectors = List.of();
        private EventBus eventBus;
        private RngManager rngManager;
        private int populationSize;
        private int elitism;
        private Map<String, Object> parameters = Map.of();

        public Builder<G> runId(String runId) { this.runId = runId; return this; }
        public Builder<G> representation(Representation<G> representation) { this.representation = representation; return this; }
        public Builder<G> problem(Problem<G> problem) { this.problem = problem; return this; }
        public Builder<G> model(Model<G> model) { this.model = model; return this; }
        public Builder<G> selectionPolicy(SelectionPolicy<G> selectionPolicy) { this.selectionPolicy = selectionPolicy; return this; }
        public Builder<G> replacementPolicy(ReplacementPolicy<G> replacementPolicy) { this.replacementPolicy = replacementPolicy; return this; }
        public Builder<G> stoppingCondition(StoppingCondition<G> stoppingCondition) { this.stoppingCondition = stoppingCondition; return this; }
        public Builder<G> constraintHandling(ConstraintHandling<G> constraintHandling) { this.constraintHandling = constraintHandling; return this; }
        public Builder<G> localSearch(LocalSearch<G> localSearch) { this.localSearch = localSearch; return this; }
        public Builder<G> restartPolicy(RestartPolicy<G> restartPolicy) { this.restartPolicy = restartPolicy; return this; }
        public Builder<G> nichingPolicy(NichingPolicy<G> nichingPolicy) { this.nichingPolicy = nichingPolicy; return this; }
        public Builder<G> metricCollectors(List<MetricCollector<G>> metricCollectors) { this.metricCollectors = metricCollectors; return this; }
        public Builder<G> eventBus(EventBus eventBus) { this.eventBus = eventBus; return this; }
        public Builder<G> rngManager(RngManager rngManager) { this.rngManager = rngManager; return this; }
        public Builder<G> populationSize(int populationSize) { this.populationSize = populationSize; return this; }
        public Builder<G> elitism(int elitism) { this.elitism = elitism; return this; }
        public Builder<G> parameters(Map<String, Object> parameters) { this.parameters = parameters; return this; }

        public AlgorithmContext<G> build() {
            return new AlgorithmContext<>(this);
        }
    }
}
