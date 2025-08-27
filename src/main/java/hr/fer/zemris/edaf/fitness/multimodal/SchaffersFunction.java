package hr.fer.zemris.edaf.fitness.multimodal;

/**
 * SCHAFFER'S FUNCTION f(x) = 0.5 + ( sqr( sin( sqrt( sum[i=1,n] {sqr(x[i])} ) )
 * ) - 0.5 ) / sqr( 1+0.001*sum[i=1,n] {sqr(x[i])} ) D: -50, 150 Min: 0 Sol: [0]
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class SchaffersFunction implements IMultimodal {

	@Override
	public double computeMultimodal(double[] variable) {
		final int n = variable.length;

		double sum = 0;
		for (int i = 0; i < n; i++) {
			sum += variable[i] * variable[i];
		}
		final double result = 0.5 + ((Math.pow(Math.sin(Math.sqrt(sum)), 2) - 0.5) / Math
				.pow((1 + (0.001 * sum)), 2));
		return result;
	}

}
