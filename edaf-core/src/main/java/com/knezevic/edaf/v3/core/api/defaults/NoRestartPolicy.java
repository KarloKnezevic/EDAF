/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.AlgorithmState;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.api.RestartPolicy;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Default restart policy that never restarts.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class NoRestartPolicy<G> implements RestartPolicy<G> {

    /**
     * Returns false because this policy never triggers restart.
     *
     * @param state current algorithm state
     * @return false
     */
    @Override
    public boolean shouldRestart(AlgorithmState<G> state) {
        return false;
    }

    /**
     * Returns original population because restart is disabled.
     *
     * @param state current algorithm state
     * @param representation genotype representation
     * @param rng random stream
     * @return original population
     */
    @Override
    public Population<G> restart(AlgorithmState<G> state, Representation<G> representation, RngStream rng) {
        return state.population();
    }

    /**
     * Returns restart-policy identifier.
     *
     * @return policy identifier
     */
    @Override
    public String name() {
        return "none";
    }
}
