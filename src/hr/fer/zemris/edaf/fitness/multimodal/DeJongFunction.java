package hr.fer.zemris.edaf.fitness.multimodal;

/**
 * DE JONG'S FUNCTION f(x) = sum[i=1,n]{sqr(x[i])} D: -5.12, 5.12 Min: 0 Sol:
 * [0]
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class DeJongFunction implements IMultimodal {

	@Override
	public double computeMultimodal(double[] variable) {
		double solution = 0;

		for (int i = 0; i < variable.length; i++) {
			solution += variable[i];
		}

		return solution;
	}

}
