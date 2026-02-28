/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.experiments.runner;

import com.knezevic.edaf.v3.core.config.BatchConfig;
import com.knezevic.edaf.v3.core.config.ConfigLoader;
import com.knezevic.edaf.v3.core.config.ExperimentConfig;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.core.runtime.ExecutionParallelism;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executes batch configurations with bounded multicore parallelism.
 *
 * <p>Run plans are expanded deterministically (stable ordering, run IDs, seeds),
 * then executed sequentially or in parallel depending on configured/derived
 * runtime parallelism and sink thread-safety constraints.</p>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class BatchRunner {

    private final ConfigLoader configLoader;

    /**
     * Creates batch runner with default config loader.
     */
    public BatchRunner() {
        this.configLoader = new ConfigLoader();
    }

    /**
     * Executes all runs defined in one batch file.
     *
     * @param batchConfigPath batch YAML path
     * @param additionalSinks externally provided run sinks
     * @return ordered run execution results
     */
    public List<RunExecution> runBatch(Path batchConfigPath, List<EventSink> additionalSinks) {
        BatchConfig batch = configLoader.loadBatch(batchConfigPath);
        List<RunPlan> plans = expandPlans(batchConfigPath, batch);
        if (plans.isEmpty()) {
            return List.of();
        }

        int parallelism = resolveParallelism(plans.size(), additionalSinks);
        if (parallelism <= 1) {
            return executeSequential(plans, additionalSinks);
        }

        ExecutorService executor = Executors.newFixedThreadPool(parallelism, batchWorkerFactory());
        try {
            List<Future<IndexedExecution>> futures = new ArrayList<>(plans.size());
            for (RunPlan plan : plans) {
                futures.add(executor.submit(() -> {
                    ExperimentRunner runner = new ExperimentRunner();
                    return new IndexedExecution(plan.index(), runner.run(plan.config(), additionalSinks));
                }));
            }

            List<IndexedExecution> completed = new ArrayList<>(plans.size());
            for (Future<IndexedExecution> future : futures) {
                completed.add(future.get());
            }
            completed.sort(Comparator.comparingInt(IndexedExecution::index));

            List<RunExecution> results = new ArrayList<>(completed.size());
            for (IndexedExecution execution : completed) {
                results.add(execution.execution());
            }
            return results;
        } catch (Exception e) {
            throw new RuntimeException("Failed executing batch runs in parallel", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private List<RunPlan> expandPlans(Path batchConfigPath, BatchConfig batch) {
        List<RunPlan> plans = new ArrayList<>();
        int index = 0;
        for (BatchConfig.BatchExperimentEntry experiment : batch.getExperiments()) {
            Path configPath = batchConfigPath.getParent() == null
                    ? Path.of(experiment.getConfig())
                    : batchConfigPath.getParent().resolve(experiment.getConfig()).normalize();

            int repetitions = Math.max(1, experiment.getRepetitions());
            for (int repetition = 0; repetition < repetitions; repetition++) {
                var loaded = configLoader.load(configPath);
                var config = loaded.config();

                if (repetitions > 1 || hasText(experiment.getRunIdPrefix())) {
                    String baseId = hasText(experiment.getRunIdPrefix())
                            ? experiment.getRunIdPrefix().trim()
                            : config.getRun().getId();
                    config.getRun().setId(baseId + "-r" + String.format(Locale.ROOT, "%02d", repetition + 1));
                }

                long baseSeed = experiment.getSeedStart() != null
                        ? experiment.getSeedStart()
                        : config.getRun().getMasterSeed();
                config.getRun().setMasterSeed(baseSeed + repetition);
                assignRunSpecificFileLog(config, index);

                plans.add(new RunPlan(index++, config));
            }
        }
        return plans;
    }

    private static int resolveParallelism(int plannedRuns, List<EventSink> additionalSinks) {
        if (plannedRuns <= 1) {
            return 1;
        }
        if (additionalSinks != null && !additionalSinks.isEmpty()) {
            // Additional sink instances may carry mutable state and are not guaranteed to be thread-safe.
            return 1;
        }
        return Math.max(1, Math.min(plannedRuns, ExecutionParallelism.suggestedRunParallelism()));
    }

    private static List<RunExecution> executeSequential(List<RunPlan> plans, List<EventSink> additionalSinks) {
        List<RunExecution> results = new ArrayList<>(plans.size());
        for (RunPlan plan : plans) {
            ExperimentRunner runner = new ExperimentRunner();
            results.add(runner.run(plan.config(), additionalSinks));
        }
        return results;
    }

    private static ThreadFactory batchWorkerFactory() {
        AtomicInteger counter = new AtomicInteger(1);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("edaf-batch-worker-" + counter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void assignRunSpecificFileLog(ExperimentConfig config, int planIndex) {
        if (config == null || config.getLogging() == null || config.getRun() == null) {
            return;
        }
        List<String> modes = config.getLogging().getModes();
        if (modes == null || modes.stream().noneMatch(mode -> "file".equalsIgnoreCase(mode))) {
            return;
        }
        if (!hasText(config.getLogging().getLogFile()) || !hasText(config.getRun().getId())) {
            return;
        }

        Path current = Path.of(config.getLogging().getLogFile());
        String fileName = current.getFileName() == null ? "edaf-v3.log" : current.getFileName().toString();
        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;
        String ext = dot > 0 ? fileName.substring(dot) : "";
        String runScopedName = base + "-" + config.getRun().getId() + "-b"
                + String.format(Locale.ROOT, "%04d", planIndex + 1) + ext;
        Path runScopedPath = current.getParent() == null
                ? Path.of(runScopedName)
                : current.getParent().resolve(runScopedName).normalize();
        config.getLogging().setLogFile(runScopedPath.toString());
    }

    private record RunPlan(int index, ExperimentConfig config) {
    }

    private record IndexedExecution(int index, RunExecution execution) {
    }
}
