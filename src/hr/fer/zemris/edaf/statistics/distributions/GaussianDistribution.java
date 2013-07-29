package hr.fer.zemris.edaf.statistics.distributions;

import java.util.Random;

/**
 * Gaussian Distribution.
 * 
 * The normal distribution is also the only absolutely continuous distribution
 * all of whose cumulants beyond the first two (i.e. other than the mean and
 * variance) are zero. It is also the continuous distribution with the maximum
 * entropy for a given mean and variance.
 * 
 * The normal distribution is symmetric about its mean, and is non-zero over the
 * entire real line. As such it may not be a suitable model for variables that
 * are inherently positive or strongly skewed, such as the weight of a person or
 * the price of a share. Such variables may be better described by other
 * distributions, such as the log-normal distribution or the Pareto
 * distribution.
 * 
 * The normal distribution is also practically zero once the value x lies more
 * than a few standard deviations away from the mean. Therefore, it may not be
 * appropriate when one expects a significant fraction of outliers, values that
 * lie many standard deviations away from the mean. Least-squares and other
 * statistical inference methods which are optimal for normally distributed
 * variables often become highly unreliable. In those cases, one assumes a more
 * heavy-tailed distribution, and the appropriate robust statistical inference
 * methods.
 * 
 * The normal distributions are a subclass of the elliptical distributions.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class GaussianDistribution extends Distribution {

	private final int attempts = 3;

	public GaussianDistribution(Random rand) {
		super(rand);
	}

	public double getGaussian(double mean, double stdev) {
		return mean + (rand.nextGaussian() * stdev);
	}

	/**
	 * If the three attempts was not selected value from the interval, returns
	 * the value of the upper or lower limit, depending on the generated values.
	 * 
	 * @param mean
	 *            mu
	 * @param stdev
	 *            sigma
	 * @param xMin
	 *            lower boundry
	 * @param xMax
	 *            upper boundry
	 * @return selected value
	 */
	public double getGaussianLimited(double mean, double stdev, double xMin,
			double xMax) {

		double rnd = 0;

		for (int i = 0; i < attempts; i++) {
			rnd = getGaussian(mean, stdev);
			if ((rnd <= xMax) && (rnd >= xMin)) {
				return rnd;
			}
		}

		if (rnd > xMax) {
			return xMax;
		}

		if (rnd < xMin) {
			return xMin;
		}

		return rnd;
	}
}