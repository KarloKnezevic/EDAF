/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.events.EventBus;
import com.knezevic.edaf.v3.core.rng.RngManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable runtime context passed to algorithms.
 *
 * @param <G> genotype value type
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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

    /**
     * Returns current run identifier.
     *
     * @return run identifier
     */
    public String runId() {
        return runId;
    }

    /**
     * Returns configured genotype representation.
     *
     * @return representation component
     */
    public Representation<G> representation() {
        return representation;
    }

    /**
     * Returns optimization problem instance.
     *
     * @return problem component
     */
    public Problem<G> problem() {
        return problem;
    }

    /**
     * Returns probabilistic model instance.
     *
     * @return model component
     */
    public Model<G> model() {
        return model;
    }

    /**
     * Returns configured selection policy.
     *
     * @return selection policy
     */
    public SelectionPolicy<G> selectionPolicy() {
        return selectionPolicy;
    }

    /**
     * Returns configured replacement policy.
     *
     * @return replacement policy
     */
    public ReplacementPolicy<G> replacementPolicy() {
        return replacementPolicy;
    }

    /**
     * Returns stopping condition used in run loop.
     *
     * @return stopping condition
     */
    public StoppingCondition<G> stoppingCondition() {
        return stoppingCondition;
    }

    /**
     * Returns constraint handling strategy.
     *
     * @return constraint handling strategy
     */
    public ConstraintHandling<G> constraintHandling() {
        return constraintHandling;
    }

    /**
     * Returns local search hook.
     *
     * @return local search implementation
     */
    public LocalSearch<G> localSearch() {
        return localSearch;
    }

    /**
     * Returns restart policy.
     *
     * @return restart policy
     */
    public RestartPolicy<G> restartPolicy() {
        return restartPolicy;
    }

    /**
     * Returns niching policy.
     *
     * @return niching policy
     */
    public NichingPolicy<G> nichingPolicy() {
        return nichingPolicy;
    }

    /**
     * Returns configured metric collectors.
     *
     * @return immutable collector list
     */
    public List<MetricCollector<G>> metricCollectors() {
        return metricCollectors;
    }

    /**
     * Returns event bus used for run telemetry.
     *
     * @return event bus
     */
    public EventBus eventBus() {
        return eventBus;
    }

    /**
     * Returns RNG manager that provides component-specific deterministic streams.
     *
     * @return rng manager
     */
    public RngManager rngManager() {
        return rngManager;
    }

    /**
     * Returns configured population size.
     *
     * @return population size
     */
    public int populationSize() {
        return populationSize;
    }

    /**
     * Returns configured elitism count.
     *
     * @return elitism count
     */
    public int elitism() {
        return elitism;
    }

    /**
     * Returns immutable free-form algorithm parameter map.
     *
     * @return parameter map
     */
    public Map<String, Object> parameters() {
        return parameters;
    }

    /**
     * Builder to keep algorithm setup explicit and readable.
     *
     * @param <G> genotype value type
     * @author Karlo Knezevic
     * @version EDAF 3.0.0
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

        /**
         * Sets run identifier.
         *
         * @param runId run identifier
         * @return this builder
         */
        public Builder<G> runId(String runId) { this.runId = runId; return this; }

        /**
         * Sets representation component.
         *
         * @param representation representation component
         * @return this builder
         */
        public Builder<G> representation(Representation<G> representation) { this.representation = representation; return this; }

        /**
         * Sets problem component.
         *
         * @param problem problem component
         * @return this builder
         */
        public Builder<G> problem(Problem<G> problem) { this.problem = problem; return this; }

        /**
         * Sets model component.
         *
         * @param model model component
         * @return this builder
         */
        public Builder<G> model(Model<G> model) { this.model = model; return this; }

        /**
         * Sets selection policy.
         *
         * @param selectionPolicy selection policy
         * @return this builder
         */
        public Builder<G> selectionPolicy(SelectionPolicy<G> selectionPolicy) { this.selectionPolicy = selectionPolicy; return this; }

        /**
         * Sets replacement policy.
         *
         * @param replacementPolicy replacement policy
         * @return this builder
         */
        public Builder<G> replacementPolicy(ReplacementPolicy<G> replacementPolicy) { this.replacementPolicy = replacementPolicy; return this; }

        /**
         * Sets stopping condition.
         *
         * @param stoppingCondition stopping condition
         * @return this builder
         */
        public Builder<G> stoppingCondition(StoppingCondition<G> stoppingCondition) { this.stoppingCondition = stoppingCondition; return this; }

        /**
         * Sets constraint handling strategy.
         *
         * @param constraintHandling constraint handling strategy
         * @return this builder
         */
        public Builder<G> constraintHandling(ConstraintHandling<G> constraintHandling) { this.constraintHandling = constraintHandling; return this; }

        /**
         * Sets local search strategy.
         *
         * @param localSearch local search strategy
         * @return this builder
         */
        public Builder<G> localSearch(LocalSearch<G> localSearch) { this.localSearch = localSearch; return this; }

        /**
         * Sets restart policy.
         *
         * @param restartPolicy restart policy
         * @return this builder
         */
        public Builder<G> restartPolicy(RestartPolicy<G> restartPolicy) { this.restartPolicy = restartPolicy; return this; }

        /**
         * Sets niching policy.
         *
         * @param nichingPolicy niching policy
         * @return this builder
         */
        public Builder<G> nichingPolicy(NichingPolicy<G> nichingPolicy) { this.nichingPolicy = nichingPolicy; return this; }

        /**
         * Sets metric collector list.
         *
         * @param metricCollectors metric collectors
         * @return this builder
         */
        public Builder<G> metricCollectors(List<MetricCollector<G>> metricCollectors) { this.metricCollectors = metricCollectors; return this; }

        /**
         * Sets event bus.
         *
         * @param eventBus event bus
         * @return this builder
         */
        public Builder<G> eventBus(EventBus eventBus) { this.eventBus = eventBus; return this; }

        /**
         * Sets RNG manager.
         *
         * @param rngManager rng manager
         * @return this builder
         */
        public Builder<G> rngManager(RngManager rngManager) { this.rngManager = rngManager; return this; }

        /**
         * Sets population size.
         *
         * @param populationSize population size
         * @return this builder
         */
        public Builder<G> populationSize(int populationSize) { this.populationSize = populationSize; return this; }

        /**
         * Sets elitism count.
         *
         * @param elitism elitism count
         * @return this builder
         */
        public Builder<G> elitism(int elitism) { this.elitism = elitism; return this; }

        /**
         * Sets free-form parameter map.
         *
         * @param parameters free-form parameter map
         * @return this builder
         */
        public Builder<G> parameters(Map<String, Object> parameters) { this.parameters = parameters; return this; }

        /**
         * Builds immutable algorithm context from provided builder values.
         *
         * @return immutable algorithm context
         */
        public AlgorithmContext<G> build() {
            return new AlgorithmContext<>(this);
        }
    }
}
