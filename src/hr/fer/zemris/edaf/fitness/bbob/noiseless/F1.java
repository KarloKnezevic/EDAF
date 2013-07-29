package hr.fer.zemris.edaf.fitness.bbob.noiseless;

import hr.fer.zemris.edaf.fitness.bbob.Benchmark;

/**
 * Sphere function
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F1 extends Benchmark {

	public F1(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 1;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, rseed;
		double r, Fval, Ftrue = 0.0;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeXopt(rseed, util.DIM);
			util.isInitDone = true;
		}

		Fadd = util.Fopt;
		/* COMPUTATION core */
		for (i = 0; i < util.DIM; i++) {
			r = x[i] - util.Xopt[i];
			Ftrue += r * r;
		}
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
