package hr.fer.zemris.edaf.testing.bbob;

/**
 * BBOB Benchmark.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public abstract class Benchmark {

	protected int funcId;

	protected double res;

	protected Util util;

	// ***********************

	protected final static int NHIGHPEAKS21 = 101;

	protected final static int NHIGHPEAKS22 = 21;

	// ***********************

	protected double Fadd = 0.;

	// ***********************

	protected double[] tmpvect;

	protected double[] tmx;

	protected double[][] rotation;

	protected double[][] rot2;

	protected double[][] linearTF;

	protected double[] peaks21;

	protected double[] peaks22;

	protected int[] rperm;

	protected int[] rperm21;

	protected int[] rperm22;

	protected double[][] Xlocal;

	protected double[][] Xlocal21;

	protected double[][] Xlocal22;

	protected double[][] arrScales;

	protected double[][] arrScales21;

	protected double[][] arrScales22;

	// ***********************

	/**
	 * Constructor.
	 * 
	 * @param instanceId
	 *            function Id
	 * @param dimension
	 */
	public Benchmark(int instanceId, int dimension) {
		util = new Util(dimension, instanceId);
	}

	/**
	 * Computes function.
	 * 
	 * @param x
	 *            variable vector
	 * @return function evaluation
	 */
	public abstract double computeFunction(double[] x);

	public int getFuncId() {
		return funcId;
	}

	public double getRes() {
		return res;
	}

	public double getFadd() {
		return Fadd;
	}

	/**
	 * Benchmark initialization. Must be called before function computation.
	 */
	public void initBenchmark() {

		tmpvect = new double[util.DIM];
		tmx = new double[util.DIM];
		rotation = new double[util.DIM][];
		rot2 = new double[util.DIM][];
		linearTF = new double[util.DIM][];
		peaks21 = new double[util.DIM * NHIGHPEAKS21];
		rperm21 = new int[(int) util.fmax(util.DIM, NHIGHPEAKS21 - 1)];
		arrScales21 = new double[NHIGHPEAKS21][];
		Xlocal21 = new double[util.DIM][];
		peaks22 = new double[util.DIM * NHIGHPEAKS22];
		rperm22 = new int[(int) util.fmax(util.DIM, NHIGHPEAKS22 - 1)];
		arrScales22 = new double[NHIGHPEAKS22][];
		Xlocal22 = new double[util.DIM][];

		for (int i = 0; i < util.DIM; i++) {
			rotation[i] = new double[util.DIM];
			rot2[i] = new double[util.DIM];
			linearTF[i] = new double[util.DIM];
			Xlocal21[i] = new double[NHIGHPEAKS21];
			Xlocal22[i] = new double[NHIGHPEAKS22];
		}

		for (int i = 0; i < NHIGHPEAKS21; i++) {
			arrScales21[i] = new double[util.DIM];
		}

		for (int i = 0; i < NHIGHPEAKS22; i++) {
			arrScales22[i] = new double[util.DIM];
		}

	}

	public void finiBenchmark() {
		tmpvect = null;
		tmx = null;
		rotation = null;
		rot2 = null;
		linearTF = null;
		peaks21 = null;
		rperm21 = null;
		arrScales21 = null;
		Xlocal21 = null;
		peaks22 = null;
		rperm22 = null;
		arrScales22 = null;
		Xlocal22 = null;
	}

}
