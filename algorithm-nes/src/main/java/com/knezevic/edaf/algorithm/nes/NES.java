package com.knezevic.edaf.algorithm.nes;

import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.AbstractAlgorithm;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.fp.FpIndividual;

/**
 * Separable Natural Evolution Strategies (SNES).
 * <p>
 * NES is a family of black-box optimization algorithms that maintain a parametric
 * search distribution (multivariate Gaussian) and update its parameters using the
 * natural gradient of expected fitness. This implementation uses the separable variant
 * (SNES) which maintains a diagonal covariance matrix, making it O(n) in both space
 * and time per generation.
 * </p>
 * <p>
 * Each generation:
 * <ol>
 *     <li>Sample lambda individuals from N(mu, diag(sigma^2))</li>
 *     <li>Evaluate fitness of all samples</li>
 *     <li>Apply rank-based fitness shaping for invariance to monotone transformations</li>
 *     <li>Compute natural gradient and update mu and sigma</li>
 * </ol>
 * </p>
 *
 * @see <a href="https://arxiv.org/abs/1106.4487">Wierstra et al., "Natural Evolution Strategies"</a>
 */
public class NES extends AbstractAlgorithm<FpIndividual> {

    private final TerminationCondition<FpIndividual> terminationCondition;
    private final NesStatistics nesStats;
    private final int populationSize;

    private Population<FpIndividual> population;

    public NES(Problem<FpIndividual> problem,
               TerminationCondition<FpIndividual> terminationCondition,
               NesStatistics nesStats,
               int populationSize) {
        super(problem, "nes");
        this.terminationCondition = terminationCondition;
        this.nesStats = nesStats;
        this.populationSize = populationSize;
    }

    @Override
    public void run() {
        publishAlgorithmStarted();
        setGeneration(0);

        while (!terminationCondition.shouldTerminate(this)) {
            // 1. Sample population from current distribution N(mu, diag(sigma^2))
            double[][] samples = nesStats.sampleRaw(populationSize);

            // 2. Build population of FpIndividuals
            population = new SimplePopulation<>(problem.getOptimizationType());
            for (double[] s : samples) {
                population.add(new FpIndividual(s));
            }

            // 3. Evaluate all individuals
            long e0 = System.nanoTime();
            evaluatePopulation(population);
            long e1 = System.nanoTime();
            publishEvaluationCompleted(getGeneration(), population.getSize(), e1 - e0);

            // 4. Rank-based fitness shaping
            double[] utilities = FitnessShaping.computeUtilities(population, problem.getOptimizationType());

            // 5. Natural gradient update of mu and sigma
            nesStats.update(samples, utilities);

            // 6. Track best individual
            population.sort();
            FpIndividual currentBest = population.getBest();
            updateBestIfBetter(currentBest);

            incrementGeneration();
            notifyListener();
            publishGenerationCompleted();
        }
        publishAlgorithmTerminated();
    }

    @Override
    public Population<FpIndividual> getPopulation() {
        return population;
    }
}
