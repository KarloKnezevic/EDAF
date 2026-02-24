/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.grammar;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.grammar.eval.EvaluationContext;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Nguyen symbolic regression benchmark using grammar-based expression representation.
 */
public final class GrammarNguyenRegressionProblem extends AbstractGrammarBitStringProblem {

    private final int variant;
    private final int sampleCount;
    private final double minX;
    private final double maxX;
    private final String variableName;

    public GrammarNguyenRegressionProblem(Map<String, Object> params,
                                          int variant,
                                          int sampleCount,
                                          double minX,
                                          double maxX,
                                          String variableName,
                                          double complexityPenalty) {
        super(params, complexityPenalty);
        this.variant = Math.max(1, Math.min(8, variant));
        this.sampleCount = Math.max(8, sampleCount);
        this.minX = minX;
        this.maxX = maxX;
        this.variableName = variableName == null || variableName.isBlank() ? "x" : variableName;
    }

    @Override
    public String name() {
        return "grammar-nguyen-" + variant;
    }

    @Override
    public Fitness evaluate(BitString genotype) {
        var inspection = inspect(genotype);
        double mse = 0.0;

        for (int i = 0; i < sampleCount; i++) {
            double t = sampleCount == 1 ? 0.5 : i / (double) (sampleCount - 1);
            double x = minX + t * (maxX - minX);
            Map<String, Double> vars = new LinkedHashMap<>();
            vars.put(variableName, x);

            double prediction = treeEngine.evaluate(genotype, EvaluationContext.real(vars));
            double target = targetFunction(x);
            double error = prediction - target;
            mse += error * error;
        }

        mse /= sampleCount;
        return new ScalarFitness(mse + complexityTerm(inspection));
    }

    private double targetFunction(double x) {
        return switch (variant) {
            case 1 -> x * x * x + x * x + x;
            case 2 -> Math.pow(x, 4) + Math.pow(x, 3) + x * x + x;
            case 3 -> Math.pow(x, 5) + Math.pow(x, 4) + Math.pow(x, 3) + x * x + x;
            case 4 -> Math.pow(x, 6) + Math.pow(x, 5) + Math.pow(x, 4) + Math.pow(x, 3) + x * x + x;
            case 5 -> Math.sin(x * x) * Math.cos(x) - 1.0;
            case 6 -> Math.sin(x) + Math.sin(x + x * x);
            case 7 -> Math.log(Math.abs(x) + 1.0) + Math.log(x * x + 1.0);
            case 8 -> Math.sqrt(Math.abs(x));
            default -> x;
        };
    }
}
