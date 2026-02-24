/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.grammar;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.grammar.CsvDataLoader;
import com.knezevic.edaf.v3.problems.grammar.GrammarCsvClassificationProblem;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.List;
import java.util.Map;

/**
 * Plugin for CSV-based symbolic classification (binary and multiclass).
 */
public final class GrammarCsvClassificationProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "grammar-csv-classification";
    }

    @Override
    public String description() {
        return "Grammar-based symbolic classification from CSV dataset (binary and multiclass)";
    }

    @Override
    public GrammarCsvClassificationProblem create(Map<String, Object> params) {
        String datasetPath = Params.str(params, "dataset", "classpath:datasets/grammar/classification/iris_multiclass.csv");
        String target = Params.str(params, "targetColumn", "label");
        String mode = Params.str(params, "classificationMode", "auto");
        String positiveLabel = Params.str(params, "positiveLabel", "1");
        List<String> classValues = Params.list(params, "classValues")
                .stream()
                .map(String::valueOf)
                .toList();
        String score = Params.str(params, "score", "accuracy");
        double threshold = Params.dbl(params, "binaryThreshold", 0.5);
        double penalty = Params.dbl(params, "complexityPenalty", 1.0e-3);

        CsvDataLoader.ClassificationData dataset = CsvDataLoader.loadClassification(datasetPath, target, classValues);
        return new GrammarCsvClassificationProblem(params, dataset, mode, positiveLabel, score, threshold, penalty);
    }
}
