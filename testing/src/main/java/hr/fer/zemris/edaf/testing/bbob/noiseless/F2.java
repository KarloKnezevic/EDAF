package hr.fer.zemris.edaf.testing.bbob.noiseless;

import hr.fer.zemris.edaf.testing.bbob.Benchmark;

/**
 * Separable ellipsoid with monotone transformation, condition 1e6
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F2 extends Benchmark {

	private static double condition = 1e6;

	public F2(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 2;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, rseed; /* Loop over dim */
		double Fval, Ftrue = 0.0;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeXopt(rseed, util.DIM);
			util.isInitDone = false;
		}

		Fadd = util.Fopt;

		for (i = 0; i < util.DIM; i++) {
			tmx[i] = x[i] - util.Xopt[i];
		}

		util.monotoneTFosc(tmx);

		/* COMPUTATION core */
		for (i = 0; i < util.DIM; i++) {
			Ftrue += Math.pow(condition, ((double) i)
					/ ((double) (util.DIM - 1)))
					* tmx[i] * tmx[i];
		}
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
