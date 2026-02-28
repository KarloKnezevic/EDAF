/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.grammar;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.grammar.eval.EvaluationContext;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Symbolic regression over CSV dataset.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarCsvRegressionProblem extends AbstractGrammarBitStringProblem {

    private final CsvDataLoader.RegressionData dataset;
    private final String metric;

    public GrammarCsvRegressionProblem(Map<String, Object> params,
                                       CsvDataLoader.RegressionData dataset,
                                       String metric,
                                       double complexityPenalty) {
        super(params, complexityPenalty);
        this.dataset = dataset;
        this.metric = metric == null ? "mse" : metric.trim().toLowerCase();
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "grammar-csv-regression";
    }

    /**
     * Evaluates candidate solution.
     *
     * @param genotype candidate genotype
     * @return fitness value
     */
    @Override
    public Fitness evaluate(BitString genotype) {
        var inspection = inspect(genotype);
        double aggregate = 0.0;

        for (CsvDataLoader.RegressionData.Row row : dataset.rows()) {
            double prediction = treeEngine.evaluate(genotype, EvaluationContext.real(row.features()));
            double error = prediction - row.target();
            aggregate += "mae".equals(metric) ? Math.abs(error) : (error * error);
        }

        aggregate /= dataset.rows().size();
        return new ScalarFitness(aggregate + complexityTerm(inspection));
    }
}
