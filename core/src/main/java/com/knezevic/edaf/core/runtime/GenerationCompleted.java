package com.knezevic.edaf.core.runtime;

import com.knezevic.edaf.core.api.Individual;

public final class GenerationCompleted {
    private final String algorithmId;
    private final int generation;
    private final Individual<?> bestIndividual;
    private final double bestFitness;
    private final double worstFitness;
    private final double avgFitness;
    private final double stdFitness;

    public GenerationCompleted(String algorithmId, int generation, Individual<?> bestIndividual) {
        this(algorithmId, generation, bestIndividual, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public GenerationCompleted(String algorithmId, int generation, Individual<?> bestIndividual,
                              double bestFitness, double worstFitness, double avgFitness, double stdFitness) {
        this.algorithmId = algorithmId;
        this.generation = generation;
        this.bestIndividual = bestIndividual;
        this.bestFitness = bestFitness;
        this.worstFitness = worstFitness;
        this.avgFitness = avgFitness;
        this.stdFitness = stdFitness;
    }

    public String getAlgorithmId() { return algorithmId; }
    public int getGeneration() { return generation; }
    public Individual<?> getBestIndividual() { return bestIndividual; }
    public double getBestFitness() { return bestFitness; }
    public double getWorstFitness() { return worstFitness; }
    public double getAvgFitness() { return avgFitness; }
    public double getStdFitness() { return stdFitness; }
    
    public boolean hasStatistics() {
        return !Double.isNaN(bestFitness) && !Double.isNaN(worstFitness) 
            && !Double.isNaN(avgFitness) && !Double.isNaN(stdFitness);
    }

    @Override
    public String toString() {
        return "GenerationCompleted{" +
                "algorithmId='" + algorithmId + '\'' +
                ", generation=" + generation +
                ", bestIndividual=" + bestIndividual +
                (hasStatistics() ? String.format(", best=%.4f, worst=%.4f, avg=%.4f, std=%.4f", 
                    bestFitness, worstFitness, avgFitness, stdFitness) : "") +
                '}';
    }
}


