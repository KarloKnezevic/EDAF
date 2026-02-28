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
 * Restart policy that triggers after a fixed number of non-improving iterations.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class StagnationRestartPolicy<G> implements RestartPolicy<G> {

    private final int patience;
    private int lastRestartIteration;

    /**
     * Creates stagnation restart policy with fixed patience window.
     *
     * @param patience number of iterations before restart trigger
     */
    public StagnationRestartPolicy(int patience) {
        this.patience = Math.max(1, patience);
    }

    /**
     * Returns whether stagnation window exceeded configured patience.
     *
     * @param state current algorithm state
     * @return true when restart should happen
     */
    @Override
    public boolean shouldRestart(AlgorithmState<G> state) {
        return state.iteration() - lastRestartIteration >= patience;
    }

    /**
     * Creates restart population snapshot and updates restart bookkeeping.
     *
     * @param state current algorithm state
     * @param representation genotype representation
     * @param rng random stream
     * @return restart population
     */
    @Override
    public Population<G> restart(AlgorithmState<G> state, Representation<G> representation, RngStream rng) {
        Population<G> restart = new Population<>(state.population().objectiveSense());
        for (int i = 0; i < state.population().size(); i++) {
            // genotype-only restart; evaluation is deferred to algorithm iteration flow.
            restart.add(state.population().get(i));
        }
        lastRestartIteration = state.iteration();
        return restart;
    }

    /**
     * Returns restart-policy identifier.
     *
     * @return policy identifier
     */
    @Override
    public String name() {
        return "stagnation";
    }
}
