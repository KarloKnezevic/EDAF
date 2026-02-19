package com.knezevic.edaf.v3.persistence.query;

import java.util.List;

/**
 * Distinct facet values used by dashboard filters.
 */
public record FilterFacets(
        List<String> algorithms,
        List<String> models,
        List<String> problems,
        List<String> statuses
) {
}
