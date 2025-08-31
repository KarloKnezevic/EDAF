package com.knezevic.edaf.algorithm.boa.acquisition;

import com.knezevic.edaf.algorithm.boa.surrogate.GaussianProcessSurrogate;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Random;

public class ExpectedImprovement {
    private GaussianProcessSurrogate surrogate;
    private double best_y;
    private Random random;

    public ExpectedImprovement(GaussianProcessSurrogate surrogate, double best_y, Random random) {
        this.surrogate = surrogate;
        this.best_y = best_y;
        this.random = random;
    }

    public Instance find_max(Instances data) throws Exception {
        Instance best_x = null;
        double max_ei = -1;

        for (int i = 0; i < 1000; i++) { // 1000 random samples
            Instance x = new DenseInstance(data.numAttributes());
            x.setDataset(data);
            for (int j = 0; j < data.numAttributes() - 1; j++) {
                x.setValue(j, random.nextDouble());
            }

            double[] pred = surrogate.predict(x);
            double mu = pred[0];
            double sigma = pred[1];

            double ei = 0;
            if (sigma > 0) {
                double z = (mu - best_y) / sigma;
                ei = (mu - best_y) * cdf(z) + sigma * pdf(z);
            }

            if (ei > max_ei) {
                max_ei = ei;
                best_x = x;
            }
        }
        return best_x;
    }

    private double pdf(double x) {
        return Math.exp(-0.5 * x * x) / Math.sqrt(2 * Math.PI);
    }

    private double cdf(double x) {
        return 0.5 * (1 + erf(x / Math.sqrt(2)));
    }

    // erf function approximation
    private double erf(double x) {
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;

        int sign = 1;
        if (x < 0) {
            sign = -1;
        }
        x = Math.abs(x);

        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return sign * y;
    }
}
