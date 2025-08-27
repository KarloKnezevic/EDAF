package hr.fer.zemris.edaf.fitness.multimodal;

/**
 * ROSENBROCK'S VALLEY f(x) = sum[i=1, n-1]{100*sqr(x[i+1] - sqr(x[i])) + sqr(1
 * - x[i])} D: -2048, 2048 Min: 0 Sol: [0]
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class RosenbrocksValleyFunction implements IMultimodal {

	@Override
	public double computeMultimodal(double[] variable) {
		double solution = 0;
		for (int i = 0; i < (variable.length - 1); i++) {
			solution += 100 * (variable[i + 1] - (variable[i] * variable[i]))
					* (variable[i + 1] - (variable[i] * variable[i]));
			solution += (1 - variable[i]) * (1 - variable[i]);
		}
		return solution;
	}

}
