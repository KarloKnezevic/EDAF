package hr.fer.zemris.edaf.fitness.multimodal;

/**
 * GRIEWANGK'S FUNCTION f(x) = (1/4000)sum[i=1,n]{sqr(x[i])} -
 * prod[i=1,n]{cos(x[i]/sqrt(i))} + 1 D: -600, 600 Min: 0 Sol: [0]
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class GriewanksFunction implements IMultimodal {

	@Override
	public double computeMultimodal(double[] variable) {
		double sum = 0;
		double product = 1;

		for (int i = 0; i < variable.length; i++) {
			sum += variable[i] * variable[i];
			product *= Math.cos(variable[i] / Math.sqrt(i + 1));
		}

		return (1 + ((1.0 / 4000.0) * sum)) - product;
	}

}
