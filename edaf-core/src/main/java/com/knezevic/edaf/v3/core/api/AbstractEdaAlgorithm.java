package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.events.AdaptiveActionEvent;
import com.knezevic.edaf.v3.core.events.IterationCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunStartedEvent;
import com.knezevic.edaf.v3.core.metrics.LatentKnowledgeAnalyzer;
import com.knezevic.edaf.v3.core.metrics.PopulationMetrics;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.core.util.Params;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Template-method base implementation for model-based algorithms.
 */
public abstract class AbstractEdaAlgorithm<G> implements Algorithm<G> {

    private AlgorithmState<G> state;
    private RunResult<G> result;

    private LatentTelemetry previousTelemetry = LatentTelemetry.empty();
    private double previousBestFitness = Double.NaN;
    private int stagnationIterations;

    /**
     * Returns how many individuals are used for model fitting.
     */
    protected abstract int selectionSize(AlgorithmContext<G> context, Population<G> population);

    /**
     * Optional hook for algorithm-specific per-iteration behavior.
     */
    protected void afterIteration(AlgorithmContext<G> context, Population<G> previous, Population<G> next) {
        // default no-op
    }

    /**
     * Evaluates one feasible genotype.
     *
     * <p>Specialized algorithms can override this hook for noisy resampling or
     * surrogate-assisted evaluation while preserving the shared iteration flow.</p>
     */
    protected Fitness evaluateGenotype(AlgorithmContext<G> context, G feasibleGenotype, RngStream evaluationRng) {
        return context.problem().evaluate(feasibleGenotype);
    }

    /**
     * Allows algorithm-specific population post-processing after replacement,
     * niching, and restarts (for example random immigrants injection).
     */
    protected Population<G> postProcessPopulation(AlgorithmContext<G> context,
                                                  Population<G> previous,
                                                  Population<G> next) {
        return next;
    }

    @Override
    public void initialize(AlgorithmContext<G> context) {
        Population<G> population = new Population<>(context.problem().objectiveSense());
        RngStream initRng = context.rngManager().stream("init");

        for (int i = 0; i < context.populationSize(); i++) {
            G genotype = context.representation().random(initRng);
            genotype = context.constraintHandling().enforce(genotype, context.representation(), context.problem(), initRng);
            Fitness fitness = context.problem().evaluate(genotype);
            population.add(new Individual<>(genotype, fitness));
        }

        population.sortByFitness();
        Individual<G> best = population.best();

        this.state = new AlgorithmState<>(
                context.runId(),
                id(),
                0,
                population.size(),
                Instant.now(),
                population,
                best
        );
        this.previousBestFitness = PopulationMetrics.best(population);
        this.stagnationIterations = 0;

        context.eventBus().publish(new RunStartedEvent(
                context.runId(),
                Instant.now(),
                id(),
                context.model().name(),
                context.problem().name(),
                context.rngManager().masterSeed()
        ));

        int eliteCount = Math.max(1, Math.min(selectionSize(context, population), population.size()));
        List<Individual<G>> elite = topIndividuals(population, eliteCount);
        LatentTelemetry telemetry = LatentKnowledgeAnalyzer.analyze(
                population,
                elite,
                context.model(),
                LatentTelemetry.empty(),
                context.parameters()
        );
        this.previousTelemetry = telemetry;

        publishIterationEvent(context, state, telemetry, List.of(), elite.size());
    }

