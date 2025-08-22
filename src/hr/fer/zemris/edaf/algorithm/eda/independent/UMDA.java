package hr.fer.zemris.edaf.algorithm.eda.independent;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

import java.util.Arrays;
import java.util.Random;

/**
 * The Univariate Marginal Distribution Algorithm (UMDA).
 * <p>
 * In each iteration, the frequency functions on each position for the selected
 * set of promising solutions are computed, and these are then used to generate
 * new solutions. The new solutions replace the old ones, and the process is
 * repeated until the termination criteria are met.
 * <p>
 * From an implementation point of view, the UMDA matches the pseudo-code of a
 * typical EDA algorithm, except that the dependence graph of the constructed
 * probabilistic model contains no edges.
 *
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.1
 */
public class UMDA extends Algorithm {

    /**
     * The ratio of individuals to be used for parameter estimation.
     */
    private final double indEstimationRatio;

    /**
     * Constructs a new UMDA algorithm.
     *
     * @param rand               the random number generator
     * @param genotype           the genotype
     * @param selection          the selection operator
     * @param evaluation         the evaluation environment
     * @param maxGenerations     the maximum number of generations
     * @param stagnation         the stagnation limit
     * @param elitism            the number of elite individuals
     * @param estimationProbab the estimation probability
     */
    public UMDA(Random rand, Genotype genotype, Selection selection,
                Evaluation evaluation, int maxGenerations, int stagnation,
                int elitism, double estimationProbab) {

        super(rand, genotype, selection, evaluation, maxGenerations,
                stagnation, elitism);

        name = "umda";

        indEstimationRatio = estimationProbab;
    }

    @Override
    public void run() {
        population = genotype.getIndividual().createPopulation(true);
        evaluation.evaluate(population);

        int generation = 0;
        while ((generation < maxGenerations) && !stagnate()) {
            ++generation;
            runStep(population);
            getBest(population);
            pushData(population, generation);
        }
    }

    @Override
    public Individual[] runStep(Individual[] population) {
        Arrays.sort(population);

        // Estimate parameters from the selected individuals
        statistics.independentlyEstimateParams(selection
                .selectBetterIndividuals(population, indEstimationRatio));

        // Sample a new population from the probabilistic model
        final Individual[] sampled = statistics.createPopulation();

        evaluation.evaluate(sampled);

        // Insert the new individuals into the population
        insertIntoPopulation(population, sampled, indEstimationRatio);

        return population;
    }
}