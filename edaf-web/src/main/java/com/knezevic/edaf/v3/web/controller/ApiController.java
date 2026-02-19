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

    private final RunRepository repository;

    public ApiController(RunRepository repository) {
        this.repository = repository;
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
        return repository.listRuns(new RunQuery(
                q, algorithm, model, problem, status, from, to, minBest, maxBest, page, size, sortBy, sortDir
        ));
    }

    @GetMapping("/runs/{runId}")
    public RunDetail getRun(@PathVariable String runId) {
        RunDetail detail = repository.getRunDetail(runId);
        if (detail == null) {
            throw new ResponseStatusException(NOT_FOUND, "Run not found: " + runId);
        }
        return detail;
    }

    @GetMapping("/runs/{runId}/iterations")
    public List<IterationMetric> listIterations(@PathVariable String runId) {
        return repository.listIterations(runId);
    }

    @GetMapping("/runs/{runId}/events")
    public PageResult<EventRow> listEvents(
            @PathVariable String runId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return repository.listEvents(runId, eventType, q, page, size);
    }

    @GetMapping("/runs/{runId}/checkpoints")
    public List<CheckpointRow> listCheckpoints(@PathVariable String runId) {
        return repository.listCheckpoints(runId);
    }

    @GetMapping("/runs/{runId}/params")
    public List<ExperimentParamRow> listParams(@PathVariable String runId) {
        return repository.listExperimentParams(runId);
    }

    @GetMapping("/facets")
    public FilterFacets facets() {
        return repository.listFacets();
    }
}
