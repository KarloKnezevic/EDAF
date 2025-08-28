package hr.fer.zemris.edaf.fitness.multimodal;

/**
 * ROSENBROCK'S FUNCTION f(x) = sum[i=1, n-1]{100*sqr( x[i+1] - sqr(x[i]) ) +
 * sqr(1-x[i])} D: -5, 10 Min: 0 Sol: [1]
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class RosenbrocksFunction implements IMultimodal {

	@Override
	public double computeMultimodal(double[] variable) {
		final int n = variable.length;
		double sum = 0;
		for (int i = 0; i < (n - 1); i++) {
			sum += (100 * Math.pow(variable[i + 1]
					- (variable[i] * variable[i]), 2))
					+ ((1 - variable[i]) * (1 - variable[i]));
		}
		return sum;
	}

}
