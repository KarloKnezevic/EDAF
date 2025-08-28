package hr.fer.zemris.edaf.testing.bbob.noiseless;

import hr.fer.zemris.edaf.testing.bbob.Benchmark;

/**
 * Linear slope
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F5 extends Benchmark {

	private static double alpha = 100.;

	public F5(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 5;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, rseed; /* Loop over dim */
		/* Fadd has treatment is different from other functions. */
		double tmp, Fval, Ftrue = 0.;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			Fadd = util.Fopt;
			util.computeXopt(rseed, util.DIM);
			for (i = 0; i < util.DIM; i++) {
				tmp = Math.pow(Math.sqrt(alpha), ((double) i)
						/ ((double) (util.DIM - 1)));
				if (util.Xopt[i] > 0) {
					util.Xopt[i] = 5.;
				} else if (util.Xopt[i] < 0) {
					util.Xopt[i] = -5.;
				}
				Fadd += 5. * tmp;
			}
			util.isInitDone = true;
		}

		/* BOUNDARY HANDLING */
		/* move "too" good coordinates back into domain */
		for (i = 0; i < util.DIM; i++) {
			if ((util.Xopt[i] == 5.) && (x[i] > 5)) {
				tmx[i] = 5.;
			} else if ((util.Xopt[i] == -5.) && (x[i] < -5)) {
				tmx[i] = -5.;
			} else {
				tmx[i] = x[i];
			}
		}

		/* COMPUTATION core */
		for (i = 0; i < util.DIM; i++) {
			if (util.Xopt[i] > 0) {
				Ftrue -= Math.pow(Math.sqrt(alpha), ((double) i)
						/ ((double) (util.DIM - 1)))
						* tmx[i];
			} else {
				Ftrue += Math.pow(Math.sqrt(alpha), ((double) i)
						/ ((double) (util.DIM - 1)))
						* tmx[i];
			}
		}

		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */

		res = Fval;

		return res;
	}

}
