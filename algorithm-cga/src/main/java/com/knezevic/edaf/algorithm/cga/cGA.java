package com.knezevic.edaf.algorithm.cga;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Random;

/**
 * The Compact Genetic Algorithm (cGA).
 */
public class cGA implements Algorithm<BinaryIndividual> {

    private final Problem<BinaryIndividual> problem;
    private final TerminationCondition<BinaryIndividual> terminationCondition;
    private final int n; // population size for update
    private final int length;
    private final Random random;
    private final double[] p;

    private BinaryIndividual best;
    private int generation;
    private ProgressListener listener;

    public cGA(Problem<BinaryIndividual> problem, TerminationCondition<BinaryIndividual> terminationCondition,
               int n, int length, Random random) {
        this.problem = problem;
        this.terminationCondition = terminationCondition;
        this.n = n;
        this.length = length;
        this.random = random;
        this.p = new double[length];
        for (int i = 0; i < length; i++) {
            p[i] = 0.5;
        }
    }

    @Override
    public void run() {
        generation = 0;
        while (!terminationCondition.shouldTerminate(this)) {
            // 1. Generate two individuals
            byte[] genotype1 = new byte[length];
            byte[] genotype2 = new byte[length];
            for (int i = 0; i < length; i++) {
                genotype1[i] = random.nextDouble() < p[i] ? (byte) 1 : (byte) 0;
                genotype2[i] = random.nextDouble() < p[i] ? (byte) 1 : (byte) 0;
            }
            BinaryIndividual individual1 = new BinaryIndividual(genotype1);
            BinaryIndividual individual2 = new BinaryIndividual(genotype2);

            // 2. Evaluate them
            problem.evaluate(individual1);
            problem.evaluate(individual2);

            // 3. Update probability vector
            BinaryIndividual winner, loser;
            if (individual1.getFitness() < individual2.getFitness()) {
                winner = individual1;
                loser = individual2;
            } else {
                winner = individual2;
                loser = individual1;
            }

            for (int i = 0; i < length; i++) {
                if (winner.getGenotype()[i] != loser.getGenotype()[i]) {
                    if (winner.getGenotype()[i] == 1) {
                        p[i] += 1.0 / n;
                    } else {
                        p[i] -= 1.0 / n;
                    }
                }
            }

            // 4. Update best
            if (best == null || winner.getFitness() < best.getFitness()) {
                best = (BinaryIndividual) winner.copy();
            }

            generation++;
            if (listener != null) {
                listener.onGenerationDone(generation, best, null);
            }
        }
    }

    @Override
    public BinaryIndividual getBest() {
        return best;
    }

    @Override
    public int getGeneration() {
        return generation;
    }

    @Override
    public Population<BinaryIndividual> getPopulation() {
        // cGA does not maintain a population
        return null;
    }

    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
}
