package com.knezevic.edaf.v3.core.api;

import com.knezevic.edaf.v3.core.events.IterationCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunStartedEvent;
import com.knezevic.edaf.v3.core.metrics.PopulationMetrics;
import com.knezevic.edaf.v3.core.rng.RngStream;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Template-method base implementation for model-based algorithms.
 */
public abstract class AbstractEdaAlgorithm<G> implements Algorithm<G> {

    private AlgorithmState<G> state;
    private RunResult<G> result;

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

        context.eventBus().publish(new RunStartedEvent(
                context.runId(),
                Instant.now(),
                id(),
                context.model().name(),
                context.problem().name(),
                context.rngManager().masterSeed()
        ));

        publishIterationEvent(context, state);
    }

    @Override
    public void iterate(AlgorithmContext<G> context) {
        if (state == null) {
            throw new IllegalStateException("initialize(context) must be called before iterate(context)");
        }

        Population<G> current = state.population();
        int selectedCount = selectionSize(context, current);
        List<Individual<G>> selected = context.selectionPolicy().select(
                current,
                Math.max(1, Math.min(selectedCount, current.size())),
                context.rngManager().stream("selection")
        );

        context.model().fit(selected, context.representation(), context.rngManager().stream("model-fit"));

        List<G> sampled = context.model().sample(
                context.populationSize(),
                context.representation(),
                context.problem(),
                context.constraintHandling(),
                context.rngManager().stream("model-sample")
        );

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
        publishIterationEvent(context, newState);
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
                artifacts
        ));
    }

    private void publishIterationEvent(AlgorithmContext<G> context, AlgorithmState<G> current) {
        Map<String, Double> metrics = new LinkedHashMap<>();
        for (MetricCollector<G> collector : context.metricCollectors()) {
            metrics.putAll(collector.collect(current));
        }

        context.eventBus().publish(new IterationCompletedEvent(
                current.runId(),
                Instant.now(),
                current.iteration(),
                current.evaluations(),
                PopulationMetrics.best(current.population()),
                PopulationMetrics.mean(current.population()),
                PopulationMetrics.std(current.population()),
                metrics,
                context.model().diagnostics()
        ));
    }

    private boolean better(ObjectiveSense sense, Individual<G> left, Individual<G> right) {
        if (right == null) {
            return true;
        }
        return sense == ObjectiveSense.MINIMIZE
                ? left.fitness().scalar() < right.fitness().scalar()
                : left.fitness().scalar() > right.fitness().scalar();
    }
}
