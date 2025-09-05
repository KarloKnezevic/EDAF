package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.configuration.pojos.Configuration.SelectionConfig;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CgpConfig {

    @Min(1)
    private int populationSize = 200;

    @Min(1)
    private int generations = 1000;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double mutationRate = 0.02;

    @NotEmpty
    private List<String> functionSet;

    @Min(1)
    private int rows = 1;

    @Min(1)
    private int cols = 20;

    @Min(1)
    private int levelsBack = 5;

    private boolean useCrossover = false;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double crossoverRate = 0.8;

    @NotNull
    private ReplacementStrategy replacementStrategy = ReplacementStrategy.GENERATIONAL;

    private Long randomSeed;

    @NotNull
    private SelectionConfig selection;

    // Getters and Setters

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public int getGenerations() {
        return generations;
    }

    public void setGenerations(int generations) {
        this.generations = generations;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

    public List<String> getFunctionSet() {
        return functionSet;
    }

    public void setFunctionSet(List<String> functionSet) {
        this.functionSet = functionSet;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getLevelsBack() {
        return levelsBack;
    }

    public void setLevelsBack(int levelsBack) {
        this.levelsBack = levelsBack;
    }

    public boolean isUseCrossover() {
        return useCrossover;
    }

    public void setUseCrossover(boolean useCrossover) {
        this.useCrossover = useCrossover;
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public void setCrossoverRate(double crossoverRate) {
        this.crossoverRate = crossoverRate;
    }

    public ReplacementStrategy getReplacementStrategy() {
        return replacementStrategy;
    }

    public void setReplacementStrategy(ReplacementStrategy replacementStrategy) {
        this.replacementStrategy = replacementStrategy;
    }

    public Long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(Long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public SelectionConfig getSelection() {
        return selection;
    }

    public void setSelection(SelectionConfig selection) {
        this.selection = selection;
    }
}
