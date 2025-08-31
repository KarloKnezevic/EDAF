package com.knezevic.edaf.testing.bbob.noiseless;

import com.knezevic.edaf.testing.bbob.Benchmark;

/**
 * Rosenbrock, non-rotated
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F8 extends Benchmark {

	private static double scales = 0;

	public F8(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 8;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, rseed; /* Loop over dim */
		double tmp, Fval, Ftrue = 0.;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);

			scales = util.fmax(1., Math.sqrt(util.DIM) / 8.);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeXopt(rseed, util.DIM);
			for (i = 0; i < util.DIM; i++) {
				util.Xopt[i] *= 0.75;
			}
			util.isInitDone = true;
		}
		Fadd = util.Fopt;

		/* BOUNDARY HANDLING */

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {
			tmx[i] = (scales * (x[i] - util.Xopt[i])) + 1;
		}

		/* COMPUTATION core */
		for (i = 0; i < (util.DIM - 1); i++) {
			tmp = ((tmx[i] * tmx[i]) - tmx[i + 1]);
			Ftrue += tmp * tmp;
		}
		Ftrue *= 1e2;
		for (i = 0; i < (util.DIM - 1); i++) {
			tmp = (tmx[i] - 1.);
			Ftrue += tmp * tmp;
		}
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
