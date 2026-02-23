package com.knezevic.edaf.v3.web.controller;

import com.knezevic.edaf.v3.persistence.query.CheckpointRow;
import com.knezevic.edaf.v3.persistence.query.ExperimentAnalytics;
import com.knezevic.edaf.v3.persistence.query.ExperimentDeletionResult;
import com.knezevic.edaf.v3.persistence.query.ExperimentDetail;
import com.knezevic.edaf.v3.persistence.query.ExperimentListItem;
import com.knezevic.edaf.v3.persistence.query.ExperimentQuery;
import com.knezevic.edaf.v3.persistence.query.EventRow;
import com.knezevic.edaf.v3.persistence.query.ExperimentParamRow;
import com.knezevic.edaf.v3.persistence.query.ExperimentRunItem;
import com.knezevic.edaf.v3.persistence.query.FilterFacets;
import com.knezevic.edaf.v3.persistence.query.IterationMetric;
import com.knezevic.edaf.v3.persistence.query.PageResult;
import com.knezevic.edaf.v3.persistence.query.ProblemComparisonReport;
import com.knezevic.edaf.v3.persistence.query.RunDetail;
import com.knezevic.edaf.v3.persistence.query.RunListItem;
import com.knezevic.edaf.v3.persistence.query.RunQuery;
import com.knezevic.edaf.v3.persistence.query.RunRepository;
import com.knezevic.edaf.v3.persistence.query.StopRequestResult;
import com.knezevic.edaf.v3.persistence.query.coco.CocoAggregateMetric;
import com.knezevic.edaf.v3.persistence.query.coco.CocoCampaignDetail;
import com.knezevic.edaf.v3.persistence.query.coco.CocoCampaignListItem;
import com.knezevic.edaf.v3.persistence.query.coco.CocoCampaignQuery;
import com.knezevic.edaf.v3.persistence.query.coco.CocoOptimizerConfigRow;
import com.knezevic.edaf.v3.persistence.query.coco.CocoRepository;
import com.knezevic.edaf.v3.persistence.query.coco.CocoTrialMetric;
import com.knezevic.edaf.v3.web.service.GrammarTreeViewService;
import com.knezevic.edaf.v3.web.service.RunArtifactService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * REST API for polling dashboard updates.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final RunRepository runRepository;
    private final CocoRepository cocoRepository;
    private final RunArtifactService runArtifactService;
    private final GrammarTreeViewService grammarTreeViewService;

    public ApiController(RunRepository runRepository,
                         CocoRepository cocoRepository,
                         RunArtifactService runArtifactService,
                         GrammarTreeViewService grammarTreeViewService) {
        this.runRepository = runRepository;
        this.cocoRepository = cocoRepository;
        this.runArtifactService = runArtifactService;
        this.grammarTreeViewService = grammarTreeViewService;
    }

    @GetMapping("/experiments")
    public PageResult<ExperimentListItem> listExperiments(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String algorithm,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String problem,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "latest_run_time") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return runRepository.listExperiments(new ExperimentQuery(
                q, algorithm, model, problem, status, from, to, page, size, sortBy, sortDir
        ));
    }

    @GetMapping("/runs")
    public PageResult<RunListItem> listRuns(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String algorithm,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String problem,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) Double minBest,
            @RequestParam(required = false) Double maxBest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "start_time") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return runRepository.listRuns(new RunQuery(
                q, algorithm, model, problem, status, from, to, minBest, maxBest, page, size, sortBy, sortDir
        ));
    }

    @GetMapping("/runs/{runId}")
    public RunDetail getRun(@PathVariable String runId) {
        RunDetail detail = runRepository.getRunDetail(runId);
        if (detail == null) {
            detail = runArtifactService.loadRunDetail(runId).orElse(null);
        }
        if (detail == null) {
            throw new ResponseStatusException(NOT_FOUND, "Run not found: " + runId);
        }
        return detail;
    }

    @GetMapping("/runs/{runId}/tree")
    public GrammarTreeViewService.GrammarTreeView getRunTree(@PathVariable String runId) {
        RunDetail detail = runRepository.getRunDetail(runId);
        if (detail == null) {
            detail = runArtifactService.loadRunDetail(runId).orElse(null);
        }
        if (detail == null) {
            throw new ResponseStatusException(NOT_FOUND, "Run not found: " + runId);
        }
        return grammarTreeViewService.view(detail)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "No grammar tree visualization payload available for run: " + runId
                ));
    }

    @GetMapping("/runs/{runId}/iterations")
    public List<IterationMetric> listIterations(@PathVariable String runId) {
        List<IterationMetric> rows = runRepository.listIterations(runId);
        if (!rows.isEmpty()) {
            return rows;
        }
        return runArtifactService.loadIterations(runId);
    }

    @GetMapping("/runs/{runId}/events")
    public PageResult<EventRow> listEvents(
            @PathVariable String runId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        PageResult<EventRow> rows = runRepository.listEvents(runId, eventType, q, page, size);
        if (rows.total() > 0) {
            return rows;
        }
        return runArtifactService.loadEvents(runId, eventType, q, page, size);
    }

    @GetMapping("/runs/{runId}/checkpoints")
    public List<CheckpointRow> listCheckpoints(@PathVariable String runId) {
        List<CheckpointRow> rows = runRepository.listCheckpoints(runId);
        if (!rows.isEmpty()) {
            return rows;
        }
        return runArtifactService.loadCheckpoints(runId);
    }

    @GetMapping("/runs/{runId}/params")
    public List<ExperimentParamRow> listParams(@PathVariable String runId) {
        List<ExperimentParamRow> rows = runRepository.listExperimentParams(runId);
        if (!rows.isEmpty()) {
            return rows;
        }
        return runArtifactService.loadParams(runId);
    }

    @GetMapping("/facets")
    public FilterFacets facets() {
        return runRepository.listFacets();
    }

    @GetMapping("/experiments/{experimentId}")
    public ExperimentDetail getExperiment(@PathVariable String experimentId) {
        ExperimentDetail detail = runRepository.getExperimentDetail(experimentId);
        if (detail == null) {
            throw new ResponseStatusException(NOT_FOUND, "Experiment not found: " + experimentId);
        }
        return detail;
    }

    @DeleteMapping("/experiments/{experimentId}")
    public DeleteExperimentResponse deleteExperiment(@PathVariable String experimentId) {
        ExperimentDetail detail = runRepository.getExperimentDetail(experimentId);
        if (detail == null) {
            throw new ResponseStatusException(NOT_FOUND, "Experiment not found: " + experimentId);
        }
        if (detail.runningRuns() > 0) {
            throw new ResponseStatusException(CONFLICT,
                    "Experiment has RUNNING runs. Stop/resume completion before deletion: " + experimentId);
        }

        List<String> runIds = runRepository.listRunIdsForExperiment(experimentId);
        ExperimentDeletionResult deleted = runRepository.deleteExperiment(experimentId);
        if (!deleted.deleted()) {
            throw new ResponseStatusException(NOT_FOUND, "Experiment not found: " + experimentId);
        }
        int artifactDirectoriesDeleted = runArtifactService.deleteRunArtifacts(runIds);
        return new DeleteExperimentResponse(
                deleted.experimentId(),
                deleted.deleted(),
                deleted.runsDeleted(),
                deleted.runObjectivesDeleted(),
                deleted.iterationsDeleted(),
                deleted.checkpointsDeleted(),
                deleted.eventsDeleted(),
                deleted.paramsDeleted(),
                artifactDirectoriesDeleted
        );
    }

    @PostMapping("/experiments/delete-bulk")
    public BulkDeleteResponse deleteExperimentsBulk(@RequestBody BulkDeleteRequest request) {
        if (request == null || request.experimentIds() == null || request.experimentIds().isEmpty()) {
            return new BulkDeleteResponse(List.of(), 0, 0, 0);
        }

        List<BulkDeleteItemResult> results = request.experimentIds().stream()
                .map(experimentId -> {
                    try {
                        ExperimentDetail detail = runRepository.getExperimentDetail(experimentId);
                        if (detail == null) {
                            return new BulkDeleteItemResult(experimentId, false, "NOT_FOUND");
                        }
                        if (detail.runningRuns() > 0) {
                            return new BulkDeleteItemResult(experimentId, false, "RUNNING");
                        }
                        List<String> runIds = runRepository.listRunIdsForExperiment(experimentId);
                        ExperimentDeletionResult deleted = runRepository.deleteExperiment(experimentId);
                        if (!deleted.deleted()) {
                            return new BulkDeleteItemResult(experimentId, false, "NOT_FOUND");
                        }
                        runArtifactService.deleteRunArtifacts(runIds);
                        return new BulkDeleteItemResult(experimentId, true, "DELETED");
                    } catch (Exception e) {
                        return new BulkDeleteItemResult(experimentId, false, "ERROR");
                    }
                })
                .toList();

        int deletedCount = (int) results.stream().filter(BulkDeleteItemResult::deleted).count();
        int blockedCount = (int) results.stream().filter(r -> !r.deleted() && "RUNNING".equals(r.reason())).count();
        int missingCount = (int) results.stream().filter(r -> !r.deleted() && "NOT_FOUND".equals(r.reason())).count();
        return new BulkDeleteResponse(results, deletedCount, blockedCount, missingCount);
    }

    @PostMapping("/runs/{runId}/stop")
    public StopResponse stopRun(@PathVariable String runId,
                                @RequestBody(required = false) StopRequestBody body) {
        String reason = body == null ? null : body.reason();
        StopRequestResult result = runRepository.requestRunStop(runId, "web-ui", reason);
        if (!result.found()) {
            throw new ResponseStatusException(NOT_FOUND, result.message());
        }
        if (!result.accepted()) {
            throw new ResponseStatusException(CONFLICT, result.message());
        }
        return new StopResponse(result.scope(), result.targetId(), result.accepted(), result.affectedRuns(), result.message());
    }

    @PostMapping("/experiments/{experimentId}/stop")
    public StopResponse stopExperiment(@PathVariable String experimentId,
                                       @RequestBody(required = false) StopRequestBody body) {
        String reason = body == null ? null : body.reason();
        StopRequestResult result = runRepository.requestExperimentStop(experimentId, "web-ui", reason);
        if (!result.found()) {
            throw new ResponseStatusException(NOT_FOUND, result.message());
        }
        if (!result.accepted()) {
            throw new ResponseStatusException(CONFLICT, result.message());
        }
        return new StopResponse(result.scope(), result.targetId(), result.accepted(), result.affectedRuns(), result.message());
    }

    @GetMapping("/experiments/{experimentId}/runs")
    public PageResult<ExperimentRunItem> listExperimentRuns(
            @PathVariable String experimentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "start_time") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return runRepository.listExperimentRuns(experimentId, page, size, sortBy, sortDir);
    }

    @GetMapping("/experiments/{experimentId}/analysis")
    public ExperimentAnalytics experimentAnalytics(
            @PathVariable String experimentId,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) Double target
    ) {
        return runRepository.analyzeExperiment(experimentId, direction, target);
    }

    @GetMapping(value = "/experiments/{experimentId}/latex", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> experimentLatex(
            @PathVariable String experimentId,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) Double target
    ) {
        ExperimentDetail detail = runRepository.getExperimentDetail(experimentId);
        if (detail == null) {
            throw new ResponseStatusException(NOT_FOUND, "Experiment not found: " + experimentId);
        }
        ExperimentAnalytics analytics = runRepository.analyzeExperiment(experimentId, direction, target);
        String latex = toExperimentLatex(detail, analytics);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"experiment-" + experimentId + ".tex\"")
                .body(latex);
    }

    @GetMapping("/analysis/problem/{problemType}")
    public ProblemComparisonReport compareProblemAlgorithms(
            @PathVariable String problemType,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) Double target,
            @RequestParam(name = "algorithm", required = false) List<String> algorithms
    ) {
        return runRepository.compareAlgorithmsOnProblem(problemType, direction, target, algorithms);
    }

    @GetMapping(value = "/analysis/problem/{problemType}/latex", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> compareProblemAlgorithmsLatex(
            @PathVariable String problemType,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) Double target,
            @RequestParam(name = "algorithm", required = false) List<String> algorithms
    ) {
        ProblemComparisonReport report = runRepository.compareAlgorithmsOnProblem(problemType, direction, target, algorithms);
        String latex = toProblemComparisonLatex(report);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"problem-" + problemType + "-comparison.tex\"")
                .body(latex);
    }

    @GetMapping("/coco/campaigns")
    public PageResult<CocoCampaignListItem> listCocoCampaigns(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String suite,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return cocoRepository.listCampaigns(new CocoCampaignQuery(q, status, suite, page, size, sortBy, sortDir));
    }

    @GetMapping("/coco/campaigns/{campaignId}")
    public CocoCampaignDetail getCocoCampaign(@PathVariable String campaignId) {
        CocoCampaignDetail detail = cocoRepository.getCampaign(campaignId);
        if (detail == null) {
            throw new ResponseStatusException(NOT_FOUND, "COCO campaign not found: " + campaignId);
        }
        return detail;
    }

    @GetMapping("/coco/campaigns/{campaignId}/optimizers")
    public List<CocoOptimizerConfigRow> listCocoOptimizers(@PathVariable String campaignId) {
        return cocoRepository.listOptimizers(campaignId);
    }

    @GetMapping("/coco/campaigns/{campaignId}/aggregates")
    public List<CocoAggregateMetric> listCocoAggregates(@PathVariable String campaignId) {
        return cocoRepository.listAggregates(campaignId);
    }

    @GetMapping("/coco/campaigns/{campaignId}/trials")
    public PageResult<CocoTrialMetric> listCocoTrials(
            @PathVariable String campaignId,
            @RequestParam(required = false) String optimizer,
            @RequestParam(required = false) Integer functionId,
            @RequestParam(required = false) Integer dimension,
            @RequestParam(required = false) Boolean reachedTarget,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return cocoRepository.listTrials(campaignId, optimizer, functionId, dimension, reachedTarget, page, size);
    }

    private static String toExperimentLatex(ExperimentDetail detail, ExperimentAnalytics analytics) {
        StringBuilder out = new StringBuilder();
        out.append("\\\\begin{table}[ht]\\n");
        out.append("\\\\centering\\n");
        out.append("\\\\caption{EDAF experiment summary: ").append(escapeLatex(detail.experimentId())).append("}\\n");
        out.append("\\\\begin{tabular}{lrrrrrr}\\n");
        out.append("\\\\hline\\n");
        out.append("Algorithm & Runs & Completed & Success rate & ERT & SP1 & Median best \\\\\\\\ \\n");
        out.append("\\\\hline\\n");
        out.append(escapeLatex(detail.algorithmType())).append(" & ")
                .append(analytics.totalRuns()).append(" & ")
                .append(analytics.completedRuns()).append(" & ")
                .append(formatDouble(analytics.successRate())).append(" & ")
                .append(formatDouble(analytics.ert())).append(" & ")
                .append(formatDouble(analytics.sp1())).append(" & ")
                .append(formatDouble(analytics.bestFitnessBox().median())).append(" \\\\\\\\ \\n");
        out.append("\\\\hline\\n");
        out.append("\\\\end{tabular}\\n");
        out.append("\\\\end{table}\\n");
        return out.toString();
    }

    private static String toProblemComparisonLatex(ProblemComparisonReport report) {
        StringBuilder out = new StringBuilder();
        out.append("\\\\begin{table}[ht]\\n");
        out.append("\\\\centering\\n");
        out.append("\\\\caption{EDAF algorithm comparison for problem ").append(escapeLatex(report.problemType())).append("}\\n");
        out.append("\\\\begin{tabular}{lrrrrrr}\\n");
        out.append("\\\\hline\\n");
        out.append("Algorithm & Runs & Success rate & Mean best & Median best & ERT & SP1 \\\\\\\\ \\n");
        out.append("\\\\hline\\n");
        for (var row : report.algorithms()) {
            out.append(escapeLatex(row.algorithm())).append(" & ")
                    .append(row.totalRuns()).append(" & ")
                    .append(formatDouble(row.successRate())).append(" & ")
                    .append(formatDouble(row.meanBest())).append(" & ")
                    .append(formatDouble(row.medianBest())).append(" & ")
                    .append(formatDouble(row.ert())).append(" & ")
                    .append(formatDouble(row.sp1())).append(" \\\\\\\\ \\n");
        }
        out.append("\\\\hline\\n");
        out.append("\\\\end{tabular}\\n");
        out.append("\\\\end{table}\\n");

        if (!report.pairwiseWilcoxon().isEmpty()) {
            out.append("\\n% Pairwise Wilcoxon-Holm\\n");
            out.append("\\\\begin{tabular}{llrrl}\\n");
            out.append("\\\\hline\\n");
            out.append("A & B & p & p_{Holm} & Better \\\\\\\\ \\n");
            out.append("\\\\hline\\n");
            for (var row : report.pairwiseWilcoxon()) {
                out.append(escapeLatex(row.algorithmA())).append(" & ")
                        .append(escapeLatex(row.algorithmB())).append(" & ")
                        .append(formatDouble(row.pValue())).append(" & ")
                        .append(formatDouble(row.holmAdjustedPValue())).append(" & ")
                        .append(escapeLatex(row.betterAlgorithm())).append(" \\\\\\\\ \\n");
            }
            out.append("\\\\hline\\n");
            out.append("\\\\end{tabular}\\n");
        }
        return out.toString();
    }

    private static String escapeLatex(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\\\", "\\\\textbackslash{}")
                .replace("_", "\\\\_")
                .replace("&", "\\\\&")
                .replace("%", "\\\\%")
                .replace("$", "\\\\$")
                .replace("#", "\\\\#")
                .replace("{", "\\\\{")
                .replace("}", "\\\\}");
    }

    private static String formatDouble(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return "n/a";
        }
        return String.format(java.util.Locale.ROOT, "%.6f", value);
    }

    /**
     * API payload for one hard-delete experiment operation.
     */
    public record DeleteExperimentResponse(
            String experimentId,
            boolean deleted,
            int runsDeleted,
            int runObjectivesDeleted,
            int iterationsDeleted,
            int checkpointsDeleted,
            int eventsDeleted,
            int paramsDeleted,
            int artifactDirectoriesDeleted
    ) {
    }

    /**
     * API payload for cooperative stop requests.
     */
    public record StopRequestBody(String reason) {
    }

    /**
     * API payload for stop request result.
     */
    public record StopResponse(
            String scope,
            String targetId,
            boolean accepted,
            int affectedRuns,
            String message
    ) {
    }

    /**
     * API payload for bulk delete request.
     */
    public record BulkDeleteRequest(List<String> experimentIds) {
    }

    /**
     * API payload for one bulk delete item.
     */
    public record BulkDeleteItemResult(
            String experimentId,
            boolean deleted,
            String reason
    ) {
    }

    /**
     * API payload for bulk delete summary.
     */
    public record BulkDeleteResponse(
            List<BulkDeleteItemResult> items,
            int deletedCount,
            int blockedCount,
            int missingCount
    ) {
    }
}
