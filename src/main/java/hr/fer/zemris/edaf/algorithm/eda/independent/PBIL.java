package hr.fer.zemris.edaf.algorithm.eda.independent;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

import java.util.Arrays;
import java.util.Random;

/**
 * The Population Based Incremental Learning (PBIL) was introduced by Baluja.
 * The solutions are represented by binary or floating point strings of fixed
 * length, but the population of solutions is replaced by the so-called
 * probability vector, where pi refers to the probability of obtaining a value
 * of 1 in the i-th gene.
 * 
 * This vector is initially set to assign each value at the same position with
 * the same probability pi = 0.5. At each generation a number of solutions is
 * generated using this vector. Then the few best solutions are selected and the
 * probability vector is shifted towards the selected solutions by using Hebbian
 * learning rule.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class PBIL extends Algorithm {

	private final double indEstimationRatio;

	public PBIL(Random rand, Genotype genotype, Selection selection,
			Evaluation evaluation, int maxGenerations, int stagnation,
			int elitism, double estimationProbab) {

		super(rand, genotype, selection, evaluation, maxGenerations,
				stagnation, elitism);

		name = "pbil";

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

		statistics.independentlyEstimateUsingPrev(selection
				.selectBetterIndividuals(population, indEstimationRatio));

		statistics.estimatedModify();

		final Individual[] sampled = statistics.createPopulation();

		evaluation.evaluate(sampled);

		insertIntoPopulation(population, sampled, indEstimationRatio);

		return population;
	}

}