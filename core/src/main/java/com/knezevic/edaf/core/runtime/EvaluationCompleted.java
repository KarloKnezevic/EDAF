package com.knezevic.edaf.core.runtime;

public final class EvaluationCompleted {
    private final String algorithmId;
    private final int generation;
    private final int evaluatedCount;
    private final long durationNanos;

    public EvaluationCompleted(String algorithmId, int generation, int evaluatedCount, long durationNanos) {
        this.algorithmId = algorithmId;
        this.generation = generation;
        this.evaluatedCount = evaluatedCount;
        this.durationNanos = durationNanos;
    }

    public String getAlgorithmId() { return algorithmId; }
    public int getGeneration() { return generation; }
    public int getEvaluatedCount() { return evaluatedCount; }
    public long getDurationNanos() { return durationNanos; }

    @Override
    public String toString() {
        return "EvaluationCompleted{" +
                "algorithmId='" + algorithmId + '\'' +
                ", generation=" + generation +
                ", evaluatedCount=" + evaluatedCount +
                ", durationNanos=" + durationNanos +
                '}';
    }
}


