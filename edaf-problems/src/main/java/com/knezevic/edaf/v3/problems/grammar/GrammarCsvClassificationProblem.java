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
import java.util.Locale;

/**
 * Symbolic classifier over CSV datasets (binary and multiclass).
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarCsvClassificationProblem extends AbstractGrammarBitStringProblem {

    private final CsvDataLoader.ClassificationData dataset;
    private final String score;
    private final String classificationMode;
    private final String positiveLabel;
    private final double binaryThreshold;

    public GrammarCsvClassificationProblem(Map<String, Object> params,
                                           CsvDataLoader.ClassificationData dataset,
                                           String classificationMode,
                                           String positiveLabel,
                                           String score,
                                           double binaryThreshold,
                                           double complexityPenalty) {
        super(params, complexityPenalty);
        this.dataset = dataset;
        this.classificationMode = classificationMode == null
                ? "auto"
                : classificationMode.trim().toLowerCase(Locale.ROOT);
        this.positiveLabel = positiveLabel == null ? "1" : positiveLabel.trim();
        this.score = score == null ? "accuracy" : score.trim().toLowerCase(Locale.ROOT);
        this.binaryThreshold = binaryThreshold;
    }

    /**
     * Returns problem identifier.
     *
     * @return problem identifier
     */
    @Override
    public String name() {
        return "grammar-csv-classification";
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

        Mode mode = resolveMode();
        int classCount = dataset.classCount();
        int[][] confusion = new int[classCount][classCount];
        int positiveIndex = resolvePositiveClassIndex();
        int negativeIndex = resolveNegativeClassIndex(positiveIndex);

        for (CsvDataLoader.ClassificationData.Row row : dataset.rows()) {
            int actualIndex = dataset.classIndex(row.label());
            int predictedIndex;
            if (mode == Mode.BINARY) {
                boolean predictedPositive = treeEngine.evaluate(genotype, EvaluationContext.real(row.features())) > binaryThreshold;
                predictedIndex = predictedPositive ? positiveIndex : negativeIndex;
            } else {
                double value = treeEngine.evaluate(genotype, EvaluationContext.real(row.features()));
                predictedIndex = toMulticlassIndex(value, classCount);
            }
            confusion[actualIndex][predictedIndex]++;
        }

        double accuracy = ClassificationMetrics.accuracy(confusion);
        double macroF1 = ClassificationMetrics.macroF1(confusion);
        double binaryF1 = ClassificationMetrics.binaryF1(confusion, positiveIndex);

        double quality = switch (score) {
            case "f1", "binary_f1" -> binaryF1;
            case "macro_f1", "multiclass_f1" -> macroF1;
            case "accuracy" -> accuracy;
            default -> accuracy;
        };
        if (mode == Mode.MULTICLASS && "f1".equals(score)) {
            quality = macroF1;
        }
        return new ScalarFitness((1.0 - quality) + complexityTerm(inspection));
    }

    private Mode resolveMode() {
        return switch (classificationMode) {
            case "binary" -> {
                if (dataset.classCount() != 2) {
                    throw new IllegalArgumentException("classificationMode=binary requires exactly 2 classes, found "
                            + dataset.classCount() + ": " + dataset.classValues());
                }
                yield Mode.BINARY;
            }
            case "multiclass" -> {
                if (dataset.classCount() < 2) {
                    throw new IllegalArgumentException("classificationMode=multiclass requires at least 2 classes");
                }
                yield Mode.MULTICLASS;
            }
            default -> dataset.classCount() <= 2 ? Mode.BINARY : Mode.MULTICLASS;
        };
    }

    private int resolvePositiveClassIndex() {
        if (dataset.classCount() == 0) {
            return 0;
        }
        if (dataset.classIndexByLabel().containsKey(positiveLabel)) {
            return dataset.classIndex(positiveLabel);
        }
        if (dataset.classIndexByLabel().containsKey("1")) {
            return dataset.classIndex("1");
        }
        return Math.min(1, dataset.classCount() - 1);
    }

    private int resolveNegativeClassIndex(int positiveIndex) {
        if (dataset.classCount() <= 1) {
            return positiveIndex;
        }
        for (int i = 0; i < dataset.classCount(); i++) {
            if (i != positiveIndex) {
                return i;
            }
        }
        return positiveIndex;
    }

    private static int toMulticlassIndex(double value, int classCount) {
        if (classCount <= 1) {
            return 0;
        }
        if (!Double.isFinite(value)) {
            return 0;
        }
        double clipped = Math.max(0.0, Math.min(classCount - 1, value));
        return (int) Math.round(clipped);
    }

    private enum Mode {
        BINARY,
        MULTICLASS
    }
}
