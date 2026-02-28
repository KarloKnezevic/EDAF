/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.algorithms.dynamic;

import com.knezevic.edaf.v3.core.api.AlgorithmContext;
import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Population;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic EDA driver that injects random immigrants after each iteration.
 *
 * <p>Given next population size {@code N}, immigrant count is:
 * <pre>
 *   I = max(minImmigrants, round(immigrantRatio * N))
 * </pre>
 * Worst {@code I} individuals are replaced by new random candidates sampled from
 * representation priors and evaluated immediately.
 *
 * <p>References:
 * <ol>
 *   <li>S. Yang, "Non-stationary problem optimization using the primal-dual genetic
 *   algorithm," CEC, 2003. Random immigrants mechanism adapted to EDA pipeline.</li>
 *   <li>J. Branke, "Evolutionary Optimization in Dynamic Environments," Kluwer, 2001.</li>
 * </ol>
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class RandomImmigrantsEdaAlgorithm<G> extends AdaptiveRatioEdaAlgorithm<G> {

    private final double immigrantRatio;
    private final int minImmigrants;

    public RandomImmigrantsEdaAlgorithm(double selectionRatio,
                                        double minRatio,
                                        double maxRatio,
                                        double immigrantRatio,
                                        int minImmigrants) {
        super("random-immigrants-eda", selectionRatio, minRatio, maxRatio);
        this.immigrantRatio = clamp(immigrantRatio, 0.0, 0.9);
        this.minImmigrants = Math.max(0, minImmigrants);
    }

    /**
     * Updates adaptive ratio after each iteration.
     *
     * @param normalizedImprovement normalized fitness improvement for current iteration
     */
    @Override
    protected void adaptRatio(double normalizedImprovement) {
        // Random immigrants keep moderate pressure; adapt gently around stability.
        if (normalizedImprovement < 0.0) {
            setRatio(ratio() + 0.01);
        } else if (normalizedImprovement > 0.01) {
            setRatio(ratio() - 0.005);
        }
    }

    /**
     * Injects random immigrants by replacing the worst-ranked individuals.
     *
     * @param context algorithm runtime context
     * @param previous previous population
     * @param next next population
     * @return population after random-immigrant replacement
     */
    @Override
    protected Population<G> postProcessPopulation(AlgorithmContext<G> context, Population<G> previous, Population<G> next) {
        int immigrants = Math.max(minImmigrants, (int) Math.round(next.size() * immigrantRatio));
        immigrants = Math.min(immigrants, Math.max(0, next.size() - 1));
        if (immigrants <= 0) {
            return next;
        }

        next.sortByFitness();
        List<Individual<G>> sorted = new ArrayList<>(next.asList());
        int survivorCount = sorted.size() - immigrants;

        Population<G> updated = new Population<>(next.objectiveSense());
        for (int i = 0; i < survivorCount; i++) {
            updated.add(sorted.get(i));
        }

        for (int i = 0; i < immigrants; i++) {
            G genotype = context.representation().random(context.rngManager().stream("immigrant-init"));
            G feasible = context.constraintHandling().enforce(
                    genotype,
                    context.representation(),
                    context.problem(),
                    context.rngManager().stream("immigrant-constraint")
            );
            Fitness fitness = context.problem().evaluate(feasible);
            Individual<G> immigrant = new Individual<>(feasible, fitness);
            updated.add(context.localSearch().refine(
                    immigrant,
                    context.problem(),
                    context.representation(),
                    context.rngManager().stream("immigrant-local-search")
            ));
        }
        return updated;
    }
}
