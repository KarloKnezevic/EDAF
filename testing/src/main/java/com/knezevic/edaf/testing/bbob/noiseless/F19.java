package com.knezevic.edaf.testing.bbob.noiseless;

import com.knezevic.edaf.testing.bbob.Benchmark;

/**
 * F8F2 sum of Griewank-Rosenbrock 2-D blocks
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F19 extends Benchmark {

	public F19(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 19;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, rseed; /* Loop over dim */
		double scales, F2, tmp = 0., tmp2, Fval, Ftrue = 0.;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			/* computeXopt(rseed, DIM); Xopt is not used. */
			scales = util.fmax(1., Math.sqrt(util.DIM) / 8.);
			util.computeRotation(rotation, rseed, util.DIM);
			for (i = 0; i < util.DIM; i++) {
				for (j = 0; j < util.DIM; j++) {
					linearTF[i][j] = scales * rotation[i][j];
				}
			}
			for (i = 0; i < util.DIM; i++) {
				util.Xopt[i] = 0.;
				for (j = 0; j < util.DIM; j++) {
					util.Xopt[i] += (linearTF[j][i] * 0.5) / scales / scales;
				}
			}
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
			tmp2 = (tmx[i] * tmx[i]) - tmx[i + 1];
			F2 = 100. * tmp2 * tmp2;
			tmp2 = 1 - tmx[i];
			F2 += tmp2 * tmp2;
			tmp += (F2 / 4000.) - Math.cos(F2);
		}
		Ftrue = 10. + ((10. * tmp) / (util.DIM - 1));

		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
