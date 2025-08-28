package hr.fer.zemris.edaf.fitness.bool;

/**
 * Boolean Function Optimization
 * 
 * Boolean functions have widespread use in symmetric cryptography. Basically,
 * ciphers in symmetric cryptography can be divided to stream and block ciphers.
 * Shannon defined two basic principles that a computationally secure cryp
 * tosystem should follow to be the confusion and diffusion principles.
 * Diffusion principle serves to propagate the influence of each bit of
 * plaintext and key to as many bits of ciphertext as possible. Confusion
 * principle is used to make the relation between the key and ciphertext as
 * complex as possible. Confusion is obtained by non-linear transformations. In
 * block ciphers, confusion comes from S-boxes where S-box can be regarded as a
 * vectorial Boolean function. In stream ciphers Boolean functions are used to
 * introduce non-linearity (in fact, they are the only non-linear elements in
 * stream ciphers) into otherwise linear systems.
 * 
 * This code is used for PhD research supervised by Prof. Domagoj Jakobovic,
 * Ph.D.C.S.
 * 
 * @author Ivo Majic, ivo.majic2@fer.hr
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @author Domagoj Jakobovic, domagoj.jakobovic@fer.hr
 * 
 */
public class BooleanFunctionOptimization implements IBool {

	private final int N;

	// length = 9
	private final int[] fitnessMask;

	// global utility
	private int Nm, Ns;

	public BooleanFunctionOptimization(int n, int[] fitnessMask) {
		N = n;
		this.fitnessMask = fitnessMask;
	}

	@Override
	public int computeBool(int[] genotype) {

		int fitness = 0;

		// UTILITY
		int wt_max = 0;
		final int[] wt_rez = new int[1 << N];
		if ((fitnessMask[1] == 1) || (fitnessMask[2] == 1)
				|| (fitnessMask[3] == 1)) {
			wt_max = wt(genotype, N, wt_rez);
		}

		final int[] anf_rez = new int[1 << N];
		if (fitnessMask[4] == 1) {
			anf(genotype, N, anf_rez);
		}

		final int[] ac_rez = new int[1 << N];
		if ((fitnessMask[6] == 1)
				|| ((fitnessMask[7] == 1) | (fitnessMask[8] == 1))) {
			ac(genotype, N, ac_rez);
		}

		// 1. BALANISRANOST
		if (fitnessMask[0] == 1) {
			fitness += balansiranost(genotype, N);
		}

		// 2. NELINEARNOST
		if (fitnessMask[1] == 1) {
			fitness += nelinearnost(wt_max, N);
		}

		// 3. KORELACIJSKI IMUNITET
		if (fitnessMask[2] == 1) {
			fitness += korelacijskiImunitet(wt_rez, N);
		}

		// 4. WALSHOV SPEKTAR
		if (fitnessMask[3] == 1) {
			fitness += walshovSpektar(wt_rez, N);
		}

		// 5. ALGEBARSKI STUPANJ
		if (fitnessMask[4] == 1) {
			fitness += algebarskiStupanj(anf_rez, N);
		}

		// 6. ALGEBARSKI IMUNITET
		if (fitnessMask[5] == 1) {
			fitness += algebarskiImunitet(genotype, N);
		}

		// 7. KARAKTERISTIKA PROPAGACIJE
		if (fitnessMask[6] == 1) {
			fitness += karakteristikaPropagacije(ac_rez, N);
		}

		// 8. SUMA KVADRATA INDIKATOR
		if (fitnessMask[7] == 1) {
			fitness += sumaKvadrataIndikator(ac_rez, N);
		}

		// 9. AC
		if (fitnessMask[8] == 1) {
			fitness += AC(ac_rez, N);
		}

		return fitness;

	}

	/*
	 * UTILITY
	 */

	private void ac(int[] tt, int n, int[] ac_rez) {

		int i, j, tmp = 0, res = 0;

		for (i = 0; i < (1 << n); i++) {
			for (j = 0; j < (1 << n); j++) {
				tmp = tt[j] ^ tt[i ^ j];
				if (tmp == 1) {
					tmp = -1;
				} else if (tmp == 0) {
					tmp = 1;
				}
				res = res + tmp;
			}
			ac_rez[i] = res;
			res = 0;
		}

	}

	private void anf(int[] tt, int n, int[] rez) {
		int i, j, k;

		final int[] u = new int[(1 << N) >> 1];
		final int[] t = new int[(1 << N) >> 1];

		for (i = 0; i < (1 << n); ++i) {
			rez[i] = tt[i];
		}
		for (i = 0; i < ((1 << n) >> 1); ++i) {
			u[i] = t[i] = 0;
		}

		for (i = 0; i < n; ++i) {
			for (j = 0; j < ((1 << n) >> 1); ++j) {
				t[j] = rez[2 * j];
				u[j] = (rez[2 * j] == rez[(2 * j) + 1]) ? 0 : 1;
			}
			for (k = 0; k < ((1 << n) >> 1); ++k) {
				rez[k] = t[k];
				rez[((1 << n) >> 1) + k] = u[k];
			}
		}

	}

