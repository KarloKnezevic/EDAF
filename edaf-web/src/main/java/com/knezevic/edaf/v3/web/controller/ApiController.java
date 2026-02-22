package com.knezevic.edaf.v3.web.controller;

import com.knezevic.edaf.v3.persistence.query.CheckpointRow;
import com.knezevic.edaf.v3.persistence.query.ExperimentAnalytics;
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
import com.knezevic.edaf.v3.persistence.query.coco.CocoAggregateMetric;
import com.knezevic.edaf.v3.persistence.query.coco.CocoCampaignDetail;
import com.knezevic.edaf.v3.persistence.query.coco.CocoCampaignListItem;
import com.knezevic.edaf.v3.persistence.query.coco.CocoCampaignQuery;
import com.knezevic.edaf.v3.persistence.query.coco.CocoOptimizerConfigRow;
import com.knezevic.edaf.v3.persistence.query.coco.CocoRepository;
import com.knezevic.edaf.v3.persistence.query.coco.CocoTrialMetric;
import com.knezevic.edaf.v3.web.service.RunArtifactService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

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

    public ApiController(RunRepository runRepository,
                         CocoRepository cocoRepository,
                         RunArtifactService runArtifactService) {
        this.runRepository = runRepository;
        this.cocoRepository = cocoRepository;
        this.runArtifactService = runArtifactService;
    }

    @GetMapping("/experiments")
    public PageResult<ExperimentListItem> listExperiments(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String algorithm,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String problem,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return runRepository.listExperiments(new ExperimentQuery(
                q, algorithm, model, problem, from, to, page, size, sortBy, sortDir
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
}
