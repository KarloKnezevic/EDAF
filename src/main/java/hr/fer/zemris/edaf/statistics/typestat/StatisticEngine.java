package hr.fer.zemris.edaf.statistics.typestat;

import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.statistics.distributions.Distribution;
import hr.fer.zemris.edaf.statistics.reprezentation.IStatReprezentation;

import java.util.Random;

/**
 * StatisticEngine.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class StatisticEngine {

	protected int sampleLength;

	protected Random random;

	protected Distribution distribution;

	public StatisticEngine(Individual sample) {

		sampleLength = sample.getGenotypeLength();
		random = sample.getRand();
		distribution = new Distribution(random);

	}

	public void initializeDefault() {
	}

	public void initializeMuProb(double init) {
	}

	public Individual[] createPopulation() {
		return null;
	}

	public Individual createIndividual() {
		return null;
	}

	public Individual[] createPopulation(IStatReprezentation chain) {
		return null;
	}

	public void independentlyEstimateParams(Individual[] samples) {
	}

	public void independentlyEstimateUsingPrev(Individual[] samples) {
	}

	public IStatReprezentation mutualInformationMaximizing(Individual[] samples) {
		return null;
	}

	public IStatReprezentation bivariateMarginalDistribution(
			Individual[] samples) {
		return null;
	}

	public void estimatedModify() {
	}

	public double[] getMu() {
		return null;
	}

	public double[] getSigma() {
		return null;
	}

}
