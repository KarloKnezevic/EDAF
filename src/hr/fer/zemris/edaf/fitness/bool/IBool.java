package hr.fer.zemris.edaf.fitness.bool;

/**
 * Boolean function optimization interface.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public interface IBool {

	/**
	 * Computes fitness.
	 * 
	 * @param bits
	 *            bit vector
	 * @return fitness
	 */
	public int computeBool(int[] bits);

}
