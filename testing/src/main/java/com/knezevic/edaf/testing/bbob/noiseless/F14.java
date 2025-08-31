package com.knezevic.edaf.testing.bbob.noiseless;

import com.knezevic.edaf.testing.bbob.Benchmark;

/**
 * Sum of different powers, between x^2 and x^6
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F14 extends Benchmark {

	private static double alpha = 4.;

	public F14(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 14;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, rseed;
		double Fval, Ftrue = 0.;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeXopt(rseed, util.DIM);
			util.computeRotation(rotation, rseed + 1000000, util.DIM);
			util.isInitDone = true;
		}
		Fadd = util.Fopt;
		/* BOUNDARY HANDLING */

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {
			tmx[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmx[i] += rotation[i][j] * (x[j] - util.Xopt[j]);
			}
		}

		/* COMPUTATION core */
		for (i = 0; i < util.DIM; i++) {
			Ftrue += Math.pow(Math.abs(tmx[i]),
					2. + ((alpha * i) / (util.DIM - 1)));
		}
		Ftrue = Math.sqrt(Ftrue);

		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
