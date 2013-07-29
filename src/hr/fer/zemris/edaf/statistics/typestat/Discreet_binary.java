package hr.fer.zemris.edaf.statistics.typestat;

import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.Binary;
import hr.fer.zemris.edaf.statistics.reprezentation.IStatReprezentation;
import hr.fer.zemris.edaf.statistics.reprezentation.binary.BinaryReprezentation;

import java.util.Arrays;

/**
 * Discreet binary statistics.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Discreet_binary extends StatisticEngine {

	private final double[] sampleProbabilities;

	private final Binary binarySample;

	private double smoothing;

	private double alpha;

	private double modify;

	public Discreet_binary(Individual sample) {
		super(sample);

		binarySample = (Binary) sample;

		sampleProbabilities = new double[sampleLength];

		initializeDefault();
	}

	@Override
	public void initializeDefault() {
		Arrays.fill(sampleProbabilities, 0.5);
	}

	@Override
	public void initializeMuProb(double init) {
		Arrays.fill(sampleProbabilities, init);
	}

	@Override
	public Individual[] createPopulation() {
		return binarySample.createPopulation(sampleProbabilities);
	}

	@Override
	public Individual[] createPopulation(IStatReprezentation condProbabTo1) {
		return binarySample.createPopulation(condProbabTo1);
	}

	@Override
	public Individual createIndividual() {
		return binarySample.createIndividual(sampleProbabilities);
	}

	@Override
	public double[] getMu() {
		return sampleProbabilities;
	}

	@Override
	public void independentlyEstimateParams(Individual[] samples) {
		initializeMuProb(0);

		smoothing = 0.5;

		for (int i = 0; i < samples.length; i++) {
			final Binary b = (Binary) samples[i];
			final byte[] barray = b.getBits();
			for (int j = 0; j < sampleLength; j++) {
				sampleProbabilities[j] += barray[j];
			}
		}

		final double r = 2 * smoothing;
		for (int i = 0; i < sampleLength; i++) {
			sampleProbabilities[i] = (sampleProbabilities[i] + smoothing)
					/ (samples.length + r);
		}
	}

	@Override
	public void independentlyEstimateUsingPrev(Individual[] samples) {

		alpha = 0.2;

		final double[] sampleProbabilitiesPrev = new double[sampleLength];
		System.arraycopy(sampleProbabilities, 0, sampleProbabilitiesPrev, 0,
				sampleLength);

		independentlyEstimateParams(samples);

		for (int i = 0; i < sampleLength; i++) {
			sampleProbabilities[i] = ((1 - alpha) * sampleProbabilitiesPrev[i])
					+ (alpha * sampleProbabilities[i]);
		}
	}

	@Override
	public void estimatedModify() {

		modify = 1E-2;

		for (int i = 0; i < sampleLength; i++) {
			sampleProbabilities[i] = (sampleProbabilities[i] * (1 - modify))
					+ ((random.nextBoolean() ? 1.0 : 0.0) * modify);
		}
	}

	@Override
	public IStatReprezentation mutualInformationMaximizing(Individual[] samples) {
		independentlyEstimateParams(samples);
		return new BinaryReprezentation(sampleProbabilities, samples)
				.getChain();
	}

	@Override
	public IStatReprezentation bivariateMarginalDistribution(
			Individual[] samples) {
		independentlyEstimateParams(samples);
		return new BinaryReprezentation(sampleProbabilities, samples).getTree();
	}
}