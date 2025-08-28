package hr.fer.zemris.edaf.testing.bbob.noiseless;

import hr.fer.zemris.edaf.testing.bbob.Benchmark;

/**
 * Rastrigin with monotone transformation separable "condition" 10
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F3 extends Benchmark {

	private static double condition = 10.;
	private static double beta = 0.2;

	public F3(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 3;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, rseed; /* Loop over dim */
		double tmp, tmp2, Fval, Ftrue = 0.0;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeXopt(rseed, util.DIM);
			util.isInitDone = true;
		}

		Fadd = util.Fopt;
		for (i = 0; i < util.DIM; i++) {
			tmx[i] = x[i] - util.Xopt[i];
		}

		util.monotoneTFosc(tmx);
		for (i = 0; i < util.DIM; i++) {
			tmp = ((double) i) / ((double) (util.DIM - 1));
			if (tmx[i] > 0) {
				tmx[i] = Math.pow(tmx[i], 1 + (beta * tmp * Math.sqrt(tmx[i])));
			}
			tmx[i] = Math.pow(Math.sqrt(condition), tmp) * tmx[i];
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
