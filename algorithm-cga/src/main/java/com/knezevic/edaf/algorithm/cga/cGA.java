package com.knezevic.edaf.algorithm.cga;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.AbstractAlgorithm;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;

import java.util.Random;

/**
 * The Compact Genetic Algorithm (cGA).
 * <p>
 * cGA is a type of Estimation of Distribution Algorithm (EDA) that simulates the behavior of a simple genetic algorithm
 * with a large population, but without storing the population itself. Instead, it maintains a probability vector
 * representing the distribution of genes in the population.
 * </p>
 * <p>
 * The algorithm works as follows:
 * <ol>
 *     <li>Initialize a probability vector `p` of length `l` (where `l` is the genotype length) with all values set to 0.5.</li>
 *     <li>In each generation, generate two individuals by sampling from the probability vector.</li>
 *     <li>Evaluate the two individuals and identify the winner (the one with better fitness).</li>
 *     <li>Update the probability vector based on the winner and loser. For each gene where the winner and loser differ,
 *         the probability is shifted towards the winner's gene value by a small amount (1/n, where n is the virtual population size).</li>
 *     <li>Repeat until a termination condition is met.</li>
 * </ol>
 * </p>
 * <p>
 * This implementation is designed to work with {@link BinaryIndividual} genotypes.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Compact_genetic_algorithm">Compact genetic algorithm on Wikipedia</a>
 */
public class cGA extends AbstractAlgorithm<BinaryIndividual> {

    private final TerminationCondition<BinaryIndividual> terminationCondition;
    private final int n; // population size for update
    private final int length;
    private final Random random;
    private final double[] p;

    public cGA(Problem<BinaryIndividual> problem, TerminationCondition<BinaryIndividual> terminationCondition,
               int n, int length, Random random) {
        super(problem, "cga");
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
        publishAlgorithmStarted();
        setGeneration(0);
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
            long e0 = System.nanoTime();
            problem.evaluate(individual1);
            problem.evaluate(individual2);
            long e1 = System.nanoTime();
            publishEvaluationCompleted(getGeneration(), 2, e1 - e0);

            // 3. Update probability vector
            BinaryIndividual winner, loser;
            if (isFirstBetter(individual1, individual2)) {
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
                    p[i] = Math.max(0.0, Math.min(1.0, p[i]));
                }
            }

            // 4. Update best
            updateBestIfBetter(winner);

            incrementGeneration();
            notifyListener(null);
            publishGenerationCompleted(getBest().getFitness(), winner.getFitness(),
                (individual1.getFitness() + individual2.getFitness()) / 2.0,
                Double.NaN);
        }
        publishAlgorithmTerminated();
    }

    @Override
    public Population<BinaryIndividual> getPopulation() {
        // cGA does not maintain a population, so this method returns null.
        return null;
    }
}
