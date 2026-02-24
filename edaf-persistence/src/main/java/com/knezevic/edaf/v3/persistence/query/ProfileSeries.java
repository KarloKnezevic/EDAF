/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.query;

import java.util.List;

/**
 * Named profile series for chart rendering.
 */
public record ProfileSeries(
        String name,
        List<ProfilePoint> points
) {
}
