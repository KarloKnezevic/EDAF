package hr.fer.zemris.edaf;

import java.util.Random;

/**
 * This interface defines the contract for providing all the parameters to the
 * EDA framework. An implementation of this interface is responsible for loading
 * the configuration from a file (e.g., XML, JSON) and providing the values
 * to the framework.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.1
 */
public interface IFrameworkContext {

    /**
     * Gets the name of the algorithm to be used.
     *
     * @return the algorithm name
     */
    public String getAlgorithmName();

    /**
     * Gets the name of the work environment (the problem to be solved).
     *
     * @return the work environment name
     */
    public String getWorkEnvironment();

    /**
     * Gets the type of the genotype (e.g., "B" for binary, "FP" for floating point).
     *
     * @return the genotype type
     */
    public String getGenotype();

    /**
     * Gets the encoding type for the genotype (e.g., "binary", "gray").
     *
     * @return the encoding type
     */
    public String getEncoding();

    /**
     * Gets the lower bound for the genotype's variables.
     *
     * @return the lower bound
     */
    public int getLBound();

    /**
     * Gets the upper bound for the genotype's variables.
     *
     * @return the upper bound
     */
    public int getUBound();

    /**
     * Gets the dimension of the problem space.
     *
     * @return the dimension
     */
    public int getDimension();

    /**
     * Gets the precision for the genotype's variables.
     *
     * @return the precision
     */
    public int getPrecision();

    /**
     * Gets the description of the precision (e.g., "decimal").
     *
     * @return the precision description
     */
    public String getPrecisionDescription();

    /**
     * Gets the name of the crossing operator.
     *
     * @return the crossing operator name
     */
    public String getCrossing();
	
	/**
	 * Gets the SBX crossing parameter.
	 * @return the SBX crossing parameter
	 */
	public int getNi();

    /**
     * Gets the crossing probability.
     *
     * @return the crossing probability
     */
    public double getCrossingProb();

    /**
     * Gets the name of the mutation operator.
     *
     * @return the mutation operator name
     */
    public String getMutation();

    /**
     * Gets the mutation probability.
     *
     * @return the mutation probability
     */
    public double getMutationProb();

    /**
     * Gets the population size.
     *
     * @return the population size
     */
    public int getPopulationSize();

    /**
     * Gets the estimation probability for EDAs.
     *
     * @return the estimation probability
     */
    public double getEstimationProbability();

    /**
     * Gets the number of elite individuals to be preserved.
     *
     * @return the number of elite individuals
     */
    public int getElitism();

    /**
     * Gets the mortality rate for eliminative GAs.
     *
     * @return the mortality rate
     */
    public double getMortality();

    /**
     * Gets the maximum number of generations.
     *
     * @return the maximum number of generations
     */
    public int getMaxNumberOfGen();

    /**
     * Gets the stagnation limit (number of generations without improvement).
     *
     * @return the stagnation limit
     */
    public int getStagnation();
	
	/**
	 * Gets the destination value (the target fitness value).
	 *
	 * @return the destination value
	 */
	public double getDestValue();

    /**
     * Gets the frequency for logging.
     *
     * @return the log frequency
     */
    public int getLogFrequency();

    /**
     * Gets the directory for logging.
     *
     * @return the log directory
     */
    public String getLogDirectory();

    /**
     * Gets the name of the selection operator.
     *
     * @return the selection operator name
     */
    public String getSelection();

    /**
     * Gets the selection ratio.
     *
     * @return the selection ratio
     */
    public double getSelectionRatio();

    /**
     * Gets the selection parameter (e.g., for tournament selection).
     *
     * @return the selection parameter
     */
    public double getSelectionParam();

    /**
     * Gets the name of the ratio selector.
     *
     * @return the ratio selector name
     */
    public String getRatioSelector();

    /**
     * Gets the ratio for the ratio selector.
     *
     * @return the ratio selector ratio
     */
    public double getRatioSelectorRatio();

    /**
     * Gets the random number generator.
     *
     * @return the random number generator
     */
    public Random getRand();

}