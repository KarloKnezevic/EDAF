package com.knezevic.edaf.persistence.api;

import com.knezevic.edaf.core.runtime.AlgorithmStarted;
import com.knezevic.edaf.core.runtime.AlgorithmTerminated;
import com.knezevic.edaf.core.runtime.EvaluationCompleted;
import com.knezevic.edaf.core.runtime.EventPublisher;
import com.knezevic.edaf.core.runtime.GenerationCompleted;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Bridges the core {@link EventPublisher} to {@link ResultSink} lifecycle calls.
 * <p>
 * Translates algorithm events into persistence sink method invocations:
 * <ul>
 *     <li>{@link AlgorithmStarted} -> {@link ResultSink#onRunStarted}</li>
 *     <li>{@link GenerationCompleted} -> {@link ResultSink#onGenerationCompleted}</li>
 *     <li>{@link AlgorithmTerminated} -> {@link ResultSink#onRunCompleted}</li>
 * </ul>
 * </p>
 */
public class PersistenceEventPublisher implements EventPublisher {

    private final List<ResultSink> sinks;
    private String runId;
    private long startTimeMillis;
    private long lastEvalDurationNanos;
    private double bestFitness = Double.NaN;
    private String bestIndividualJson;

    public PersistenceEventPublisher(List<ResultSink> sinks) {
        this.sinks = new CopyOnWriteArrayList<>(sinks);
    }

    public PersistenceEventPublisher(ResultSink sink) {
        this(List.of(sink));
    }

    @Override
    public void publish(Object event) {
        switch (event) {
            case AlgorithmStarted started -> handleStarted(started);
            case EvaluationCompleted eval -> handleEvaluation(eval);
            case GenerationCompleted gen -> handleGeneration(gen);
            case AlgorithmTerminated term -> handleTerminated(term);
            default -> { /* ignore unknown events */ }
        }
    }

    private void handleStarted(AlgorithmStarted event) {
        this.runId = UUID.randomUUID().toString();
        this.startTimeMillis = System.currentTimeMillis();
        this.bestFitness = Double.NaN;
        this.bestIndividualJson = null;

        RunMetadata metadata = new RunMetadata(
            runId,
            event.getAlgorithmId(),
            "",   // problem class not available from event — populated by caller if needed
            "",   // genotype type not available from event
            0,    // population size not available from event
            0,    // max generations not available from event
            null, // config hash
            null, // seed
            Instant.now()
        );

        for (ResultSink sink : sinks) {
            sink.onRunStarted(metadata);
        }
    }

    private void handleEvaluation(EvaluationCompleted event) {
        this.lastEvalDurationNanos = event.getDurationNanos();
    }

    private void handleGeneration(GenerationCompleted event) {
        if (runId == null) return;

        String individualJson = event.getBestIndividual() != null
            ? event.getBestIndividual().toString()
            : null;

        // Track overall best
        if (Double.isNaN(bestFitness) || event.hasStatistics() && event.getBestFitness() < bestFitness) {
            bestFitness = event.getBestFitness();
            bestIndividualJson = individualJson;
        }

        GenerationRecord record = new GenerationRecord(
            event.getGeneration(),
            event.getBestFitness(),
            event.getWorstFitness(),
            event.getAvgFitness(),
            event.getStdFitness(),
            individualJson,
            lastEvalDurationNanos,
            Instant.now()
        );

        for (ResultSink sink : sinks) {
            sink.onGenerationCompleted(runId, record);
        }
    }

    private void handleTerminated(AlgorithmTerminated event) {
        if (runId == null) return;

        long durationMillis = System.currentTimeMillis() - startTimeMillis;
        RunResult result = new RunResult(
            event.getGeneration(),
            bestFitness,
            bestIndividualJson,
            durationMillis,
            Instant.now()
        );

        for (ResultSink sink : sinks) {
            sink.onRunCompleted(runId, result);
        }
    }

    public String getRunId() {
        return runId;
    }
}