    @Override
    public void iterate(AlgorithmContext<G> context) {
        if (state == null) {
            throw new IllegalStateException("initialize(context) must be called before iterate(context)");
        }

        Population<G> current = state.population();
        updateStagnationCounter(context, current);

        int selectedCount = selectionSize(context, current);
        List<Individual<G>> selected = context.selectionPolicy().select(
                current,
                Math.max(1, Math.min(selectedCount, current.size())),
                context.rngManager().stream("selection")
        );

        context.model().fit(selected, context.representation(), context.rngManager().stream("model-fit"));

        List<Individual<G>> elite = topIndividuals(current, Math.max(1, Math.min(selectedCount, current.size())));
        LatentTelemetry telemetry = LatentKnowledgeAnalyzer.analyze(
                current,
                elite,
                context.model(),
                previousTelemetry,
                context.parameters()
        );

        AdaptivePlan adaptivePlan = buildAdaptivePlan(context, telemetry);
        if (!adaptivePlan.actions().isEmpty()) {
            Instant actionTimestamp = Instant.now();
            int targetIteration = state.iteration() + 1;
            for (AdaptiveActionRecord action : adaptivePlan.actions()) {
                context.eventBus().publish(new AdaptiveActionEvent(
                        state.runId(),
                        actionTimestamp,
                        targetIteration,
                        action
                ));
            }
        }

        List<G> sampled = context.model().sample(
                context.populationSize(),
                context.representation(),
                context.problem(),
                context.constraintHandling(),
                context.rngManager().stream("model-sample")
        );

        sampled = applyAdaptiveSamplingControls(context, sampled, telemetry, adaptivePlan);

        List<Individual<G>> offspring = new ArrayList<>(sampled.size());
        for (G genotype : sampled) {
            G feasible = context.constraintHandling().enforce(
                    genotype,
                    context.representation(),
                    context.problem(),
                    context.rngManager().stream("constraint")
            );
            Fitness fitness = evaluateGenotype(context, feasible, context.rngManager().stream("evaluation"));
            Individual<G> individual = new Individual<>(feasible, fitness);
            offspring.add(context.localSearch().refine(
                    individual,
                    context.problem(),
                    context.representation(),
                    context.rngManager().stream("local-search")
            ));
        }

        Population<G> next = context.replacementPolicy().replace(
                current,
                offspring,
                context.elitism(),
                context.problem().objectiveSense()
        );
        next = context.nichingPolicy().apply(next, context.representation(), context.rngManager().stream("niching"));
        next.sortByFitness();

        if (context.restartPolicy().shouldRestart(state)) {
            next = context.restartPolicy().restart(state, context.representation(), context.rngManager().stream("restart"));
            next.sortByFitness();
        }

        next = postProcessPopulation(context, current, next);
        next.sortByFitness();

        Individual<G> previousBest = state.best();
        Individual<G> newBest = better(context.problem().objectiveSense(), next.best(), previousBest)
                ? next.best()
                : previousBest;

        int newIteration = state.iteration() + 1;
        long newEvaluations = state.evaluations() + sampled.size();

        AlgorithmState<G> newState = new AlgorithmState<>(
                state.runId(),
                id(),
                newIteration,
                newEvaluations,
                state.startedAt(),
                next,
                newBest
        );

        afterIteration(context, current, next);
        this.state = newState;
        this.previousTelemetry = telemetry;

        publishIterationEvent(context, newState, telemetry, adaptivePlan.actions(), elite.size());
    }

    @Override
    public void run(AlgorithmContext<G> context) {
        initialize(context);
        while (!context.stoppingCondition().shouldStop(state)) {
            iterate(context);
        }
        complete(context, Map.of());
    }

    @Override
    public AlgorithmState<G> state() {
        return state;
    }

    @Override
    public RunResult<G> result() {
        return result;
    }

    /**
     * Restores internal state from checkpoint payload.
     */
    public void restoreState(AlgorithmState<G> restoredState) {
        this.state = restoredState;
        this.previousBestFitness = PopulationMetrics.best(restoredState.population());
        this.stagnationIterations = 0;
        this.previousTelemetry = LatentTelemetry.empty();
    }

    /**
     * Finalizes the run and emits completion event with artifact pointers.
     */
    public void complete(AlgorithmContext<G> context, Map<String, String> artifacts) {
        Duration runtime = Duration.between(state.startedAt(), Instant.now());
        this.result = new RunResult<>(
                state.runId(),
                id(),
                context.problem().name(),
                state.best(),
                state.iteration(),
                state.evaluations(),
                runtime,
                artifacts
        );

        context.eventBus().publish(new RunCompletedEvent(
                state.runId(),
                Instant.now(),
                state.iteration(),
                state.evaluations(),
                runtime.toMillis(),
                state.best().fitness().scalar(),
                context.representation().summarize(state.best().genotype()),
                String.valueOf(state.best().genotype()),
                artifacts
        ));
    }

