/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

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
