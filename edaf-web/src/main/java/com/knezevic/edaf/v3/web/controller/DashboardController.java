package com.knezevic.edaf.v3.web.controller;

import com.knezevic.edaf.v3.persistence.query.RunRepository;
import com.knezevic.edaf.v3.persistence.query.RunQuery;
import com.knezevic.edaf.v3.persistence.query.coco.CocoCampaignQuery;
import com.knezevic.edaf.v3.persistence.query.coco.CocoRepository;
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

    private final RunRepository runRepository;
    private final CocoRepository cocoRepository;

    public DashboardController(RunRepository runRepository, CocoRepository cocoRepository) {
        this.runRepository = runRepository;
        this.cocoRepository = cocoRepository;
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
        model.addAttribute("facets", runRepository.listFacets());
        model.addAttribute("initialPage", runRepository.listRuns(new RunQuery(
                q, algorithm, modelFilter, problem, status, from, to, minBest, maxBest, page, size, sortBy, sortDir
        )));
        return "index";
    }

    @GetMapping("/runs/{runId}")
    public String run(@PathVariable String runId, Model model) {
        var detail = runRepository.getRunDetail(runId);
        if (detail == null) {
            throw new ResponseStatusException(NOT_FOUND, "Run not found: " + runId);
        }
        model.addAttribute("run", detail);
        model.addAttribute("runId", runId);
        model.addAttribute("iterations", runRepository.listIterations(runId));
        model.addAttribute("checkpoints", runRepository.listCheckpoints(runId));
        model.addAttribute("params", runRepository.listExperimentParams(runId));
        model.addAttribute("eventsPage", runRepository.listEvents(runId, null, null, 0, 25));
        return "run";
    }

    @GetMapping("/coco")
    public String cocoIndex(Model model,
                            @RequestParam(required = false) String q,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String suite,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "25") int size,
                            @RequestParam(defaultValue = "created_at") String sortBy,
                            @RequestParam(defaultValue = "desc") String sortDir) {
        model.addAttribute("initialCampaignPage",
                cocoRepository.listCampaigns(new CocoCampaignQuery(q, status, suite, page, size, sortBy, sortDir)));
        return "coco";
    }

    @GetMapping("/coco/{campaignId}")
    public String cocoCampaign(@PathVariable String campaignId, Model model) {
        var campaign = cocoRepository.getCampaign(campaignId);
        if (campaign == null) {
            throw new ResponseStatusException(NOT_FOUND, "COCO campaign not found: " + campaignId);
        }
        model.addAttribute("campaign", campaign);
        model.addAttribute("campaignId", campaignId);
        model.addAttribute("optimizers", cocoRepository.listOptimizers(campaignId));
        model.addAttribute("aggregates", cocoRepository.listAggregates(campaignId));
        model.addAttribute("trialsPage", cocoRepository.listTrials(campaignId, null, null, null, null, 0, 25));
        return "coco-campaign";
    }
}