    private void publishIterationEvent(AlgorithmContext<G> context,
                                       AlgorithmState<G> current,
                                       LatentTelemetry telemetry,
                                       List<AdaptiveActionRecord> adaptiveActions,
                                       int eliteSize) {
        Map<String, Double> metrics = new LinkedHashMap<>();
        for (MetricCollector<G> collector : context.metricCollectors()) {
            metrics.putAll(collector.collect(current));
        }
        metrics.putAll(telemetry.flattenedNumeric());
        metrics.put("adaptive_event_count", (double) adaptiveActions.size());

        context.eventBus().publish(new IterationCompletedEvent(
                current.runId(),
                Instant.now(),
                current.iteration(),
                current.evaluations(),
                current.population().size(),
                eliteSize,
                PopulationMetrics.best(current.population()),
                PopulationMetrics.mean(current.population()),
                PopulationMetrics.std(current.population()),
                metrics,
                context.model().diagnostics(),
                telemetry,
                List.copyOf(adaptiveActions)
        ));
    }

    private void updateStagnationCounter(AlgorithmContext<G> context, Population<G> population) {
        double currentBest = PopulationMetrics.best(population);
        if (Double.isNaN(previousBestFitness)) {
            previousBestFitness = currentBest;
            stagnationIterations = 0;
            return;
        }

        double epsilon = Math.max(0.0, Params.dbl(context.parameters(), "adaptiveImprovementEpsilon", 1.0e-12));
        double improvement = normalizedImprovement(context.problem().objectiveSense(), previousBestFitness, currentBest);
        if (improvement > epsilon) {
            stagnationIterations = 0;
        } else {
            stagnationIterations++;
        }
        previousBestFitness = currentBest;
    }

    private AdaptivePlan buildAdaptivePlan(AlgorithmContext<G> context, LatentTelemetry telemetry) {
        if (!Params.bool(context.parameters(), "adaptiveEnabled", false)) {
            return AdaptivePlan.none();
        }

        List<AdaptiveActionRecord> actions = new ArrayList<>();

        String family = telemetry.representationFamily().toLowerCase(Locale.ROOT);
        int iteration = state == null ? 0 : state.iteration();
        int earlyLimit = Math.max(1, Params.integer(context.parameters(), "adaptiveEarlyIterationLimit", 40));
        boolean earlyPhase = iteration < earlyLimit;

        double perturbFraction = 0.0;
        double perturbRate = 0.0;
        double realNoiseScale = 0.0;
        int permutationSwaps = 0;
        double randomReplacementRatio = 0.0;

        if (earlyPhase && isEntropyCollapse(family, telemetry, context.parameters())) {
            perturbFraction = Math.max(perturbFraction,
                    clamp(Params.dbl(context.parameters(), "adaptiveExplorationFraction", 0.35), 0.01, 1.0));
            perturbRate = Math.max(perturbRate,
                    clamp(Params.dbl(context.parameters(), "adaptiveExplorationNoiseRate", 0.08), 0.001, 1.0));
            realNoiseScale = Math.max(realNoiseScale,
                    Math.max(1.0e-8,
                            Params.dbl(context.parameters(), "adaptiveRealNoiseScale",
                                    telemetry.metrics().getOrDefault("real_sigma_mean", 0.05))));
            permutationSwaps = Math.max(permutationSwaps,
                    Math.max(1, Params.integer(context.parameters(), "adaptivePermutationSwaps", 2)));

            Map<String, Object> details = new LinkedHashMap<>();
            details.put("family", family);
            details.put("iteration", iteration);
            details.put("perturbFraction", perturbFraction);
            details.put("perturbRate", perturbRate);
            details.put("realNoiseScale", realNoiseScale);
            details.put("permutationSwaps", permutationSwaps);
            details.put("metrics", telemetry.metrics());

            actions.add(new AdaptiveActionRecord(
                    "entropy_collapse",
                    "exploration_boost",
                    "Entropy/fixation signal indicates premature convergence.",
                    details
            ));
        }

        int stagnationLimit = Math.max(1, Params.integer(context.parameters(), "adaptiveStagnationGenerations", 10));
        if (stagnationIterations >= stagnationLimit
                && isLowDiversity(family, telemetry, context.parameters())) {
            randomReplacementRatio = Math.max(randomReplacementRatio,
                    clamp(Params.dbl(context.parameters(), "adaptivePartialRestartFraction", 0.25), 0.01, 1.0));

            Map<String, Object> details = new LinkedHashMap<>();
            details.put("family", family);
            details.put("stagnationIterations", stagnationIterations);
            details.put("stagnationLimit", stagnationLimit);
            details.put("diversity", telemetry.diversity());
            details.put("randomReplacementRatio", randomReplacementRatio);

            actions.add(new AdaptiveActionRecord(
                    "stagnation_low_diversity",
                    "partial_restart",
                    "Best fitness stagnated while diversity stayed below threshold.",
                    details
            ));
            stagnationIterations = 0;
        }

        return new AdaptivePlan(
                List.copyOf(actions),
                randomReplacementRatio,
                perturbFraction,
                perturbRate,
                realNoiseScale,
                permutationSwaps
        );
    }

