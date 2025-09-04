package com.knezevic.edaf.core.impl;

import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.OptimizationType;
import com.knezevic.edaf.core.api.Problem;

import java.util.Map;

/**
 * An abstract base class for implementing problems.
 * <p>
 * This class provides a default implementation for handling the optimization type.
 *
 * @param <T> The type of individual to be evaluated.
 */
public abstract class AbstractProblem<T extends Individual> implements Problem<T> {

    private final OptimizationType optimizationType;

    /**
     * Creates a new instance of the abstract problem.
     *
     * @param params A map of parameters for the problem, which must include "optimizationType".
     */
    protected AbstractProblem(Map<String, Object> params) {
        if (!params.containsKey("optimizationType")) {
            throw new IllegalArgumentException("Problem parameters must contain 'optimizationType'.");
        }
        this.optimizationType = (OptimizationType) params.get("optimizationType");
    }

    @Override
    public OptimizationType getOptimizationType() {
        return optimizationType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void evaluate(T individual);
}
