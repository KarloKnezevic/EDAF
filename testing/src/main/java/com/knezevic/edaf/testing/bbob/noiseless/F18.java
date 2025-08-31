package com.knezevic.edaf.testing.bbob.noiseless;

import com.knezevic.edaf.testing.bbob.Benchmark;

/**
 * Schaffers F7 with asymmetric non-linear transformation, condition 1000
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F18 extends Benchmark {

	private static double condition = 1e3;
	private static double beta = 0.5;

	public F18(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 18;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, rseed; /* Loop over dim */
		double tmp, Fval, Fpen = 0., Ftrue = 0.;

		if (!util.isInitDone) {
			rseed = 17 + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeXopt(rseed, util.DIM);
			util.computeRotation(rotation, rseed + 1000000, util.DIM);
			util.computeRotation(rot2, rseed, util.DIM);
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
		Fadd += 10. * Fpen;

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {
			tmpvect[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmpvect[i] += rotation[i][j] * (x[j] - util.Xopt[j]);
			}
			if (tmpvect[i] > 0) {
				tmpvect[i] = Math.pow(tmpvect[i],
						1. + (((beta * i) / (util.DIM - 1)) * Math
								.sqrt(tmpvect[i])));
			}
		}

		for (i = 0; i < util.DIM; i++) {
			tmx[i] = 0.;
			tmp = Math.pow(Math.sqrt(condition), ((double) i)
					/ ((double) (util.DIM - 1)));
			for (j = 0; j < util.DIM; j++) {
				tmx[i] += tmp * rot2[i][j] * tmpvect[j];
			}
		}

		/* COMPUTATION core */
		for (i = 0; i < (util.DIM - 1); i++) {
			tmp = (tmx[i] * tmx[i]) + (tmx[i + 1] * tmx[i + 1]);
			Ftrue += Math.pow(tmp, 0.25)
					* (Math.pow(Math.sin(50. * Math.pow(tmp, 0.1)), 2.) + 1.);
		}
		Ftrue = Math.pow(Ftrue / (util.DIM - 1), 2.);
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
