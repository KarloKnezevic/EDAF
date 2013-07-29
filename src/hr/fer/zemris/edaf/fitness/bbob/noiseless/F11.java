package hr.fer.zemris.edaf.fitness.bbob.noiseless;

import hr.fer.zemris.edaf.fitness.bbob.Benchmark;

/**
 * Discus (tablet) with monotone transformation, condition 1e6
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F11 extends Benchmark {

	private static double condition = 1e6;

	public F11(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 11;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, rseed; /* Loop over dim */
		double Fval, Ftrue;

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

		util.monotoneTFosc(tmx);

		/* COMPUTATION core */
		Ftrue = condition * tmx[0] * tmx[0];
		for (i = 1; i < util.DIM; i++) {
			Ftrue += tmx[i] * tmx[i];
		}
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