	private int wt(int[] tt, int n, int[] rez) {

		int i, j, m, halfm, t1, t2, r, a, b, max = 0;

		for (i = 0; i < (1 << n); ++i) {
			rez[i] = (tt[i] == 0) ? 1 : -1;
		}

		for (i = 1; i <= n; ++i) {
			m = (1 << i);
			halfm = m / 2;
			for (r = 0; r < (1 << n); r += m) {
				t1 = r;
				t2 = r + halfm;
				for (j = 0; j < halfm; ++j, ++t1, ++t2) {
					a = rez[t1];
					b = rez[t2];
					rez[t1] = a + b;
					rez[t2] = a - b;

					if (Math.abs(rez[t1]) > max) {
						max = Math.abs(rez[t1]);
					}
					if (Math.abs(rez[t2]) > max) {
						max = Math.abs(rez[t2]);
					}
				}
			}
		}

		return max;

	}

	private int hammingWeight(int x) {

		int res;
		for (res = 0; x > 0; x = x >> 1) {
			res = res + (x % 2);
		}
		return res;

	}

	private int[] getMonomials(int n, int d, int[] res) {

		// N je globalna varijabla Nm

		int i, k;
		for (Nm = 0, k = 0; k <= d; ++k) {
			Nm = Nm + choose(n, k);
		}
		res = new int[Nm];
		for (k = 0, i = 0; i < (1 << n); i++) {
			if (hammingWeight(i) <= d) {
				res[k++] = i;
			}
		}

		return res;

	}

	private int choose(int n, int k) {

		int i, num = 1, den = 1;

		if ((k < 0) || (k > n)) {
			return 0;
		}

		for (i = 0; i < k; i++) {
			num *= n--;
			den *= (k - i);
		}

		return (num / den);

	}

	private void sortIncreasingDeg(int[] v, int len) {

		int i, j, tmp;
		for (i = 0; i < (len - 1); i++) {
			for (j = i + 1; j < len; j++) {
				if (hammingWeight(v[j]) < hammingWeight(v[i])) {
					tmp = v[j];
					v[j] = v[i];
					v[i] = tmp;
				}
			}
		}

	}

	private int[] getSupport(int[] tt, int n, int[] res, int b) {

		// Ns je globalna varijabla

		int i, k, len;
		len = 1 << n;

		for (Ns = 0, i = 0; i < len; i++) {
			Ns = Ns + (tt[i] != b ? 1 : 0);
		}

		res = new int[Ns];

		for (k = 0, i = 0; i < len; i++) {
			if (tt[i] != b) {
				res[k++] = i;
			}
		}

		return res;

	}

	private MAT getMatrix(int[] tt, int n, int[] monomials, int ai, int b) {

		// Ns je globalna varijabla

		int i, j, len;
		Ns = 0;
		int[] support = null;

		len = 1 << n;
		MAT m = new MAT();

		support = getSupport(tt, n, support, b);

		if ((Ns == 0) || (Ns == len)) {
			m = null;
		} else {
			if (Nm > Ns) {
				m.initializeMat(Nm, Nm);
			} else {
				m.initializeMat(Nm, Ns);
			}
			for (i = 0; i < Nm; i++) {
				for (j = 0; j < Ns; j++) {
					m.elements[i][j] = preceq(monomials[i], support[j]);
				}
			}
		}

		return m;

	}

	int preceq(int a, int b) {

		int res = 1;

		while (((a > 0) || (b > 0)) && (res == 1)) {
			if ((a % 2) > (b % 2)) {
				res = 0;
			}
			a >>= 1;
			b >>= 1;
		}

		return res;

	}

	int solveMatrix(MAT m, int[] monomials, int b) {

		int i, j, l, res;
		// varijable koje se nikad za ništa korisno ne koriste
		// int processed_lines, zero_lines;
		int[] deg;
		deg = new int[m.n];

		for (res = 0, i = 0; i < m.n; i++) {
			deg[i] = hammingWeight(monomials[i]);
			if (deg[i] > res) {
				res = deg[i];
			}
		}

		// processed_lines = zero_lines = 0;

		for (i = 0; i < m.n; i++) {
			for (j = 0; (j < m.m) && (m.elements[i][j] == 0); j++) {
				;
			}
			if (j == m.m) {
				// zero_lines++;
				if ((deg[i] < res) && (deg[i] != 0)) {
					res = deg[i];
				}
			} else {
				// processed_lines++;
				if ((i != j) && (i < m.m) && (j < m.m)) {
					m.swapColums(i, j);
				}
				for (l = i + 1; (l < m.n) && (i < m.m); l++) {
					if ((i < m.m) && (m.elements[l][i] != 0)) {
						m.addLine(l, i);
						deg[l] = (deg[i] > deg[l]) ? deg[i] : deg[l];
					}
				}
			}
		}

		return res;

	}

