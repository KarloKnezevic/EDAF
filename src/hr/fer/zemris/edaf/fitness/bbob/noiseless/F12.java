package hr.fer.zemris.edaf.fitness.bbob.noiseless;

import hr.fer.zemris.edaf.fitness.bbob.Benchmark;

/**
 * Bent cigar with asymmetric space distortion, condition 1e6
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F12 extends Benchmark {

	private static double condition = 1e6;
	private static double beta = 0.5;

	public F12(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 12;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, rseed; /* Loop over dim */
		double Fval, Ftrue;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeXopt(rseed + 1000000, util.DIM);
			util.computeRotation(rotation, rseed + 1000000, util.DIM);
			util.isInitDone = true;
		}
		Fadd = util.Fopt;
		/* BOUNDARY HANDLING */

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {
			tmpvect[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmpvect[i] += rotation[i][j] * (x[j] - util.Xopt[j]);
			}
			if (tmpvect[i] > 0) {
				tmpvect[i] = Math.pow(tmpvect[i],
						1 + (((beta * i) / (util.DIM - 1)) * Math
								.sqrt(tmpvect[i])));
			}
		}

		for (i = 0; i < util.DIM; i++) {
			tmx[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmx[i] += rotation[i][j] * tmpvect[j];
			}
		}

		/* COMPUTATION core */
		Ftrue = tmx[0] * tmx[0];
		for (i = 1; i < util.DIM; i++) {
			Ftrue += condition * tmx[i] * tmx[i];
		}
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
