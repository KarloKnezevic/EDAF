package hr.fer.zemris.edaf.fitness.bbob.noiseless;

import hr.fer.zemris.edaf.fitness.bbob.Benchmark;

/**
 * Weierstrass, condition 100
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F16 extends Benchmark {

	private static double condition = 100.;
	private static double[] aK = new double[12];
	private static double[] bK = new double[12];
	private static double F0 = 0;

	public F16(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 16;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, k, rseed; /* Loop over dim */
		double tmp, Fval, Fpen = 0., Ftrue = 0.;

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
								* Math.pow(1. / Math.sqrt(condition),
										((double) k)
												/ ((double) (util.DIM - 1)))
								* rot2[k][j];
					}
				}
			}

			F0 = 0.;
			/*
			 * number of summands, 20 in CEC2005, 10/12 saves 30% of time
			 */
			for (i = 0; i < 12; i++) {
				aK[i] = Math.pow(0.5, i);
				bK[i] = Math.pow(3., i);
				F0 += aK[i] * Math.cos(2 * Math.PI * bK[i] * 0.5);
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
		Fadd += (10. / util.DIM) * Fpen;

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {
			tmpvect[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmpvect[i] += rotation[i][j] * (x[j] - util.Xopt[j]);
			}
		}

		util.monotoneTFosc(tmpvect);
		for (i = 0; i < util.DIM; i++) {
			tmx[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmx[i] += linearTF[i][j] * tmpvect[j];
			}
		}
		/* COMPUTATION core */
		for (i = 0; i < util.DIM; i++) {
			tmp = 0.;
			for (j = 0; j < 12; j++) {
				tmp += Math.cos(2 * Math.PI * (tmx[i] + 0.5) * bK[j]) * aK[j];
			}
			Ftrue += tmp;
		}
		Ftrue = 10. * Math.pow((Ftrue / util.DIM) - F0, 3.);
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
