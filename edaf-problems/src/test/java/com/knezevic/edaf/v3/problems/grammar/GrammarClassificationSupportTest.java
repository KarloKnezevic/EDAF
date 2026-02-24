/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.grammar;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests binary and multiclass CSV classification support.
 */
class GrammarClassificationSupportTest {

    @Test
    void metricsComputeAccuracyAndMacroF1ForMulticlassConfusion() {
        int[][] confusion = new int[][]{
                {8, 1, 1},
                {2, 6, 2},
                {1, 2, 7}
        };

        double accuracy = ClassificationMetrics.accuracy(confusion);
        double macroF1 = ClassificationMetrics.macroF1(confusion);
        double class2F1 = ClassificationMetrics.binaryF1(confusion, 2);

        assertEquals(0.7, accuracy, 1.0e-12);
        assertEquals(0.697828, macroF1, 1.0e-6);
        assertEquals(0.7, class2F1, 1.0e-12);
    }

    @Test
    void loaderSupportsIrisMulticlassDataset() {
        CsvDataLoader.ClassificationData data = CsvDataLoader.loadClassification(
                "classpath:datasets/grammar/classification/iris_multiclass.csv",
                "label"
        );

        assertEquals(150, data.rows().size());
        assertEquals(3, data.classCount());
        assertEquals(List.of("0", "1", "2"), data.classValues());
    }

    @Test
    void loaderSupportsExplicitClassValuesForMulticlass() {
        CsvDataLoader.ClassificationData data = CsvDataLoader.loadClassification(
                "classpath:datasets/grammar/classification/wine_recognition_multiclass.csv",
                "label",
                List.of("0", "1", "2")
        );

        assertEquals(178, data.rows().size());
        assertEquals(3, data.classCount());
        assertEquals(List.of("0", "1", "2"), data.classValues());
    }

    @Test
    void loaderRejectsUnexpectedLabelsWhenClassValuesAreProvided() throws Exception {
        Path csv = Files.createTempFile("edaf-grammar-invalid-label", ".csv");
        Files.writeString(csv, """
                x1,x2,label
                1.0,2.0,A
                2.0,3.0,C
                """);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> CsvDataLoader.loadClassification(csv.toString(), "label", List.of("A", "B"))
        );
        assertTrue(error.getMessage().contains("not listed in classValues"));
    }
}
