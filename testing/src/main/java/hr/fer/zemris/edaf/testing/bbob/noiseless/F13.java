package hr.fer.zemris.edaf.testing.bbob.noiseless;

import hr.fer.zemris.edaf.testing.bbob.Benchmark;

/**
 * Sharp ridge
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F13 extends Benchmark {

	private static double condition = 10.;
	private static double alpha = 100.;

	public F13(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 13;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, k, rseed; /* Loop over dim */

		double Fval, Ftrue = 0.;

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
			tmx[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmx[i] += linearTF[i][j] * (x[j] - util.Xopt[j]);
			}
		}

		/* COMPUTATION core */
		for (i = 1; i < util.DIM; i++) {
			Ftrue += tmx[i] * tmx[i];
		}
		Ftrue = alpha * Math.sqrt(Ftrue);
		Ftrue += tmx[0] * tmx[0];

		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