    private boolean isEntropyCollapse(String family, LatentTelemetry telemetry, Map<String, Object> params) {
        return switch (family) {
            case "binary" -> {
                double entropy = telemetry.metrics().getOrDefault("binary_mean_entropy", 1.0);
                double fixation = telemetry.metrics().getOrDefault("binary_fixation_ratio", 0.0);
                double entropyDrop = telemetry.drift().getOrDefault("binary_entropy_delta", 0.0);
                double entropyThreshold = clamp(Params.dbl(params, "adaptiveBinaryEntropyThreshold", 0.28), 0.0, 1.0);
                double fixationThreshold = clamp(Params.dbl(params, "adaptiveBinaryFixationThreshold", 0.6), 0.0, 1.0);
                double dropThreshold = Math.max(0.0, Params.dbl(params, "adaptiveBinaryEntropyDropThreshold", 0.1));
                yield entropy < entropyThreshold || fixation > fixationThreshold || entropyDrop > dropThreshold;
            }
            case "permutation" -> {
                double entropy = telemetry.metrics().getOrDefault("perm_position_entropy_mean", 10.0);
                double threshold = Math.max(0.0, Params.dbl(params, "adaptivePermutationEntropyThreshold", 0.9));
                yield entropy < threshold;
            }
            case "real" -> {
                double sigmaMean = telemetry.metrics().getOrDefault("real_sigma_mean", 1.0);
                double threshold = Math.max(1.0e-12, Params.dbl(params, "adaptiveRealSigmaThreshold", 0.04));
                yield sigmaMean < threshold;
            }
            default -> false;
        };
    }

    private boolean isLowDiversity(String family, LatentTelemetry telemetry, Map<String, Object> params) {
        return switch (family) {
            case "binary" -> telemetry.diversity().getOrDefault("hamming_elite", 1.0)
                    < Math.max(0.0, Params.dbl(params, "adaptiveBinaryDiversityThreshold", 0.08));
            case "permutation" -> telemetry.diversity().getOrDefault("kendall_elite", 1.0)
                    < Math.max(0.0, Params.dbl(params, "adaptivePermutationDiversityThreshold", 0.18));
            case "real" -> telemetry.diversity().getOrDefault("euclidean_elite", 1.0)
                    < Math.max(0.0, Params.dbl(params, "adaptiveRealDiversityThreshold", 0.12));
            default -> false;
        };
    }

    private List<G> applyAdaptiveSamplingControls(AlgorithmContext<G> context,
                                                  List<G> sampled,
                                                  LatentTelemetry telemetry,
                                                  AdaptivePlan plan) {
        if (sampled.isEmpty() || plan.isNone()) {
            return sampled;
        }

        List<G> adapted = new ArrayList<>(sampled);
        RngStream restartRng = context.rngManager().stream("adaptive-restart");
        RngStream noiseRng = context.rngManager().stream("adaptive-noise");

        if (plan.randomReplacementRatio() > 0.0) {
            int replaceCount = Math.max(1, (int) Math.round(adapted.size() * plan.randomReplacementRatio()));
            for (int i = 0; i < replaceCount; i++) {
                int index = adapted.size() - 1 - i;
                if (index < 0) {
                    break;
                }
                adapted.set(index, context.representation().random(restartRng));
            }
        }

        if (plan.perturbFraction() > 0.0 && plan.perturbRate() > 0.0) {
            int affected = Math.max(1, (int) Math.round(adapted.size() * plan.perturbFraction()));
            for (int i = 0; i < affected; i++) {
                int index = noiseRng.nextInt(adapted.size());
                adapted.set(index, perturbGenotype(
                        adapted.get(index),
                        plan.perturbRate(),
                        plan.realNoiseScale(),
                        plan.permutationSwaps(),
                        noiseRng,
                        telemetry.representationFamily()
                ));
            }
        }

        return adapted;
    }

