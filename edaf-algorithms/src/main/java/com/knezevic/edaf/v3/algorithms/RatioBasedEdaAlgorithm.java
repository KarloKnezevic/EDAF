/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms;

import com.knezevic.edaf.v3.core.api.AbstractEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.AlgorithmContext;
import com.knezevic.edaf.v3.core.api.Population;

/**
 * Generic ratio-based EDA driver.
 *
 * <p>This base class implements the canonical EDA cycle from
 * {@link AbstractEdaAlgorithm}: initialize population, evaluate, select elites,
 * fit model, sample offspring, and apply replacement. The only strategy detail
 * overridden here is elite size:
 * <pre>
 *   eliteCount = round(selectionRatio * populationSize)
 * </pre>
 * clipped to at least one individual.</p>
 *
 * <p>Dedicated algorithm aliases (UMDA, BOA, PBIL, etc.) inherit this behavior
 * and express algorithm identity through plugin/model selection in configuration.</p>
 *
 * <p>References:
 * <ul>
 *     <li>Larrañaga and Lozano (2001) - Estimation of Distribution Algorithms</li>
 *     <li>Mühlenbein and Paass (1996) - UMDA</li>
 * </ul></p>
 *
 * @param <G> genotype type
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public class RatioBasedEdaAlgorithm<G> extends AbstractEdaAlgorithm<G> {

    private final String id;
    private final double selectionRatio;

    /**
     * Creates a ratio-based algorithm driver with bounded elite selection ratio.
     *
     * @param id stable algorithm identifier used in events, persistence and CLI
     * @param selectionRatio fraction of population used for model fitting
     */
    public RatioBasedEdaAlgorithm(String id, double selectionRatio) {
        this.id = id;
        this.selectionRatio = Math.max(0.01, Math.min(1.0, selectionRatio));
    }

    /**
     * Returns algorithm identifier.
     *
     * @return algorithm identifier
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Returns number of selected individuals used for model fitting.
     *
     * @param context algorithm runtime context
     * @param population current population
     * @return elite count used for model fitting
     */
    @Override
    protected int selectionSize(AlgorithmContext<G> context, Population<G> population) {
        return Math.max(1, (int) Math.round(population.size() * selectionRatio));
    }
}
