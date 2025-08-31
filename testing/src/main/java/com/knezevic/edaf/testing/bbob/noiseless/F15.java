package com.knezevic.edaf.testing.bbob.noiseless;

import com.knezevic.edaf.testing.bbob.Benchmark;

/**
 * Rastrigin with asymmetric non-linear distortion, "condition" 10
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F15 extends Benchmark {

	private static double condition = 10.;
	private static double beta = 0.2;

	public F15(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 15;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, k, rseed; /* Loop over dim */
		double tmp = 0., tmp2 = 0., Fval, Ftrue;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeXopt(rseed, util.DIM);
			util.computeRotation(rotation, rseed + 1000000, util.DIM);
			util.computeRotation(rot2, rseed, util.DIM);
			for (i = 0; i < util.DIM; i++) {
				for (j = 0; j < util.DIM; j++) {
					linearTF[i][j] = 0.;
					for (k = 0; k < util.DIM; k++) {
						linearTF[i][j] += rotation[i][k]
								* Math.pow(Math.sqrt(condition), ((double) k)
										/ ((double) (util.DIM - 1)))
								* rot2[k][j];
					}
				}
			}
			util.isInitDone = true;
		}
		Fadd = util.Fopt;
		/* BOUNDARY HANDLING */

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {
			tmpvect[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmpvect[i] += rotation[i][j] * (x[j] - util.Xopt[j]);
			}
		}

		util.monotoneTFosc(tmpvect);
		for (i = 0; i < util.DIM; i++) {
			if (tmpvect[i] > 0) {
				tmpvect[i] = Math.pow(tmpvect[i],
						1 + (((beta * i) / (util.DIM - 1)) * Math
								.sqrt(tmpvect[i])));
			}
		}
		for (i = 0; i < util.DIM; i++) {
			tmx[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmx[i] += linearTF[i][j] * tmpvect[j];
			}
		}
		/* COMPUTATION core */
		for (i = 0; i < util.DIM; i++) {
			tmp += Math.cos(2. * Math.PI * tmx[i]);
			tmp2 += tmx[i] * tmx[i];
		}
		Ftrue = (10. * (util.DIM - tmp)) + tmp2;
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