    @SuppressWarnings("unchecked")
    private G perturbGenotype(G genotype,
                              double perturbRate,
                              double realNoiseScale,
                              int permutationSwaps,
                              RngStream rng,
                              String representationFamily) {
        if (genotype == null) {
            return null;
        }

        if ("binary".equalsIgnoreCase(representationFamily)) {
            boolean[] genes = extractBooleanArray(genotype, "genes");
            if (genes == null) {
                return genotype;
            }
            for (int i = 0; i < genes.length; i++) {
                if (rng.nextDouble() < perturbRate) {
                    genes[i] = !genes[i];
                }
            }
            return (G) newInstanceWithArray(genotype, boolean[].class, genes);
        }

        if ("real".equalsIgnoreCase(representationFamily)) {
            double[] values = extractDoubleArray(genotype, "values");
            if (values == null) {
                return genotype;
            }
            for (int i = 0; i < values.length; i++) {
                if (rng.nextDouble() < perturbRate) {
                    values[i] += rng.nextGaussian() * Math.max(1.0e-12, realNoiseScale);
                }
            }
            return (G) newInstanceWithArray(genotype, double[].class, values);
        }

        if ("permutation".equalsIgnoreCase(representationFamily)) {
            int[] order = extractIntArray(genotype, "order");
            if (order == null || order.length <= 1) {
                return genotype;
            }
            int swaps = Math.max(1, permutationSwaps);
            for (int s = 0; s < swaps; s++) {
                int left = rng.nextInt(order.length);
                int right = rng.nextInt(order.length);
                int tmp = order[left];
                order[left] = order[right];
                order[right] = tmp;
            }
            return (G) newInstanceWithArray(genotype, int[].class, order);
        }

        return genotype;
    }

    private Object newInstanceWithArray(Object genotype, Class<?> parameterType, Object value) {
        try {
            return genotype.getClass().getDeclaredConstructor(parameterType).newInstance(value);
        } catch (Exception ignored) {
            return genotype;
        }
    }

    private boolean[] extractBooleanArray(Object genotype, String methodName) {
        Object raw = invokeNoArg(genotype, methodName);
        if (!(raw instanceof boolean[] values)) {
            return null;
        }
        return Arrays.copyOf(values, values.length);
    }

    private int[] extractIntArray(Object genotype, String methodName) {
        Object raw = invokeNoArg(genotype, methodName);
        if (!(raw instanceof int[] values)) {
            return null;
        }
        return Arrays.copyOf(values, values.length);
    }

    private double[] extractDoubleArray(Object genotype, String methodName) {
        Object raw = invokeNoArg(genotype, methodName);
        if (!(raw instanceof double[] values)) {
            return null;
        }
        return Arrays.copyOf(values, values.length);
    }

    private Object invokeNoArg(Object source, String methodName) {
        try {
            Method method = source.getClass().getMethod(methodName);
            return method.invoke(source);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<Individual<G>> topIndividuals(Population<G> population, int count) {
        List<Individual<G>> ranked = new ArrayList<>(population.asList());
        ranked.sort((left, right) -> {
            double diff = left.fitness().scalar() - right.fitness().scalar();
            if (population.objectiveSense() == ObjectiveSense.MAXIMIZE) {
                diff = -diff;
            }
            return diff < 0 ? -1 : (diff > 0 ? 1 : 0);
        });
        return ranked.subList(0, Math.min(count, ranked.size()));
    }

    private double normalizedImprovement(ObjectiveSense sense, double previousBest, double currentBest) {
        double scale = Math.max(1.0e-12, Math.abs(previousBest));
        if (sense == ObjectiveSense.MINIMIZE) {
            return (previousBest - currentBest) / scale;
        }
        return (currentBest - previousBest) / scale;
    }

    private boolean better(ObjectiveSense sense, Individual<G> left, Individual<G> right) {
        if (right == null) {
            return true;
        }
        return sense == ObjectiveSense.MINIMIZE
                ? left.fitness().scalar() < right.fitness().scalar()
                : left.fitness().scalar() > right.fitness().scalar();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Immutable adaptive control plan derived from latent telemetry.
     */
    private record AdaptivePlan(List<AdaptiveActionRecord> actions,
                                double randomReplacementRatio,
                                double perturbFraction,
                                double perturbRate,
                                double realNoiseScale,
                                int permutationSwaps) {

        private static AdaptivePlan none() {
            return new AdaptivePlan(List.of(), 0.0, 0.0, 0.0, 0.0, 0);
        }

        private boolean isNone() {
            return actions.isEmpty()
                    && randomReplacementRatio <= 0.0
                    && perturbFraction <= 0.0
                    && perturbRate <= 0.0;
        }
    }
}
