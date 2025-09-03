package com.knezevic.edaf.examples.crypto;

/**
 * A utility class for cryptographic calculations.
 */
public class CryptoUtils {

    /**
     * Computes the Walsh-Hadamard Transform of a boolean function.
     *
     * @param truthTable The truth table of the boolean function.
     * @param n          The number of variables in the function.
     * @param result     An array to store the transform result.
     * @return The maximum absolute value in the Walsh spectrum.
     */
    public static int walshHadamardTransform(int[] truthTable, int n, int[] result) {
        int max = 0;
        for (int i = 0; i < (1 << n); ++i) {
            result[i] = (truthTable[i] == 0) ? 1 : -1;
        }

        for (int i = 1; i <= n; ++i) {
            int m = (1 << i);
            int halfM = m / 2;
            for (int r = 0; r < (1 << n); r += m) {
                int t1 = r;
                int t2 = r + halfM;
                for (int j = 0; j < halfM; ++j, ++t1, ++t2) {
                    int a = result[t1];
                    int b = result[t2];
                    result[t1] = a + b;
                    result[t2] = a - b;

                    if (Math.abs(result[t1]) > max) {
                        max = Math.abs(result[t1]);
                    }
                    if (Math.abs(result[t2]) > max) {
                        max = Math.abs(result[t2]);
                    }
                }
            }
        }
        return max;
    }

    /**
     * Computes the Algebraic Normal Form (ANF) of a boolean function.
     *
     * @param truthTable The truth table of the boolean function.
     * @param n          The number of variables in the function.
     * @param result     An array to store the ANF coefficients.
     */
    public static void algebraicNormalForm(int[] truthTable, int n, int[] result) {
        for (int i = 0; i < (1 << n); ++i) {
            result[i] = truthTable[i];
        }

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < (1 << n); j += (1 << (i + 1))) {
                for (int k = 0; k < (1 << i); ++k) {
                    result[j + k + (1 << i)] = result[j + k] ^ result[j + k + (1 << i)];
                }
            }
        }
    }

    /**
     * Computes the autocorrelation of a boolean function.
     *
     * @param truthTable The truth table of the boolean function.
     * @param n          The number of variables in the function.
     * @param result     An array to store the autocorrelation values.
     */
    public static void autocorrelation(int[] truthTable, int n, int[] result) {
        for (int i = 0; i < (1 << n); i++) {
            int res = 0;
            for (int j = 0; j < (1 << n); j++) {
                int tmp = truthTable[j] ^ truthTable[i ^ j];
                if (tmp == 1) {
                    tmp = -1;
                } else if (tmp == 0) {
                    tmp = 1;
                }
                res += tmp;
            }
            result[i] = res;
        }
    }

    /**
     * Calculates the Hamming weight of an integer.
     *
     * @param x The integer.
     * @return The Hamming weight.
     */
    public static int hammingWeight(int x) {
        int res;
        for (res = 0; x > 0; x = x >> 1) {
            res += (x % 2);
        }
        return res;
    }
}
