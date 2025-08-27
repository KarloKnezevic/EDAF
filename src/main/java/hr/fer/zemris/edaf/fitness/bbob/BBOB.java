package hr.fer.zemris.edaf.fitness.bbob;

import hr.fer.zemris.edaf.fitness.bbob.noiseless.NoislessFunctionsFactory;

/**
 * Black-Box Optimization Benchmarking Real-Parameter Black-Box Optimization
 * Benchmarking 2010: Presentation of the Noiseless Functions Steffen Finck,
 * Nikolaus Hanseny, Raymond Rosz and Anne Augerx Working Paper 2009/20,
 * compiled March 1, 2013.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */

public class BBOB {

	private int benchmarkId;

	private int instanceId;

	private int dimension;

	private final Benchmark benchmark;

	/**
	 * Constructor.
	 * 
	 * @param benchmarkId
	 *            benchmark id
	 * @param instanceId
	 *            instance id; used to change the function optimum
	 * @param dimension
	 *            domain vector dimension
	 */
	public BBOB(int benchmarkId, int instanceId, int dimension) {
		super();
		this.benchmarkId = benchmarkId;
		this.instanceId = instanceId;
		this.dimension = dimension;

		benchmark = NoislessFunctionsFactory.getBenchmark(benchmarkId,
				instanceId, dimension);

		if (benchmark == null) {
			System.err.println("Benchmark ERROR. STOP.");
			System.exit(-1);
		}
	}

	public void init() {
		benchmark.initBenchmark();
		benchmark.util.initUtil();
	}

	public double evaluate(double[] variable) {
		return benchmark.computeFunction(variable);
	}

	public void fini() {
		benchmark.finiBenchmark();
		benchmark.util.finiUtil();
	}

	public int getBenchmarkId() {
		return benchmarkId;
	}

	public void setBenchmarkId(int benchmarkId) {
		this.benchmarkId = benchmarkId;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public Benchmark getBenchmark() {
		return benchmark;
	}

}
