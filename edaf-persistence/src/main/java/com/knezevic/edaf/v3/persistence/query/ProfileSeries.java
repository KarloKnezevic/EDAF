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
