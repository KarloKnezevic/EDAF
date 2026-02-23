package com.knezevic.edaf.v3.problems.grammar;

/**
 * Utility functions for single-label classification metric computation.
 */
public final class ClassificationMetrics {

    private ClassificationMetrics() {
        // utility class
    }

    /**
     * Computes plain classification accuracy from confusion matrix.
     */
    public static double accuracy(int[][] confusion) {
        long correct = 0;
        long total = 0;
        for (int actual = 0; actual < confusion.length; actual++) {
            int[] row = confusion[actual];
            for (int predicted = 0; predicted < row.length; predicted++) {
                int value = row[predicted];
                if (actual == predicted) {
                    correct += value;
                }
                total += value;
            }
        }
        if (total <= 0) {
            return 0.0;
        }
        return correct / (double) total;
    }

    /**
     * Computes macro averaged F1 score across all classes.
     */
    public static double macroF1(int[][] confusion) {
        if (confusion.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        for (int classIndex = 0; classIndex < confusion.length; classIndex++) {
            sum += f1ForClass(confusion, classIndex);
        }
        return sum / confusion.length;
    }

    /**
     * Computes F1 score for a selected positive class in one-vs-rest view.
     */
    public static double binaryF1(int[][] confusion, int positiveClassIndex) {
        if (confusion.length == 0) {
            return 0.0;
        }
        int clamped = Math.max(0, Math.min(confusion.length - 1, positiveClassIndex));
        return f1ForClass(confusion, clamped);
    }

    private static double f1ForClass(int[][] confusion, int classIndex) {
        long tp = confusion[classIndex][classIndex];
        long fp = 0;
        long fn = 0;

        for (int actual = 0; actual < confusion.length; actual++) {
            if (actual != classIndex) {
                fp += confusion[actual][classIndex];
            }
        }
        for (int predicted = 0; predicted < confusion[classIndex].length; predicted++) {
            if (predicted != classIndex) {
                fn += confusion[classIndex][predicted];
            }
        }

        double precision = tp / (double) Math.max(1L, tp + fp);
        double recall = tp / (double) Math.max(1L, tp + fn);
        if ((precision + recall) <= 1.0e-12) {
            return 0.0;
        }
        return (2.0 * precision * recall) / (precision + recall);
    }
}
