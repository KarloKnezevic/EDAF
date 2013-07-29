package hr.fer.zemris.edaf.fitness.multimodal;

/**
 * RASTRIGIN'S FUNCTION f(x) = 10*n + sum[i=1, n]{sqr(x[i]) - 10*cos(2*PI*x[i])}
 * D: -5.12, 5.12 Min: 0 Sol: [0]
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class RastriginsFunction implements IMultimodal {

	@Override
	public double computeMultimodal(double[] variable) {

		final int dimension = variable.length;
		double value = 10 * dimension;

		for (int i = 0; i < dimension; i++) {
			value += (variable[i] * variable[i])
					- (10 * Math.cos(2 * Math.PI * variable[i]));
		}

		return value;

	}

}
