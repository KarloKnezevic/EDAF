package hr.fer.zemris.edaf.fitness.multimodal;

/**
 * SCHWEFEL'S FUNCTION f(x) = sum[i=1, n]{-x[i]*sin( sqrt( abs(x[i]) ) )} D:
 * -500, 500 Min: -418.9829 Sol: [420.9687]
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class SchwefelsFunction implements IMultimodal {

	@Override
	public double computeMultimodal(double[] variable) {

		final int dimension = variable.length;
		double value = 0;
		for (int i = 0; i < dimension; i++) {
			value += -variable[i] * Math.sin(Math.sqrt(Math.abs(variable[i])));
		}

		return value / dimension;
	}

}
