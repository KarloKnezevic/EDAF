/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.grammar;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.grammar.GrammarTreeEngine;
import com.knezevic.edaf.v3.repr.grammar.eval.EvaluationContext;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Boolean XOR symbolic classification benchmark.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarXorProblem extends AbstractGrammarBitStringProblem {

    private final int bits;
    private final String variablePrefix;

    /**
     * Creates a new GrammarXorProblem instance.
     *
     * @param params configuration the input value map
     * @param bits the bits argument
     * @param variablePrefix the variablePrefix argument
     * @param complexityPenalty the complexityPenalty argument
     */
    public GrammarXorProblem(Map<String, Object> params, int bits, String variablePrefix, double complexityPenalty) {
        super(params, complexityPenalty);
        this.bits = Math.max(2, bits);
        this.variablePrefix = variablePrefix == null || variablePrefix.isBlank() ? "x" : variablePrefix;
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "grammar-xor-" + bits;
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
    @Override
    public Fitness evaluate(BitString genotype) {
        GrammarTreeEngine.TreeInspection inspection = inspect(genotype);
        int total = 1 << bits;
        int mismatches = 0;

        for (int mask = 0; mask < total; mask++) {
            Map<String, Boolean> inputs = new LinkedHashMap<>();
            int ones = 0;
            for (int i = 0; i < bits; i++) {
                boolean value = ((mask >> i) & 1) == 1;
                if (value) {
                    ones++;
                }
                inputs.put(variablePrefix + i, value);
            }
            boolean predicted = treeEngine.evaluateBoolean(genotype, EvaluationContext.bool(inputs));
            boolean expected = (ones % 2) == 1;
            if (predicted != expected) {
                mismatches++;
            }
        }

        double errorRate = mismatches / (double) total;
        return new ScalarFitness(errorRate + complexityTerm(inspection));
    }
}
