package com.knezevic.edaf.core.runtime;

public final class AlgorithmTerminated {
    private final String algorithmId;
    private final int generation;

    public AlgorithmTerminated(String algorithmId, int generation) {
        this.algorithmId = algorithmId;
        this.generation = generation;
    }

    public String getAlgorithmId() { return algorithmId; }
    public int getGeneration() { return generation; }

    @Override
    public String toString() {
        return "AlgorithmTerminated{" +
                "algorithmId='" + algorithmId + '\'' +
                ", generation=" + generation +
                '}';
    }
}


