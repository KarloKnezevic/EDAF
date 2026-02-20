package com.knezevic.edaf.v3.persistence.query;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Read-side repository for run metadata, filtered queries, and run detail resources.
 */
public interface RunRepository {

    /**
     * Returns one page of experiments matching provided query.
     */
    PageResult<ExperimentListItem> listExperiments(ExperimentQuery query);

    /**
     * Returns one page of runs matching provided query.
     */
    PageResult<RunListItem> listRuns(RunQuery query);

    /**
     * Returns rich run detail record or {@code null} when run does not exist.
     */
    RunDetail getRunDetail(String runId);

    /**
     * Lists iteration metrics for one run.
     */
    List<IterationMetric> listIterations(String runId);

    /**
     * Lists checkpoints for one run.
     */
    List<CheckpointRow> listCheckpoints(String runId);

    /**
     * Lists events for one run with optional filtering and paging.
     */
    PageResult<EventRow> listEvents(String runId, String eventType, String q, int page, int size);

    /**
     * Lists flattened experiment parameters for run's experiment.
     */
    List<ExperimentParamRow> listExperimentParams(String runId);

    /**
     * Lists distinct facet values used by dashboard filters.
     */
    FilterFacets listFacets();

    /**
     * Returns one experiment metadata row or {@code null} when id does not exist.
     */
    ExperimentDetail getExperimentDetail(String experimentId);

    /**
     * Lists runs for one experiment with pagination and sortable columns.
     */
    PageResult<ExperimentRunItem> listExperimentRuns(String experimentId, int page, int size, String sortBy, String sortDir);

    /**
     * Computes experiment-level aggregate analytics and profile curves.
     */
    ExperimentAnalytics analyzeExperiment(String experimentId, String objectiveDirection, Double targetFitness);

    /**
     * Computes cross-algorithm comparison for one problem family.
     */
    ProblemComparisonReport compareAlgorithmsOnProblem(String problemType,
                                                       String objectiveDirection,
                                                       Double targetFitness,
                                                       List<String> algorithms);

    /**
     * Legacy convenience listing used by reporting and older callers.
     */
    default List<RunSummary> listRuns(int limit) {
        RunQuery query = new RunQuery(null, null, null, null, null, null, null, null, null, 0, limit, "start_time", "desc");
        return listRuns(query).items().stream()
                .map(item -> new RunSummary(
                        item.runId(),
                        item.algorithmType(),
                        item.modelType(),
                        item.problemType(),
                        item.startTime(),
                        item.endTime(),
                        item.bestFitness(),
                        item.runtimeMillis(),
                        item.status()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Legacy convenience run getter used by report generation.
     */
    default RunSummary getRun(String runId) {
        RunDetail detail = getRunDetail(runId);
        if (detail == null) {
            return null;
        }
        return new RunSummary(
                detail.runId(),
                detail.algorithmType(),
                detail.modelType(),
                detail.problemType(),
                detail.startTime(),
                detail.endTime(),
                detail.bestFitness(),
                detail.runtimeMillis(),
                detail.status()
        );
    }
}
