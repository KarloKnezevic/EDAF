package hr.fer.zemris.edaf.genotype.floatingpoint;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.statistics.distributions.Distribution;

import java.util.Random;

/**
 * Floating point representation (FPR) is of the strongpoint of high precision
 * and facilitating search on high-dimension space. It is superior to other
 * representation in function optimization and restriction optimization.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class FloatingPoint extends Individual {

	private double xMin;

	private double xMax;

	public FloatingPoint(int variableNumber, double xMin, double xMax,
			Random rand, int populationLen) {

		this.xMin = xMin;
		this.xMax = xMax;
		fitness = 0;
		variable = new double[variableNumber];
		this.rand = rand;
		this.populationLen = populationLen;

	}

	public double getxMin() {
		return xMin;
	}

	public double getxMax() {
		return xMax;
	}

	@Override
	public Individual copy() {

		final FloatingPoint copied = new FloatingPoint(variable.length, xMin,
				xMax, rand, populationLen);

		System.arraycopy(variable, 0, copied.variable, 0, variable.length);

		copied.fitness = fitness;

		return copied;
	}

	@Override
	public int getGenotypeLength() {
		return variable.length;
	}

	@Override
	public Individual[] createPopulation(boolean init) {

		final FloatingPoint[] individuals = new FloatingPoint[populationLen];

		for (int i = 0; i < populationLen; i++) {
			individuals[i] = new FloatingPoint(variable.length, xMin, xMax,
					rand, populationLen);

			if (init) {
				initIndividual(individuals[i]);
			}
		}

		return individuals;

	}

	public Individual[] createPopulation(double[] muValues,
			double[] sigmaValues, Distribution gauss) {

		final FloatingPoint[] individuals = new FloatingPoint[populationLen];

		for (int i = 0; i < populationLen; i++) {
			individuals[i] = new FloatingPoint(variable.length, xMin, xMax,
					rand, populationLen);

			initIndividual(individuals[i], muValues, sigmaValues, gauss);
		}

		return individuals;

	}

	public Individual createIndividual(double[] muValues, double[] sigmaValues,
			Distribution gauss) {

		final FloatingPoint fp = new FloatingPoint(variable.length, xMin, xMax,
				rand, populationLen);
		initIndividual(fp, muValues, sigmaValues, gauss);
		return fp;
	}

	private void initIndividual(FloatingPoint individual, double[] muValues,
			double[] sigmaValues, Distribution gauss) {

		for (int i = 0; i < variable.length; i++) {
			individual.variable[i] = gauss
					.getGaussianDistribution()
					.getGaussianLimited(muValues[i], sigmaValues[i], xMin, xMax);

		}

	}

	private void initIndividual(FloatingPoint individual) {

		for (int i = 0; i < variable.length; i++) {
			individual.variable[i] = xMin + ((xMax - xMin) * rand.nextDouble());
		}

	}

	@Override
	public double[] getVariable() {
		return variable;
	}

	@Override
	public void copy(Individual copy) {

		if (!(copy instanceof FloatingPoint)) {
			MSGPrinter
					.printERROR(
							System.err,
							"Can not non floating point individual copy into floating point.",
							true, -1);
		}

		final FloatingPoint fp = (FloatingPoint) copy;

		System.arraycopy(copy.getVariable(), 0, variable, 0, variable.length);

		xMin = fp.xMin;

		xMax = fp.xMax;

	}

	@Override
	public int compareGenotype(Individual ind, int index) {
		final FloatingPoint fp = (FloatingPoint) ind;

		if (variable[index] < fp.variable[index]) {
			return -1;
		}

		if (variable[index] > fp.variable[index]) {
			return 1;
		}

		return 0;
	}

}