package com.knezevic.edaf.algorithm.pbil;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;

/**
 * Population-Based Incremental Learning (PBIL).
 *
 * @param <T> The type of individual in the population.
 */
public class Pbil<T extends Individual> implements Algorithm<T> {

    private final Problem<T> problem;
    private final Statistics<T> statistics;
    private final TerminationCondition<T> terminationCondition;
    private final int populationSize;
    private final double learningRate;

    private T best;
    private int generation;
    private Population<T> population;
    private ProgressListener listener;

    public Pbil(Problem<T> problem, Statistics<T> statistics,
                TerminationCondition<T> terminationCondition, int populationSize,
                double learningRate) {
        this.problem = problem;
        this.statistics = statistics;
        this.terminationCondition = terminationCondition;
        this.populationSize = populationSize;
        this.learningRate = learningRate;
    }

    @Override
    public void run() {
        // 1. Initialize probability vector (done in statistics impl)
        generation = 0;

        // 2. Run generations
        while (!terminationCondition.shouldTerminate(this)) {
            // 2.1. Sample a population
            population = statistics.sample(populationSize);

            // 2.2. Evaluate the population
            for (T individual : population) {
                problem.evaluate(individual);
            }
            population.sort();

            // 2.3. Get the best individual
            T currentBest = population.getBest();

            // 2.4. Update the probability vector
            statistics.update(currentBest, learningRate);

            // 2.5. Update the best-so-far individual
            if (best == null || currentBest.getFitness() < best.getFitness()) {
                best = (T) currentBest.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, population.getBest(), population);
            }
        }
    }

    @Override
    public T getBest() {
        return best;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public Population<T> getPopulation() {
        return population;
    }

    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
}