	/*
	 * 1. BALANSIRANOST
	 */
	private int balansiranost(int[] tt, int n) {

		int i, rez = 0;

		for (i = 0; i < (1 << n); i++) {
			if (tt[i] == 0) {
				rez = rez + 1;
			}
		}

		if (rez == ((1 << n) / 2)) {
			return 1;
		} else if (rez < ((1 << n) / 2)) {
			if (rez == 0) {
				rez = 1;
			}
			return (int) ((((1 << n) - rez) / (1. * rez)) * (-50));

		} else if (rez > ((1 << n) / 2)) {
			if (rez == 256) {
				rez = 255;
			}
			return (int) (((1. * rez) / ((1 << n) - rez)) * (-50));
		}

		return 0;

	}

	/*
	 * 2. NELINEARNOST
	 */
	private int nelinearnost(int max, int n) {

		return (((1 << n) - max) / 2);

	}

	/*
	 * 3. KORELACIJSKI IMUNITET
	 */
	private int korelacijskiImunitet(int[] wh, int n) {

		int i, red = 1;

		do {
			for (i = 1; i < (1 << n); i++) {
				if (red == hammingWeight(i)) {
					if (wh[i] != 0) {
						return red - 1;
					}
				}
			}
			red++;
		} while (red <= n);

		return red - 2;

	}

	/*
	 * 4. WALSHOV SPEKTAR
	 */
	private int walshovSpektar(int[] wh, int n) {

		int i;
		double rez = 0;
		float pot = (float) 0.0, x = 0;
		pot = (float) n / 2;
		x = (float) Math.pow((float) 2.0, pot);
		for (i = 0; i < (1 << n); i++) {
			pot = Math.abs((float) wh[i]) - x;
			rez = rez + Math.pow(pot, 2);
		}
		rez = Math.sqrt(rez);
		return (int) (rez / (-2));

	}

	/*
	 * 5. ALGEBARSKI STUPANJ
	 */
	private int algebarskiStupanj(int[] anf, int n) {

		int i, tmp, weight, deg;

		if (anf[(1 << n) - 1] != 0) {
			deg = n;
		} else {
			for (deg = 0, i = 1; i < ((1 << n) - 1); ++i) {
				if (anf[i] != 0) {
					for (weight = 0, tmp = i; tmp > 0; tmp >>= 1) {
						weight = weight + (tmp % 2);
					}
					if (weight > deg) {
						deg = weight;
					}
				}
			}
		}

		return deg;

	}

	/*
	 * 6. ALGEBARSKI IMUNITET
	 */
	private int algebarskiImunitet(int[] tt, int n) {
		MAT m0 = null;
		MAT m1 = null;
		int[] monomials = null;
		int deg;
		Nm = 0;
		int a, b, rez = 0;

		deg = (n >> 1) + (n % 2);
		monomials = getMonomials(n, deg, monomials);
		sortIncreasingDeg(monomials, Nm);
		m0 = getMatrix(tt, n, monomials, deg, 0);

		if (m0 == null) {
			rez = 0;
		} else {
			m1 = getMatrix(tt, n, monomials, deg, 1);
			a = solveMatrix(m0, monomials, 0);
			b = solveMatrix(m1, monomials, 1);
			rez = (a < b) ? a : b;
		}

		return rez;

	}

	/*
	 * 7. KARAKTERISTIKA PROPAGACIJE
	 */
	private int karakteristikaPropagacije(int[] ac_rez, int n) {

		int i, red = 1;
		do {
			for (i = 1; i < (1 << n); i++) {
				if (red == hammingWeight(i)) {
					if (ac_rez[i] != 0) {
						return red - 1;
					}
				}
			}
			red++;
		} while (red <= n);

		return red - 2;

	}

	/*
	 * 8. SUMA KVADRATA INDIKATOR
	 */
	private int sumaKvadrataIndikator(int[] ac_rez, int n) {

		int i, suma = 0;
		for (i = 0; i < (1 << n); i++) {
			suma = suma + (ac_rez[i] * ac_rez[i]);
		}
		suma = (int) ((Math.sqrt((double) suma / n)) / 2);
		return suma * (-1);

	}

	/*
	 * 9. AC
	 */
	private int AC(int[] ac_rez, int n) {

		int i, max = 0;
		int tmp = 0;
		tmp = n / 2;

		for (i = 1; i < (1 << n); i++) {
			if (Math.abs(ac_rez[i]) > max) {
				max = Math.abs(ac_rez[i]);
			}
		}

		return max / (-tmp);

	}

	private class MAT {

		double[][] elements;

		// row
		int n;

		// col
		int m;

		public void initializeMat(int nrow, int ncol) {
			elements = new double[nrow][];
			for (int i = 0; i < elements.length; i++) {
				elements[i] = new double[ncol];
			}
		}

		public void swapColums(int a, int b) {
			for (int i = 0; i < n; i++) {
				final double tmp = elements[i][a];
				elements[i][a] = elements[i][b];
				elements[i][b] = tmp;
			}
		}

		public void addLine(int dst, int src) {
			for (int j = 0; j < m; j++) {
				elements[dst][j] = (elements[dst][j] + elements[src][j]) % 2;
			}
		}

	}
}
