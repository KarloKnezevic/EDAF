package hr.fer.zemris.edaf.fitness.multimodal;

/**
 * IMultimodal.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public interface IMultimodal {

	/**
	 * Evaluate multimodal function.
	 * 
	 * @param variable
	 * @return value
	 */
	public double computeMultimodal(double[] variable);

}
