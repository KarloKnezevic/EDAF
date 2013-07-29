package hr.fer.zemris.edaf.statistics.reprezentation;

/**
 * Interface to statistics data reprezentation.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public interface IStatReprezentation {

	/**
	 * Length of reprezentation.
	 * 
	 * @return length of reprezentation
	 */
	public double getLength();

	/**
	 * If there is more probab data?
	 * 
	 * @return true if not all data is read; false if not
	 */
	public boolean hasMoreProbabilities();

	/**
	 * Get data indexes in reprezentation to reach.
	 * 
	 * @return data indexes
	 */
	public int[] getIndexesInRepresentation();

	/**
	 * Get probability for index in reprezentation.
	 * 
	 * @param index
	 *            data index in reprezentation
	 * @return probability
	 */
	public double getProbability(int index);

	/**
	 * set the selected value according to the probability
	 * 
	 * @param value
	 *            selected value
	 * @param index
	 *            data index in reprezentation
	 */
	public void setValueForIndex(double value, int index);
}