package hr.fer.zemris.edaf.fitness.bbob.noiseless;

import hr.fer.zemris.edaf.fitness.bbob.Benchmark;

/**
 * Attractive sector function
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F6 extends Benchmark {

	private static double alpha = 100.;

	public F6(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 6;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, k, rseed; /* Loop over dim */
		double Fval, Ftrue = 0.;

		if (!util.isInitDone) {
			final double condition = 10.;
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeXopt(rseed, util.DIM);
			util.computeRotation(rotation, rseed + 1000000, util.DIM);
			util.computeRotation(rot2, rseed, util.DIM);
			/* decouple scaling from function definition */
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
		for (i = 0; i < util.DIM; i++) {
			if ((tmx[i] * util.Xopt[i]) > 0) {
				tmx[i] *= alpha;
			}
			Ftrue += tmx[i] * tmx[i];
		}

		/* MonotoneTFosc... */
		if (Ftrue > 0) {
			Ftrue = Math.pow(
					Math.exp((Math.log(Ftrue) / 0.1)
							+ (0.49 * (Math.sin(Math.log(Ftrue) / 0.1) + Math
									.sin((0.79 * Math.log(Ftrue)) / 0.1)))),
					0.1);
		} else if (Ftrue < 0) {
			Ftrue = -Math.pow(Math.exp((Math.log(-Ftrue) / 0.1)
					+ (0.49 * (Math.sin((0.55 * Math.log(-Ftrue)) / 0.1) + Math
							.sin((0.31 * Math.log(-Ftrue)) / 0.1)))), 0.1);
		}
		Ftrue = Math.pow(Ftrue, 0.9);
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
