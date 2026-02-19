package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.NichingPolicy;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Fitness sharing niching scaffold.
 *
 * TODO(priority=medium): Implement distance-based sharing and niche penalties.
 */
public final class FitnessSharingNichingPolicy<G> implements NichingPolicy<G> {

    @Override
    public Population<G> apply(Population<G> population, Representation<G> representation, RngStream rng) {
        return population;
    }

    @Override
    public String name() {
        return "fitness-sharing";
    }
}
