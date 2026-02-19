package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.AlgorithmState;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.api.RestartPolicy;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Default restart policy that never restarts.
 */
public final class NoRestartPolicy<G> implements RestartPolicy<G> {

    @Override
    public boolean shouldRestart(AlgorithmState<G> state) {
        return false;
    }

    @Override
    public Population<G> restart(AlgorithmState<G> state, Representation<G> representation, RngStream rng) {
        return state.population();
    }

    @Override
    public String name() {
        return "none";
    }
}
