package com.knezevic.edaf.v3.web.controller;

import com.knezevic.edaf.v3.persistence.query.CheckpointRow;
import com.knezevic.edaf.v3.persistence.query.EventRow;
import com.knezevic.edaf.v3.persistence.query.ExperimentParamRow;
import com.knezevic.edaf.v3.persistence.query.FilterFacets;
import com.knezevic.edaf.v3.persistence.query.IterationMetric;
import com.knezevic.edaf.v3.persistence.query.PageResult;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    public ApiController(RunRepository runRepository, CocoRepository cocoRepository) {
        this.runRepository = runRepository;
        this.cocoRepository = cocoRepository;
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
            throw new ResponseStatusException(NOT_FOUND, "Run not found: " + runId);
        }
        return detail;
    }

    @GetMapping("/runs/{runId}/iterations")
    public List<IterationMetric> listIterations(@PathVariable String runId) {
        return runRepository.listIterations(runId);
    }

    @GetMapping("/runs/{runId}/events")
    public PageResult<EventRow> listEvents(
            @PathVariable String runId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return runRepository.listEvents(runId, eventType, q, page, size);
    }

    @GetMapping("/runs/{runId}/checkpoints")
    public List<CheckpointRow> listCheckpoints(@PathVariable String runId) {
        return runRepository.listCheckpoints(runId);
    }

    @GetMapping("/runs/{runId}/params")
    public List<ExperimentParamRow> listParams(@PathVariable String runId) {
        return runRepository.listExperimentParams(runId);
    }

    @GetMapping("/facets")
    public FilterFacets facets() {
        return runRepository.listFacets();
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
}
