package hr.fer.zemris.edaf.testing.bbob.noiseless;

import hr.fer.zemris.edaf.testing.bbob.Benchmark;

/**
 * Skew Rastrigin-Bueche, condition 10, skew-"condition" 100
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F4 extends Benchmark {

	private static double condition = 10.;
	private static double alpha = 100.;

	public F4(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 4;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, rseed; /* Loop over dim */
		double tmp, tmp2, Fval, Fpen = 0., Ftrue = 0.;

		if (!util.isInitDone) {
			rseed = 3 + (10000 * util.trialid); /* Not the same as before. */
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeXopt(rseed, util.DIM);
			for (i = 0; i < util.DIM; i += 2) {
				util.Xopt[i] = Math.abs(util.Xopt[i]); /* Skew */
			}
			util.isInitDone = true;
		}
		Fadd = util.Fopt;

		for (i = 0; i < util.DIM; i++) {
			tmp = Math.abs(x[i]) - 5.;
			if (tmp > 0.) {
				Fpen += tmp * tmp;
			}
		}
		Fpen *= 1e2;
		Fadd += Fpen;

		for (i = 0; i < util.DIM; i++) {
			tmx[i] = x[i] - util.Xopt[i];
		}

		util.monotoneTFosc(tmx);
		for (i = 0; i < util.DIM; i++) {
			if (((i % 2) == 0) && (tmx[i] > 0)) {
				tmx[i] = Math.sqrt(alpha) * tmx[i];
			}
			tmx[i] = Math.pow(Math.sqrt(condition), ((double) i)
					/ ((double) (util.DIM - 1)))
					* tmx[i];
		}
		/* COMPUTATION core */
		tmp = 0.;
		tmp2 = 0.;
		for (i = 0; i < util.DIM; i++) {
			tmp += Math.cos(2 * Math.PI * tmx[i]);
			tmp2 += tmx[i] * tmx[i];
		}
		Ftrue = (10 * (util.DIM - tmp)) + tmp2;
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
