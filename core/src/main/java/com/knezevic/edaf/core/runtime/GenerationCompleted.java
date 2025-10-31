package com.knezevic.edaf.core.runtime;

public final class GenerationCompleted {
    private final String algorithmId;
    private final int generation;
    private final Object bestIndividual;

    public GenerationCompleted(String algorithmId, int generation, Object bestIndividual) {
        this.algorithmId = algorithmId;
        this.generation = generation;
        this.bestIndividual = bestIndividual;
    }

    public String getAlgorithmId() { return algorithmId; }
    public int getGeneration() { return generation; }
    public Object getBestIndividual() { return bestIndividual; }

    @Override
    public String toString() {
        return "GenerationCompleted{" +
                "algorithmId='" + algorithmId + '\'' +
                ", generation=" + generation +
                ", bestIndividual=" + bestIndividual +
                '}';
    }
}


