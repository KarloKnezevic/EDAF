package hr.fer.zemris.edaf.testing.bbob.noiseless;

import hr.fer.zemris.edaf.testing.bbob.Benchmark;

/**
 * Schwefel with tridiagonal variable transformation
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F20 extends Benchmark {

	private static double condition = 10.;

	public F20(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 20;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, rseed; /* Loop over dim */
		double tmp, Fval, Fpen = 0., Ftrue = 0.;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.unif(tmpvect, util.DIM, rseed);
			for (i = 0; i < util.DIM; i++) {
				util.Xopt[i] = 0.5 * 4.2096874633;
				if ((tmpvect[i] - 0.5) < 0) {
					util.Xopt[i] *= -1.;
				}
			}
			util.isInitDone = true;
		}
		Fadd = util.Fopt;

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {
			tmpvect[i] = 2. * x[i];
			if (util.Xopt[i] < 0.) {
				tmpvect[i] *= -1.;
			}
		}

		tmx[0] = tmpvect[0];
		for (i = 1; i < util.DIM; i++) {
			tmx[i] = tmpvect[i]
					+ (0.25 * (tmpvect[i - 1] - (2. * Math
							.abs(util.Xopt[i - 1]))));
		}

		for (i = 0; i < util.DIM; i++) {
			tmx[i] -= 2 * Math.abs(util.Xopt[i]);
			tmx[i] *= Math.pow(Math.sqrt(condition), ((double) i)
					/ ((double) (util.DIM - 1)));
			tmx[i] = 100. * (tmx[i] + (2 * Math.abs(util.Xopt[i])));
		}

		/* BOUNDARY HANDLING */
		for (i = 0; i < util.DIM; i++) {
			tmp = Math.abs(tmx[i]) - 500.;
			if (tmp > 0.) {
				Fpen += tmp * tmp;
			}
		}
		Fadd += 0.01 * Fpen;

		/* COMPUTATION core */
		for (i = 0; i < util.DIM; i++) {
			Ftrue += tmx[i] * Math.sin(Math.sqrt(Math.abs(tmx[i])));
		}
		Ftrue = 0.01 * ((418.9828872724339) - (Ftrue / util.DIM));

		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
