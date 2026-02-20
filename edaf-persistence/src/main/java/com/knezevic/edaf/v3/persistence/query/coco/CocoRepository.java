package com.knezevic.edaf.v3.persistence.query.coco;

import com.knezevic.edaf.v3.persistence.query.PageResult;

import java.util.List;

/**
 * Read-side repository for COCO campaign dashboards and reports.
 */
public interface CocoRepository {

    /**
     * Lists campaigns with optional filtering and pagination.
     */
    PageResult<CocoCampaignListItem> listCampaigns(CocoCampaignQuery query);

    /**
     * Gets one campaign detail row.
     */
    CocoCampaignDetail getCampaign(String campaignId);

    /**
     * Lists optimizer config rows for campaign.
     */
    List<CocoOptimizerConfigRow> listOptimizers(String campaignId);

    /**
     * Lists aggregate rows for campaign.
     */
    List<CocoAggregateMetric> listAggregates(String campaignId);

    /**
     * Lists trial rows for campaign with optional filters.
     */
    PageResult<CocoTrialMetric> listTrials(String campaignId,
                                           String optimizerId,
                                           Integer functionId,
                                           Integer dimension,
                                           Boolean reachedTarget,
                                           int page,
                                           int size);
}
