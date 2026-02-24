/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.grammar;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.grammar.CsvDataLoader;
import com.knezevic.edaf.v3.problems.grammar.GrammarCsvRegressionProblem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin for CSV-based symbolic regression.
 */
public final class GrammarCsvRegressionProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "grammar-csv-regression";
    }

    @Override
    public String description() {
        return "Grammar-based symbolic regression from CSV dataset";
    }

    @Override
    public GrammarCsvRegressionProblem create(Map<String, Object> params) {
        String datasetPath = Params.str(params, "dataset", "classpath:datasets/grammar/regression/nguyen_like.csv");
        String target = Params.str(params, "targetColumn", "y");
        String metric = Params.str(params, "metric", "mse");
        double penalty = Params.dbl(params, "complexityPenalty", 1.0e-3);

        CsvDataLoader.RegressionData dataset = CsvDataLoader.loadRegression(datasetPath, target);
        return new GrammarCsvRegressionProblem(params, dataset, metric, penalty);
    }
}
