package com.knezevic.edaf.v3.web.controller;

import com.knezevic.edaf.v3.persistence.query.RunRepository;
import com.knezevic.edaf.v3.persistence.query.RunQuery;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Thymeleaf page controller for run list/detail views.
 */
@Controller
public class DashboardController {

    private final RunRepository repository;

    public DashboardController(RunRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String algorithm,
                        @RequestParam(name = "model", required = false) String modelFilter,
                        @RequestParam(required = false) String problem,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String from,
                        @RequestParam(required = false) String to,
                        @RequestParam(required = false) Double minBest,
                        @RequestParam(required = false) Double maxBest,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "25") int size,
                        @RequestParam(defaultValue = "start_time") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDir) {
        model.addAttribute("facets", repository.listFacets());
        model.addAttribute("initialPage", repository.listRuns(new RunQuery(
                q, algorithm, modelFilter, problem, status, from, to, minBest, maxBest, page, size, sortBy, sortDir
        )));
        return "index";
    }

    @GetMapping("/runs/{runId}")
    public String run(@PathVariable String runId, Model model) {
        var detail = repository.getRunDetail(runId);
        if (detail == null) {
            throw new ResponseStatusException(NOT_FOUND, "Run not found: " + runId);
        }
        model.addAttribute("run", detail);
        model.addAttribute("runId", runId);
        model.addAttribute("iterations", repository.listIterations(runId));
        model.addAttribute("checkpoints", repository.listCheckpoints(runId));
        model.addAttribute("params", repository.listExperimentParams(runId));
        model.addAttribute("eventsPage", repository.listEvents(runId, null, null, 0, 25));
        return "run";
    }
}
