package com.knezevic.edaf.core.runtime;

import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Individual;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PopulationStatistics {
    
    private PopulationStatistics() {
        // Utility class
    }
    
    public static <T extends Individual<?>> Statistics calculate(Population<T> population) {
        if (population == null || population.getSize() == 0) {
            return new Statistics(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }
        
        double best = population.getBest().getFitness();
        double worst = population.getWorst().getFitness();
        
        // Collect all fitness values for average, std, and median
        List<Double> fitnessValues = new ArrayList<>();
        double sum = 0.0;
        for (T individual : population) {
            double fitness = individual.getFitness();
            fitnessValues.add(fitness);
            sum += fitness;
        }
        double avg = sum / population.getSize();
        
        // Calculate standard deviation
        double varianceSum = 0.0;
        for (double fitness : fitnessValues) {
            double diff = fitness - avg;
            varianceSum += diff * diff;
        }
        double variance = varianceSum / population.getSize();
        double std = Math.sqrt(variance);
        
        // Calculate median
        Collections.sort(fitnessValues);
        double median;
        int size = fitnessValues.size();
        if (size % 2 == 0) {
            median = (fitnessValues.get(size / 2 - 1) + fitnessValues.get(size / 2)) / 2.0;
        } else {
            median = fitnessValues.get(size / 2);
        }
        
        return new Statistics(best, worst, avg, std, median);
    }
    
    public record Statistics(double best, double worst, double avg, double std, double median) {}
}

