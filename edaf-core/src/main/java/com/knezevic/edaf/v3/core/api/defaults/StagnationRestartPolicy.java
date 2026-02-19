package com.knezevic.edaf.v3.core.api.defaults;

import com.knezevic.edaf.v3.core.api.AlgorithmState;
import com.knezevic.edaf.v3.core.api.Population;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.api.RestartPolicy;
import com.knezevic.edaf.v3.core.rng.RngStream;

/**
 * Restart policy that triggers after a fixed number of non-improving iterations.
 */
public final class StagnationRestartPolicy<G> implements RestartPolicy<G> {

    private final int patience;
    private int lastRestartIteration;

    public StagnationRestartPolicy(int patience) {
        this.patience = Math.max(1, patience);
    }

    @Override
    public boolean shouldRestart(AlgorithmState<G> state) {
        return state.iteration() - lastRestartIteration >= patience;
    }

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

    @Override
    public String name() {
        return "stagnation";
    }
}
