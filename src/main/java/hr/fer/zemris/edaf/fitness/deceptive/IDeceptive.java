package hr.fer.zemris.edaf.fitness.deceptive;

/**
 * IDeceptive. Iterface for deceptive functions.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public interface IDeceptive {

	/**
	 * Evaluate deceptive function.
	 * 
	 * @param bits
	 * @return value
	 */
	public double computeDeceptive(byte[] bits);

}
