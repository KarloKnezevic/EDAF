package com.knezevic.edaf.testing.bbob.noiseless;

import com.knezevic.edaf.testing.bbob.Benchmark;

/**
 * Katsuura function
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F23 extends Benchmark {

	private static double condition = 100.;

	public F23(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 23;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, k, rseed; /* Loop over dim */
		double Fpen = 0., tmp, Ftrue = 0., arr, prod = 1., tmp2, Fval;

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
										/ (double) (util.DIM - 1)) * rot2[k][j];
					}
				}
			}
			util.isInitDone = true;
		}
		Fadd = util.Fopt;

		/* BOUNDARY HANDLING */
		for (i = 0; i < util.DIM; i++) {
			tmp = Math.abs(x[i]) - 5.;
			if (tmp > 0.) {
				Fpen += tmp * tmp;
			}
		}
		Fadd += Fpen;

		/* TRANSFORMATION IN SEARCH SPACE */
		/* write rotated difference vector into tmx */
		for (j = 0; j < util.DIM; j++) {
			/* store difference vector */
			tmpvect[j] = x[j] - util.Xopt[j];
		}
		for (i = 0; i < util.DIM; i++) {
			tmx[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmx[i] += linearTF[i][j] * tmpvect[j];
			}
		}

		/*
		 * for (i = 0; i < DIM; i++) { tmx[i] = 0.; for (j = 0; j < DIM; j++) {
		 * tmx[i] += linearTF[i][j] * (x[j] - Xopt[j]); } }
		 */

		/* COMPUTATION core */
		for (i = 0; i < util.DIM; i++) {
			tmp = 0.;
			for (j = 1; j < 33; j++) {
				tmp2 = Math.pow(2., j);
				arr = tmx[i] * tmp2;
				tmp += Math.abs(arr - util.round(arr)) / tmp2;
			}
			tmp = 1. + (tmp * (i + 1));
			prod *= tmp;
		}
		Ftrue = (10. / util.DIM / util.DIM)
				* (-1. + Math.pow(prod, 10. / Math.pow(util.DIM, 1.2)));
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
