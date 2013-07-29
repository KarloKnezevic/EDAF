package hr.fer.zemris.edaf.statistics.typestat;

import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.floatingpoint.FloatingPoint;

import java.util.Arrays;

/**
 * Continuous statistics.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Continuous extends StatisticEngine {

	private final double[] muValues;

	private final double[] sigmaValues;

	private double alpha;

	private double beta;

	private double modify;

	private final FloatingPoint floatingPointSample;

	public Continuous(Individual sample) {
		super(sample);

		floatingPointSample = (FloatingPoint) sample;

		muValues = new double[sampleLength];
		sigmaValues = new double[sampleLength];

		initializeDefault();
	}

	@Override
	public void initializeDefault() {
		final double mean = (floatingPointSample.getxMax() + floatingPointSample
				.getxMin()) / 2.0;
		final double dev = floatingPointSample.getxMax()
				- floatingPointSample.getxMin();

		for (int i = 0; i < muValues.length; i++) {
			muValues[i] = mean + (dev * random.nextDouble());
			sigmaValues[i] = (dev / 2.0) + ((dev / 4.0) * random.nextDouble());
		}
	}

	@Override
	public void initializeMuProb(double init) {
		Arrays.fill(muValues, init);
	}

	public void initializeSigmaProb(double init) {
		Arrays.fill(sigmaValues, init);
	}

	@Override
	public Individual[] createPopulation() {
		return floatingPointSample.createPopulation(muValues, sigmaValues,
				distribution.getGaussianDistribution());
	}

	@Override
	public Individual createIndividual() {
		return floatingPointSample.createIndividual(muValues, sigmaValues,
				distribution.getGaussianDistribution());
	}

	@Override
	public double[] getMu() {
		return muValues;
	}

	@Override
	public double[] getSigma() {
		return sigmaValues;
	}

	@Override
	public void independentlyEstimateParams(Individual[] samples) {
		initializeMuProb(0);
		initializeSigmaProb(0);

		alpha = 1;
		beta = 1;

		for (int i = 0; i < samples.length; i++) {
			final double[] fparray = samples[i].getVariable();
			for (int j = 0; j < sampleLength; j++) {
				muValues[j] += fparray[j];
			}
		}

		for (int i = 0; i < sampleLength; i++) {
			muValues[i] /= samples.length;
		}

		for (int i = 0; i < samples.length; i++) {
			final double[] fparray = samples[i].getVariable();
			for (int j = 0; j < sampleLength; j++) {
				sigmaValues[j] += (fparray[j] - muValues[j])
						* (fparray[j] - muValues[j]);
			}
		}

		for (int i = 0; i < sampleLength; i++) {
			sigmaValues[i] /= (samples.length - 1);
			sigmaValues[i] = Math.sqrt(sigmaValues[i]);

			muValues[i] *= alpha;
			sigmaValues[i] *= beta;
		}

	}

	@Override
	public void independentlyEstimateUsingPrev(Individual[] samples) {

		alpha = 0.2;
		beta = 0.2;

		final double[] muValuesPrev = new double[sampleLength];
		final double[] sigmaValuesPrev = new double[sampleLength];
		System.arraycopy(muValues, 0, muValuesPrev, 0, sampleLength);
		System.arraycopy(sigmaValues, 0, sigmaValuesPrev, 0, sampleLength);

		independentlyEstimateParams(samples);

		for (int i = 0; i < sampleLength; i++) {
			muValues[i] = ((1 - alpha) * muValuesPrev[i])
					+ (alpha * muValues[i]);
			sigmaValues[i] = ((1 - beta) * sigmaValuesPrev[i])
					+ (beta * sigmaValues[i]);
		}
	}

	@Override
	public void estimatedModify() {

		modify = 1E-2;

		for (int i = 0; i < sampleLength; i++) {
			muValues[i] = (muValues[i] * (1 - modify))
					+ ((random.nextBoolean() ? muValues[i] : 0.0) * modify);
			sigmaValues[i] = (sigmaValues[i] * (1 - modify))
					+ ((random.nextBoolean() ? sigmaValues[i] : 0.0) * modify);
		}
	}

}