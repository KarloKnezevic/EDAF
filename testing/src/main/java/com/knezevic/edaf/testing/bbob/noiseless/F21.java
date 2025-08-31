package com.knezevic.edaf.testing.bbob.noiseless;

import com.knezevic.edaf.testing.bbob.Benchmark;

import java.util.Arrays;

/**
 * Gallagher with 101 Gaussian peaks, condition up to 1000, one global rotation
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class F21 extends Benchmark {

	private static double[] fitvalues = new double[] { 1.1, 9.1 };
	private static double maxcondition = 1000.;
	private static double[] arrCondition = new double[NHIGHPEAKS21];
	private static double[] peakvalues = new double[NHIGHPEAKS21];
	private static double a = 0.1;

	public F21(int instanceId, int dimension) {
		super(instanceId, dimension);

		funcId = 21;
	}

	@Override
	public double computeFunction(double[] x) {
		int i, j, k, rseed; /* Loop over dim */
		double tmp2, f = 0., Fval, tmp, Fpen = 0., Ftrue = 0.;
		final double fac = -0.5 / util.DIM;

		if (!util.isInitDone) {
			rseed = funcId + (10000 * util.trialid);
			/* INITIALIZATION */
			util.Fopt = util.computeFopt(funcId, util.trialid);
			util.computeRotation(rotation, rseed, util.DIM);
			util.peaks = peaks21;
			util.unif(util.peaks, NHIGHPEAKS21 - 1, rseed);
			rperm = rperm21;
			for (i = 0; i < (NHIGHPEAKS21 - 1); i++) {
				rperm[i] = i;
			}
			// qsort(rperm, NHIGHPEAKS21 - 1, sizeof(int), compare_doubles);
			Arrays.sort(rperm);

			/* Random permutation */
			arrCondition[0] = Math.sqrt(maxcondition);
			peakvalues[0] = 10;
			for (i = 1; i < NHIGHPEAKS21; i++) {
				arrCondition[i] = Math
						.pow(maxcondition, (double) (rperm[i - 1])
								/ ((double) (NHIGHPEAKS21 - 2)));
				peakvalues[i] = (((double) (i - 1) / (double) (NHIGHPEAKS21 - 2)) * (fitvalues[1] - fitvalues[0]))
						+ fitvalues[0];
			}
			arrScales = arrScales21;
			for (i = 0; i < NHIGHPEAKS21; i++) {
				util.unif(util.peaks, util.DIM, rseed + (1000 * i));
				for (j = 0; j < util.DIM; j++) {
					rperm[j] = j;
				}
				Arrays.sort(rperm);
				// qsort(rperm, DIM, sizeof(int), compare_doubles);
				for (j = 0; j < util.DIM; j++) {
					arrScales[i][j] = Math
							.pow(arrCondition[i],
									(((double) rperm[j]) / ((double) (util.DIM - 1))) - 0.5);
				}
			}

			util.unif(util.peaks, util.DIM * NHIGHPEAKS21, rseed);
			Xlocal = Xlocal21;
			for (i = 0; i < util.DIM; i++) {
				util.Xopt[i] = 0.8 * ((10. * util.peaks[i]) - 5.);
				for (j = 0; j < NHIGHPEAKS21; j++) {
					Xlocal[i][j] = 0.;
					for (k = 0; k < util.DIM; k++) {
						Xlocal[i][j] += rotation[i][k]
								* ((10. * util.peaks[(j * util.DIM) + k]) - 5.);
					}
					if (j == 0) {
						Xlocal[i][j] *= 0.8;
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
		Fadd += Fpen;

		/* TRANSFORMATION IN SEARCH SPACE */
		for (i = 0; i < util.DIM; i++) {
			tmx[i] = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmx[i] += rotation[i][j] * x[j];
			}
		}

		/* COMPUTATION core */
		for (i = 0; i < NHIGHPEAKS21; i++) {
			tmp2 = 0.;
			for (j = 0; j < util.DIM; j++) {
				tmp = (tmx[j] - Xlocal[j][i]);
				tmp2 += arrScales[i][j] * tmp * tmp;
			}
			tmp2 = peakvalues[i] * Math.exp(fac * tmp2);
			f = util.fmax(f, tmp2);
		}

		f = 10. - f;
		if (f > 0) {
			Ftrue = Math.log(f) / a;
			Ftrue = Math.pow(Math.exp(Ftrue
					+ (0.49 * (Math.sin(Ftrue) + Math.sin(0.79 * Ftrue)))), a);
		} else if (f < 0) {
			Ftrue = Math.log(-f) / a;
			Ftrue = -Math.pow(
					Math.exp(Ftrue
							+ (0.49 * (Math.sin(0.55 * Ftrue) + Math
									.sin(0.31 * Ftrue)))), a);
		} else {
			Ftrue = f;
		}

		Ftrue *= Ftrue;
		Ftrue += Fadd;
		Fval = Ftrue; /* without noise */
		res = Fval;

		return res;
	}

}
