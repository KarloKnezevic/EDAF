package hr.fer.zemris.edaf.fitness.bbob.noiseless;

import hr.fer.zemris.edaf.fitness.bbob.Benchmark;

/**
 * Lunacek bi-Rastrigin, condition 100 in PPSN 2008, Rastrigin part rotated and
 * scaled
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F24 extends Benchmark {

	private static double condition = 100.;
	private static double mu1 = 2.5;
	private static double d = 1.;

	public F24(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 24;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, k, rseed; /* Loop over dim */
		double Fpen = 0., tmp, Ftrue = 0., tmp2 = 0., tmp3 = 0., tmp4 = 0., Fval;
		final double s = 1. - (0.5 / (Math.sqrt((util.DIM + 20)) - 4.1));
		final double mu2 = -Math.sqrt(((mu1 * mu1) - d) / s);

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeRotation(rotation, rseed + 1000000, util.DIM);
			util.computeRotation(rot2, rseed, util.DIM);
			util.gauss(tmpvect, util.DIM, rseed);
			for (i = 0; i < util.DIM; i++) {
				util.Xopt[i] = 0.5 * mu1;
				if (tmpvect[i] < 0.) {
					util.Xopt[i] *= -1.;
				}
			}

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
		for (i = 0; i < util.DIM; i++) {
			tmp = Math.abs(x[i]) - 5.;
			if (tmp > 0.) {
				Fpen += tmp * tmp;
			}
		}
		Fadd += 1e4 * Fpen;

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {
			tmx[i] = 2. * x[i];
			if (util.Xopt[i] < 0.) {
				tmx[i] *= -1.;
			}
		}

		/* COMPUTATION core */
		tmp = 0.;
		for (i = 0; i < util.DIM; i++) {
			tmp2 += (tmx[i] - mu1) * (tmx[i] - mu1);
			tmp3 += (tmx[i] - mu2) * (tmx[i] - mu2);
			tmp4 = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmp4 += linearTF[i][j] * (tmx[j] - mu1);
			}
			tmp += Math.cos(2 * Math.PI * tmp4);
		}
		Ftrue = util.fmin(tmp2, (d * util.DIM) + (s * tmp3))
				+ (10. * (util.DIM - tmp));
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
