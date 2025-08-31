package com.knezevic.edaf.testing.bbob;

/**
 * Util interface. Defined in benchmarkshelper.h.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public interface IUtil {

	/**
	 * Round element to higher.
	 * 
	 * @param a
	 * @return rounded
	 */
	double round(double a);

	/**
	 * Smaller element.
	 * 
	 * @param a
	 * @param b
	 * @return Minor element
	 */
	double fmin(double a, double b);

	/**
	 * Higher element.
	 * 
	 * @param a
	 * @param b
	 * @return Higher element
	 */
	double fmax(double a, double b);

	/**
	 * Generates N uniform numbers with starting seed
	 * 
	 * @param r
	 * @param N
	 * @param inseed
	 */
	void unif(double[] r, int N, int inseed);

	/**
	 * Samples N standard normally distributed numbers being the same for a
	 * given seed.
	 * 
	 * @param g
	 * @param N
	 * @param seed
	 */
	void gauss(double[] g, int N, int seed);

	/**
	 * ?
	 * 
	 * @param seed
	 * @param _DIM
	 */
	void computeXopt(int seed, int _DIM);

	/**
	 * ?
	 * 
	 * @param f
	 */
	void monotoneTFosc(double[] f);

	/**
	 * ?
	 * 
	 * @param B
	 * @param vector
	 * @param m
	 * @param n
	 * @return ?
	 */
	double[][] reshape(double[][] B, double[] vector, int m, int n);

	/**
	 * ?
	 * 
	 * @param B
	 * @param seed
	 * @param _DIM
	 */
	void computeRotation(double[][] B, int seed, int _DIM);

	/**
	 * Adaptation of myrand
	 * 
	 * @return generated unif random number
	 */
	double myrand();

	/**
	 * Adaptation of myrand
	 * 
	 * @return generated gauss random number
	 */
	double randn();

	/**
	 * ?
	 * 
	 * @param Ftrue
	 * @param beta
	 * @return ?
	 */
	double FGauss(double Ftrue, double beta);

	/**
	 * ?
	 * 
	 * @param Ftrue
	 * @param alpha
	 * @param beta
	 * @return ?
	 */
	double FUniform(double Ftrue, double alpha, double beta);

	/**
	 * ?
	 * 
	 * @param Ftrue
	 * @param alpha
	 * @param p
	 * @return ?
	 */
	double FCauchy(double Ftrue, double alpha, double p);

	/**
	 * Not used.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	int compare_doubles(int a, int b);

	/**
	 * Util initialization.
	 */
	void initUtil();

	/**
	 * Util terminating.
	 */
	void finiUtil();

	double computeFopt(int _funcId, int _trialId);

	/**
	 * Set the seed for the noise. If the seeds are larger than 1e9 they are set
	 * back to 1 in randn and myrand.
	 * 
	 * @param _seed
	 * @param _seedn
	 */
	void setNoiseSeed(int _seed, int _seedn);

}
