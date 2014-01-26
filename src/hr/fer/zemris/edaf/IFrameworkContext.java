package hr.fer.zemris.edaf;

import java.util.Random;

/**
 * Framework Context Interface. Returns necessary data for framework.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public interface IFrameworkContext {

	/**
	 * Algorithm abbreviation.
	 * 
	 * @return String
	 */
	public String getAlgorithmName();

	public String getWorkEnvironment();

	/**
	 * Genotype abbreviation.
	 * 
	 * @return String
	 */
	public String getGenotype();

	/**
	 * Encoding type. Binary or gray.
	 * 
	 * @return String
	 */
	public String getEncoding();

	/**
	 * Lower bound of axis. Each axis has the same value.
	 * 
	 * @return int
	 */
	public int getLBound();

	/**
	 * Upper bound of axis. Each axis has the same value.
	 * 
	 * @return int
	 */
	public int getUBound();

	/**
	 * Space dimension.
	 * 
	 * @return int
	 */
	public int getDimension();

	/**
	 * Decimal place precision. Important for non floating point genotype.
	 * 
	 * @return int
	 */
	public int getPrecision();

	public String getPrecisionDescription();

	public String getCrossing();
	
	/**
	 * SBX crossing parameter
	 * @return sbx crossing parameter
	 */
	public int getNi();

	public double getCrossingProb();

	public String getMutation();

	public double getMutationProb();

	public int getPopulationSize();

	public double getEstimationProbability();

	public int getElitism();

	public double getMortality();

	public int getMaxNumberOfGen();

	public int getStagnation();

	public int getLogFrequency();

	public String getLogDirectory();

	public String getSelection();

	public double getSelectionRatio();

	public double getSelectionParam();

	public String getRatioSelector();

	public double getRatioSelectorRatio();

	public Random getRand();

}