package com.knezevic.edaf.testing.bbob.noiseless;

import com.knezevic.edaf.testing.bbob.Benchmark;

/**
 * Rosenbrock, rotated
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F9 extends Benchmark {

	public F9(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 9;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, rseed; /* Loop over dim */
		double scales, tmp, Fval, Ftrue = 0.;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			/* computeXopt(rseed, DIM); */
			util.computeRotation(rotation, rseed, util.DIM);
			scales = util.fmax(1., Math.sqrt(util.DIM) / 8.);
			for (i = 0; i < util.DIM; i++) {
				for (j = 0; j < util.DIM; j++) {
					linearTF[i][j] = scales * rotation[i][j];
				}
			}
			/*
			 * for (i = 0; i < DIM; i++) { Xopt[i] = 0.; for (j = 0; j < DIM;
			 * j++) { Xopt[i] += linearTF[j][i] * 0.5/scales/scales; //computed
			 * only if Xopt is returned which is not the case at this point. } }
			 */
			util.isInitDone = true;
		}
		Fadd = util.Fopt;

		/* BOUNDARY HANDLING */

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {
			tmx[i] = 0.5;
			for (j = 0; j < util.DIM; j++) {
				tmx[i] += linearTF[i][j] * x[j];
			}
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
