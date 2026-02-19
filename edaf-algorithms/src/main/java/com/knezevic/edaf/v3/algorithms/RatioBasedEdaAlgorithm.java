package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.core.api.AbstractEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.AlgorithmContext;
import com.knezevic.edaf.v3.core.api.Population;

/**
 * Generic model-based EDA driver using ratio-based truncation for model fit selection size.
 */
public class RatioBasedEdaAlgorithm<G> extends AbstractEdaAlgorithm<G> {

    private final String id;
    private final double selectionRatio;

    public RatioBasedEdaAlgorithm(String id, double selectionRatio) {
        this.id = id;
        this.selectionRatio = Math.max(0.01, Math.min(1.0, selectionRatio));
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    protected int selectionSize(AlgorithmContext<G> context, Population<G> population) {
        return Math.max(1, (int) Math.round(population.size() * selectionRatio));
    }
}
