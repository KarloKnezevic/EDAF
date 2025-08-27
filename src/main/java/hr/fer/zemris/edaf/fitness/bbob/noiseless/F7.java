package hr.fer.zemris.edaf.fitness.bbob.noiseless;

import hr.fer.zemris.edaf.fitness.bbob.Benchmark;

/**
 * Step-ellipsoid, condition 100
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F7 extends Benchmark {

	private static double condition = 100.;
	private static double alpha = 10.;

	public F7(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 7;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, rseed; /* Loop over dim */
		double x1, tmp, Fval, Fpen = 0., Ftrue = 0.;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
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
		Fadd += Fpen;

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {

			tmpvect[i] = 0.;
			tmp = Math.sqrt(Math.pow(condition / 10., ((double) i)
					/ ((double) (util.DIM - 1))));
			for (j = 0; j < util.DIM; j++) {
				tmpvect[i] += tmp * rot2[i][j] * (x[j] - util.Xopt[j]);
			}

		}
		x1 = tmpvect[0];

		for (i = 0; i < util.DIM; i++) {
			if (Math.abs(tmpvect[i]) > 0.5) {
				tmpvect[i] = util.round(tmpvect[i]);
			} else {
				tmpvect[i] = util.round(alpha * tmpvect[i]) / alpha;
			}
		}

		for (i = 0; i < util.DIM; i++) {
			tmx[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmx[i] += rotation[i][j] * tmpvect[j];
			}
		}

		/* COMPUTATION core */
		for (i = 0; i < util.DIM; i++) {
			Ftrue += Math.pow(condition, ((double) i)
					/ ((double) (util.DIM - 1)))
					* tmx[i] * tmx[i];
		}
		Ftrue = 0.1 * util.fmax(1e-4 * Math.abs(x1), Ftrue);

		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
