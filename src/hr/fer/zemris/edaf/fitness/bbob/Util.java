package hr.fer.zemris.edaf.fitness.bbob;

/**
 * Util. Defined in benchmarkshelper.c.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Util implements IUtil {

	public final double TOL = 1E-8;

	public int seed = -1;

	public int seedn = -1;

	public static double[] gval;

	public static double[] gval2;

	public static double[] gvect;

	public static double[] uniftmp;

	public static double[] tmpvect;

	// ***********************

	public int DIM;

	public int trialid;

	public double[] peaks;

	public double[] Xopt;

	public double Fopt;

	public boolean isInitDone;

	// ***********************

	public Util(int DIM, int instanceId) {
		this.DIM = DIM;
		trialid = instanceId;
		isInitDone = false;
	}

	@Override
	public double round(double a) {
		return Math.floor(a + 0.5);
	}

	@Override
	public double fmin(double a, double b) {
		return b < a ? b : a;
	}

	@Override
	public double fmax(double a, double b) {
		return b > a ? b : a;
	}

	@Override
	public void unif(double[] r, int N, int inseed) {
		int aktseed;
		int tmp;
		final int[] rgrand = new int[32];
		int aktrand;
		int i;

		if (inseed < 0) {
			inseed = -inseed;
		}
		if (inseed < 1) {
			inseed = 1;
		}
		aktseed = inseed;

		for (i = 39; i >= 0; i--) {

			tmp = (int) Math.floor((double) aktseed / (double) 127773);
			aktseed = (16807 * (aktseed - (tmp * 127773))) - (2836 * tmp);
			if (aktseed < 0) {
				aktseed = aktseed + 2147483647;
			}
			if (i < 32) {
				rgrand[i] = aktseed;
			}

		}
		aktrand = rgrand[0];

		for (i = 0; i < N; i++) {

			tmp = (int) Math.floor((double) aktseed / (double) 127773);
			aktseed = (16807 * (aktseed - (tmp * 127773))) - (2836 * tmp);

			if (aktseed < 0) {
				aktseed = aktseed + 2147483647;
			}
			tmp = (int) Math.floor((double) aktrand / (double) 67108865);
			aktrand = rgrand[tmp];
			rgrand[tmp] = aktseed;
			r[i] = aktrand / 2.147483647e9;

			if (r[i] == 0.) {
				System.err.println("Warning: zero sampled(?), set to 1e-99.\n");
				r[i] = 1e-99;
			}

		}

	}

	@Override
	public void gauss(double[] g, int N, int seed) {
		int i;

		unif(uniftmp, 2 * N, seed);

		for (i = 0; i < N; i++) {
			g[i] = Math.sqrt(-2 * Math.log(uniftmp[i]))
					* Math.cos(2 * Math.PI * uniftmp[N + i]);
			if (g[i] == 0.) {
				g[i] = 1e-99;
			}
		}

	}

	@Override
	public void computeXopt(int seed, int _DIM) {
		int i;

		unif(tmpvect, _DIM, seed);
		for (i = 0; i < _DIM; i++) {
			Xopt[i] = ((8 * Math.floor(1e4 * tmpvect[i])) / 1e4) - 4;
			if (Xopt[i] == 0.0) {
				Xopt[i] = -1e-5;
			}
		}

	}

	@Override
	public void monotoneTFosc(double[] f) {
		final double a = 0.1;
		int i;
		for (i = 0; i < DIM; i++) {
			if (f[i] > 0) {

				f[i] = Math.log(f[i]) / a;
				f[i] = Math.pow(
						Math.exp(f[i]
								+ (0.49 * (Math.sin(f[i]) + Math
										.sin(0.79 * f[i])))), a);

			} else if (f[i] < 0) {

				f[i] = Math.log(-f[i]) / a;
				f[i] = -Math.pow(
						Math.exp(f[i]
								+ (0.49 * (Math.sin(0.55 * f[i]) + Math
										.sin(0.31 * f[i])))), a);

			}
		}

	}

	@Override
	public double[][] reshape(double[][] B, double[] vector, int m, int n) {
		int i, j;
		for (i = 0; i < m; i++) {
			for (j = 0; j < n; j++) {
				B[i][j] = vector[(j * m) + i];
			}
		}
		return B;
	}

	@Override
	public void computeRotation(double[][] B, int seed, int _DIM) {
		double prod;
		/* Loop over pairs of column vectors */
		int i, j, k;

		gauss(gvect, _DIM * _DIM, seed);
		reshape(B, gvect, _DIM, _DIM);
		/* 1st coordinate is row, 2nd is column. */

		for (i = 0; i < _DIM; i++) {
			for (j = 0; j < i; j++) {

				prod = 0;
				for (k = 0; k < _DIM; k++) {
					prod += B[k][i] * B[k][j];
				}
				for (k = 0; k < _DIM; k++) {
					B[k][i] -= prod * B[k][j];
				}

			}

			prod = 0;

			for (k = 0; k < _DIM; k++) {
				prod += B[k][i] * B[k][i];
			}

			for (k = 0; k < _DIM; k++) {
				B[k][i] /= Math.sqrt(prod);
			}
		}

	}

	@Override
	public double myrand() {
		if (seed == -1) {
			/* cannot be larger than 1e9 */
			seed = (int) (System.currentTimeMillis() / 1000) % 1000000000;
		}

		seed++;
		if (seed > 1e9) {
			seed = 1;
		}
		unif(uniftmp, 1, seed);
		return uniftmp[0];
	}

	@Override
	public double randn() {
		if (seedn == -1) {
			/* cannot be larger than 1e9 */
			seed = (int) (System.currentTimeMillis() / 1000) % 1000000000;
		}

		seedn++;
		if (seedn > 1e9) {
			seedn = 1;
		}
		gauss(uniftmp, 1, seedn);
		return uniftmp[0];
	}

	@Override
	public double FGauss(double Ftrue, double beta) {
		double Fval = Ftrue * Math.exp(beta * randn());
		Fval += 1.01 * TOL;
		if (Ftrue < TOL) {
			Fval = Ftrue;
		}
		return Fval;
	}

	@Override
	public double FUniform(double Ftrue, double alpha, double beta) {
		double Fval = Math.pow(myrand(), beta) * Ftrue
				* fmax(1., Math.pow(1e9 / (Ftrue + 1e-99), alpha * myrand()));
		Fval += 1.01 * TOL;
		if (Ftrue < TOL) {
			Fval = Ftrue;
		}
		return Fval;
	}

	@Override
	public double FCauchy(double Ftrue, double alpha, double p) {
		double Fval;
		final double tmp = randn() / Math.abs(randn() + 1e-199);
		/*
		 * tmp is so as to actually do the calls to randn in order for the
		 * number of calls to be the same as in the Matlab code.
		 */
		if (myrand() < p) {
			Fval = Ftrue + (alpha * fmax(0., 1e3 + tmp));
		} else {
			Fval = Ftrue + (alpha * 1e3);
		}

		Fval += 1.01 * TOL;
		if (Ftrue < TOL) {
			Fval = Ftrue;
		}
		return Fval;
	}

	@Override
	public int compare_doubles(int a, int b) {
		final double temp = peaks[a] - peaks[b];
		if (temp > 0) {
			return 1;
		} else if (temp < 0) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public void initUtil() {
		gval = new double[1];
		gval2 = new double[1];
		gvect = new double[DIM * DIM];
		uniftmp = new double[2 * DIM * DIM];
		tmpvect = new double[DIM];
		Xopt = new double[DIM];
	}

	@Override
	public void finiUtil() {
		gval = null;
		gval2 = null;
		gvect = null;
		uniftmp = null;
		tmpvect = null;
		Xopt = null;
	}

	@Override
	public double computeFopt(int _funcId, int _trialId) {
		int rseed, rrseed;
		if (_funcId == 4) {
			rseed = 3;
		} else if (_funcId == 18) {
			rseed = 17;
		} else if ((_funcId == 101) || (_funcId == 102) || (_funcId == 103)
				|| (_funcId == 107) || (_funcId == 108) || (_funcId == 109)) {
			rseed = 1;
		} else if ((_funcId == 104) || (_funcId == 105) || (_funcId == 106)
				|| (_funcId == 110) || (_funcId == 111) || (_funcId == 112)) {
			rseed = 8;
		} else if ((_funcId == 113) || (_funcId == 114) || (_funcId == 115)) {
			rseed = 7;
		} else if ((_funcId == 116) || (_funcId == 117) || (_funcId == 118)) {
			rseed = 10;
		} else if ((_funcId == 119) || (_funcId == 120) || (_funcId == 121)) {
			rseed = 14;
		} else if ((_funcId == 122) || (_funcId == 123) || (_funcId == 124)) {
			rseed = 17;
		} else if ((_funcId == 125) || (_funcId == 126) || (_funcId == 127)) {
			rseed = 19;
		} else if ((_funcId == 128) || (_funcId == 129) || (_funcId == 130)) {
			rseed = 21;
		} else {
			rseed = _funcId;
		}

		rrseed = rseed + (10000 * _trialId);
		gauss(gval, 1, rrseed);
		gauss(gval2, 1, rrseed + 1);
		return fmin(
				1000.,
				fmax(-1000., (round((100. * 100. * gval[0]) / gval2[0]) / 100.)));
	}

	@Override
	public void setNoiseSeed(int _seed, int _seedn) {
		seed = _seed;
		seedn = _seedn;
	}

}
