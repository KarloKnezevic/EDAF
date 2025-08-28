package hr.fer.zemris.edaf.algorithm;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.statistics.Statistics;
import hr.fer.zemris.edaf.statistics.typestat.StatisticEngine;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

import java.util.Arrays;
import java.util.Observable;
import java.util.Random;

/**
 * Algorithm.
 * 
 * INFORMS (FRAMEWORK EXECUTOR) OF CHANGES.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */

public abstract class Algorithm extends Observable {

	protected String name;

	protected Random rand;

	protected Genotype genotype;

	protected Selection selection;

	protected Evaluation evaluation;

	protected StatisticEngine statistics;

	protected int maxGenerations;

	protected int stagnation;
	
	protected double destValue;

	protected int elitism;

	protected double mortality;

	protected Individual[] population;

	private Individual best;

	private int cntStagnation;

	private int logFreq;

	public Algorithm(Random rand, Genotype genotype, Selection selection,
			Evaluation evaluation, int maxGenerations, int stagnation,
			int elitism) {

		name = "undefined";
		this.rand = rand;
		this.genotype = genotype;
		this.selection = selection;
		this.evaluation = evaluation;
		statistics = new Statistics(genotype.getIndividual())
				.getStatisticEngine();
		this.maxGenerations = maxGenerations;
		this.stagnation = stagnation;
		this.elitism = elitism;
	}

	public Algorithm(Random rand, Genotype genotype, Selection selection,
			Evaluation evaluation, int maxGenerations, int stagnation,
			double mortality) {

		name = "undefined";
		this.rand = rand;
		this.genotype = genotype;
		this.selection = selection;
		this.evaluation = evaluation;
		this.maxGenerations = maxGenerations;
		this.stagnation = stagnation;
		this.mortality = mortality;
	}

	public abstract void run();

	public abstract Individual[] runStep(Individual[] population);

	protected Individual getBest(Individual[] population) {

		Individual best = null;

		for (int i = 0; i < population.length; i++) {
			if ((i == 0) || (best.getFitness() > population[i].getFitness())) {
				best = population[i];
			}
		}
		setBest(best);
		return best;
	}

	protected void setBest(Individual ind) {
		if (best == null) {
			best = ind;
			return;
		}

		if (best.getFitness() < ind.getFitness()) {
			++cntStagnation;
			return;
		}
		
		if (Math.abs(best.getFitness() - this.destValue) < 1e-5) {
			cntStagnation = stagnation;
			return;
		}

		cntStagnation = best.equals(ind) ? ++cntStagnation : 0;

		best = ind.copy();
	}

	protected Individual getBest() {
		return best;
	}

	protected boolean stagnate() {

		if (stagnation <= 0) {
			return false;
		}

		return cntStagnation >= stagnation ? true : false;
	}

	protected void insertIntoPopulation(Individual[] population,
			Individual[] sampled) {

		Arrays.sort(sampled);
		for (int i = elitism, j = 0; i < population.length; i++, j++) {
			population[i] = sampled[j];
		}

	}

	protected void insertIntoPopulation(Individual[] population,
			Individual[] sampled, double survivingPopulationRatio) {

		Arrays.sort(sampled);
		int survivingIndividuals = (int) Math.ceil(survivingPopulationRatio
				* population.length);

		survivingIndividuals = survivingIndividuals > elitism ? survivingIndividuals
				: elitism;

		for (int i = survivingIndividuals, j = 0; i < population.length; i++, j++) {
			population[i] = sampled[j];
		}

	}

	protected double computeReducingFactor(double start, double end) {
		if ((start == 0) || (maxGenerations == 0)) {
			return 1;
		}

		return Math.pow(10, Math.log10(end / start) / maxGenerations);
	}

	/**
	 * Print evaluations number. Eval = Gen * PopSize.
	 * 
	 * @param population
	 * @param generation
	 */
	protected void pushData(Individual[] population, int generation) {
		printer(generation,
				Statistics.stat2String(population, generation));
	}

	/**
	 * Print evaluations number. Eval = Gen * PopSize.
	 * 
	 * @param population
	 * @param generation
	 * @param opt
	 */
	protected void pushData(Individual[] population, int generation, String opt) {
		printer(generation,
				Statistics.stat2String(population, generation)
						+ String.format(" %-15s", opt));
	}

	private void printer(int generation, String print) {

		if (generation == 1) {
			MSGPrinter.printMESSAGE(System.out,
					Statistics.stat2StringHeadder(name.toUpperCase()));
		}

		if ((generation == 1) || (logFreq <= 0)
				|| ((generation % logFreq) == 0)
				|| (generation == (maxGenerations - 1)) || stagnate()) {

			MSGPrinter.printMESSAGE(System.out, print);

		}
	}

	public Genotype getGenotype() {
		return genotype;
	}

	public String getName() {
		return name;
	}

	public void setLogFreq(int logFreq) {
		this.logFreq = logFreq;
	}
	
	public void setDestValue(double destValue) {
		this.destValue = destValue;
	}

}