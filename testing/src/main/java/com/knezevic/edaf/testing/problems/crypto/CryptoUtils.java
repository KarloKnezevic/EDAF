package com.knezevic.edaf.testing.problems.crypto;

public final class CryptoUtils {
    private CryptoUtils() {}

    public static int walshHadamardTransform(int[] f, int n, int[] out) {
        // Copy f into out with mapping {0->1, 1->-1}
        int size = 1 << n;
        for (int i = 0; i < size; i++) {
            out[i] = (f[i] == 0) ? 1 : -1;
        }
        for (int len = 1; len < size; len <<= 1) {
            for (int i = 0; i < size; i += (len << 1)) {
                for (int j = 0; j < len; j++) {
                    int u = out[i + j];
                    int v = out[i + j + len];
                    out[i + j] = u + v;
                    out[i + j + len] = u - v;
                }
            }
        }
        int max = 0;
        for (int v : out) {
            int abs = Math.abs(v);
            if (abs > max) max = abs;
        }
        return max;
    }
}


