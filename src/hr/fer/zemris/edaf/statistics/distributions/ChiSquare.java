package hr.fer.zemris.edaf.statistics.distributions;

/**
 * The chi-squared distribution is used in the common chi-squared tests for
 * goodness of fit of an observed distribution to a theoretical one, the
 * independence of two criteria of classification of qualitative data, and in
 * confidence interval estimation for a population standard deviation of a
 * normal distribution from a sample standard deviation. Many other statistical
 * tests also use this distribution, like Friedman's analysis of variance by
 * ranks.
 * 
 * The chi-squared distribution is a special case of the gamma distribution.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class ChiSquare {

	/*
	 * Credibility values: 50% = 0.455 70% = 1.074 75% = 1.323 80% = 1.642 90% =
	 * 2.706 95% = 3.841 97.5% = 5.024 98% = 5.412 99% = 6.635
	 */
	private final double credibilityValue = 2.706;

	private final int M;

	public ChiSquare(int M) {
		this.M = M;
	}

	public boolean isDependent(double pxi, double pxj, double pxixj) {
		if ((pxi == 0) || (pxj == 0)) {
			return false;
		}

		final double chiSquare = (M * (pxixj - (pxi * pxj)) * (pxixj - (pxi * pxj)))
				/ (pxi * pxj);

		return chiSquare < credibilityValue ? false : true;
	}

}
