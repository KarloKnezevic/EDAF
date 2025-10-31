package com.knezevic.edaf.core.runtime;

public final class AlgorithmStarted {
    private final String algorithmId;

    public AlgorithmStarted(String algorithmId) {
        this.algorithmId = algorithmId;
    }

    public String getAlgorithmId() { return algorithmId; }

    @Override
    public String toString() {
        return "AlgorithmStarted{" +
                "algorithmId='" + algorithmId + '\'' +
                '}';
    }
}


