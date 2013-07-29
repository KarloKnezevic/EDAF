package hr.fer.zemris.edaf.fitness.bbob.noiseless;

import hr.fer.zemris.edaf.fitness.bbob.Benchmark;

/**
 * Ellipsoid with monotone transformation, condition 1e6
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F10 extends Benchmark {

	private static double condition = 1e6;

	public F10(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 10;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, rseed; /* Loop over dim */
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
