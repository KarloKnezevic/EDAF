/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

import java.util.List;

/**
 * Generic page envelope for API and repository responses.
 */
public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long total,
        long totalPages
) {
}
