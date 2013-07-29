package hr.fer.zemris.edaf.fitness.tfon;

/**
 * Test Functions For Optimization Needs Functions author: Marcin Molga, Czesław
 * Smutnicki, 3 kwietnia 2005 Paper description: This paper provides the review
 * of literature benchmarks (test functions) commonly used in order to test
 * optimization procedures dedicated for multidimensional, continuous
 * optimization task. Special attention has been paid to multiple-extreme
 * functions, treated as the quality test for “resistant” optimization methods
 * (GA, SA, TS, etc.).
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TestFunctionsForOptimizationNeeds {

	/**
	 * DE JONG'S FUNCTION f(x) = sum[i=1,n]{sqr(x[i])} D: -5.12, 5.12 Min: 0
	 * Sol: [0]
	 * 
	 * @param variable
	 * @return res
	 */
	public double DeJong(double[] variable) {
		double res = 0;
		final int dim = variable.length;
		for (int i = 0; i < dim; i++) {
			res += variable[i] * variable[i];
		}
		return res;
	}

	/**
	 * AXIS PARALLEL HYPER-ELLIPSOID FUNCTION f(x) = sum[i=1,n]{i*sqr(x[i])} D:
	 * -5.12, 5.12 Min: 0 Sol: [0]
	 * 
	 * @param variable
	 * @return res
	 */
	public double AxisParallelHyperEllipsoid(double[] variable) {
		double res = 0;
		final int dim = variable.length;
		for (int i = 0; i < dim; i++) {
			res += (i + 1) * variable[i] * variable[i];
		}
		return res;
	}

	/**
	 * ROTATED HYPER-ELLIPSOID FUNCTION f(x) = sum[i=1,n]{ sum[j,i]{sqr(x[i])} }
	 * D: -65536, 65536 Min: 0 Sol: [0]
	 * 
	 * @param variable
	 * @return res
	 */
	public double RotatedHyperEllipsoid(double[] variable) {
		double res = 0;
		final int dim = variable.length;
		for (int i = 0; i < dim; i++) {
			for (int j = 0; j <= i; j++) {
				res += variable[i] * variable[i];
			}
		}
		return res;
	}

	/**
	 * ROSENBROCK'S VALLEY f(x) = sum[i=1, n-1]{100*sqr(x[i+1] - sqr(x[i])) +
	 * sqr(1 - x[i])} D: -2048, 2048 Min: 0 Sol: [0]
	 * 
	 * @param variable
	 * @return res
	 */
	public double RosenbrocksValley(double[] variable) {
		double res = 0;
		final int dim = variable.length;
		for (int i = 0; i < (dim - 1); i++) {
			res += (100 * (variable[i + 1] - (variable[i] * variable[i])) * (variable[i + 1] - (variable[i] * variable[i])))
					+ ((1 - variable[i]) * (1 - variable[i]));
		}
		return res;
	}

	/**
	 * RASTRIGIN'S FUNCTION f(x) = 10*n + sum[i=1, n]{sqr(x[i]) -
	 * 10*cos(2*PI*x[i])} D: -5.12, 5.12 Min: 0 Sol: [0]
	 * 
	 * @param variable
	 * @return res
	 */
	public double Rastrigin(double[] variable) {
		double res = 0;
		final int dim = variable.length;

		res += 10 * dim;
		for (int i = 0; i < dim; i++) {
			res += (variable[i] * variable[i])
					- (10 * Math.cos(2 * Math.PI * variable[i]));
		}
		return res;
	}

	/**
	 * SCHWEFEL'S FUNCTION f(x) = sum[i=1, n]{-x[i]*sin( sqrt( abs(x[i]) ) )} D:
	 * -500, 500 Min: -418.9829 Sol: [420.9687]
	 * 
	 * @param variable
	 * @return res
	 */
	public double Schwefel(double[] variable) {
		double res = 0;
		final int dim = variable.length;
		for (int i = 0; i < dim; i++) {
			res += -variable[i] * Math.sin(Math.sqrt(Math.abs(variable[i])));
		}
		return res;
	}

	/**
	 * GRIEWANGK'S FUNCTION f(x) = (1/4000)sum[i=1,n]{sqr(x[i])} -
	 * prod[i=1,n]{cos(x[i]/sqrt(i))} + 1 D: -600, 600 Min: 0 Sol: [0]
	 * 
	 * @param variable
	 * @return res
	 */
	public double Griewangk(double[] variable) {
		double sum = 0;
		double product = 1;
		final int dim = variable.length;

		for (int i = 0; i < dim; i++) {
			sum += variable[i] * variable[i];
			product *= Math.cos(variable[i] / Math.sqrt(i + 1));
		}

		return (1 + ((1.0 / 4000.0) * sum)) - product;

	}

	/**
	 * SUM OF DIFFERENT POWER FUNCTIONS f(x) = sum[i=1,n]{pow(x[i],i+1)} D: -1,
	 * 1 Min: 0 Sol: [0]
	 * 
	 * @param variable
	 * @return res
	 */
	public double SumOfDifferentPower(double[] variable) {
		double res = 0;
		final int dim = variable.length;

		for (int i = 0; i < dim; i++) {
			res += (i % 2) == 0 ? variable[i] * variable[i] : Math
					.abs(variable[i] * variable[i] * variable[i]);
		}
		return res;
	}

	/**
	 * ACKLEY'S FUNCTION f(x) = -a*exp(-b * sqrt(1/n * sum[i=1,n]{sqr(x[i])}) -
	 * exp(1/n * sum[i=1,n]{cos(c*x[i])}) + a + e D: -32768, 32768 Min: 0 Sol:
	 * [0]
	 * 
	 * @param variable
	 * @return res
	 */
	public double Ackley(double[] variable) {
		final int dim = variable.length;

		final double a = 20;
		final double b = 0.2;
		final double c = 2 * Math.PI;

		double sum1 = 0;
		double sum2 = 0;
		for (int i = 0; i < dim; i++) {
			sum1 += variable[i] * variable[i];
			sum2 += Math.cos(c * variable[i]);
		}

		sum1 = -b * Math.sqrt((1.0 / dim) * sum1);
		sum2 = (1.0 / dim) * sum2;

		return ((-a * Math.exp(sum1)) - Math.exp(sum2)) + a + Math.E;
	}

	/**
	 * MICHALEWICZ'S FUNCTION f(x) = -sum[i=1,n]{ sin(x[i])* pow(
	 * sin(i*sqr(x[i])/PI) , 2*m) } D: 0, PI Min: Respective optimal solutions
	 * are not given Sol: Respective optimal solutions are not given
	 * 
	 * @param variable
	 * @return res
	 */
	public double Michalewicz(double[] variable) {
		double res = 0;
		final int dim = variable.length;

		final int m = 10;

		for (int i = 0; i < dim; i++) {
			res += Math.sin(variable[i]
					* Math.pow(
							Math.sin(((i + 1) * variable[i] * variable[i])
									/ Math.PI), 2 * m));
		}
		return -res;
	}

	/**
	 * DECEPTIVE FUNCTIONS
	 * 
	 * @param alpha
	 *            type of deceptive function: TYPE I (alpha = 1, g(1,x,0)), TYPE
	 *            II (alpha = rand(0,1)), TYPE III (alpha = 1, g(1,x,i))
	 * @param x
	 * @param i
	 * @return
	 */
	/**
	 * DECEPTIVE FUNCTIONS f(x) = -pow( 1/n * sum[i=1,n]{g_i(x[i])}, beta ) D:
	 * 0, 1 Min: Depends of TYPE of deceptive function Sol: Respective optimal
	 * solutions are not given DEFAULT: TYPE III
	 * 
	 * @param variable
	 * @return res
	 */
	public double Deceptive(double[] variable) {
		double res = 0;
		final int dim = variable.length;

		final double alpha = 1.0;
		final double beta = 2.5;

		for (int i = 0; i < dim; i++) {
			res += g(alpha, variable[i], i);
		}

		return -Math.pow((1.0 / dim) * res, beta);
	}

	/**
	 * DECEPTIVE TRANSFORM FUNCTION
	 * 
	 * @param alpha
	 *            alpha type of deceptive function: TYPE I (alpha = 1,
	 *            g(1,x,0)), TYPE II (alpha = rand(0,1)), TYPE III (alpha = 1,
	 *            g(1,x,i))
	 * @param x
	 *            variable
	 * @param i
	 *            index
	 * @return transformation
	 */
	private double g(double alpha, double x, int i) {
		final double alphaI = alpha / (i + 1);

		if ((0 <= x) && (x <= ((4.0 / 5.0) * alphaI))) {
			return (-x / alphaI) + (4.0 / 5.0);
		} else if ((((4.0 / 5.0) * alphaI) <= x) && (x <= alphaI)) {
			return ((5 * x) / alphaI) - 4;
		} else if ((alphaI <= x) && (x <= ((1 + (4 * alphaI)) / 5.0))) {
			return ((5 * (x - alphaI)) / (alphaI - 1)) + 1;
		}

		return ((x - 1) / (1 - alphaI)) + (4.0 / 5.0);
	}

}
