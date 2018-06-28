package hr.fer.zemris.edaf.fitness.disjunctmatrices;

import java.util.List;
import java.util.Set;

public class TEpsilonDeviation implements IMDeviation {

	private int columns;

	private int t;

	private long BiColumnsT;

	private TDeviation tdeviation;

	public TEpsilonDeviation(TDeviation tdeviation, int columns, int t) {
		this.columns = columns;
		this.t = t;
		this.BiColumnsT = binomial(columns, t);
		this.tdeviation = tdeviation;
	}

	@Override
	public double compute(List<Integer> supportIndexes, List<Set<Integer>> support) {
		return tdeviation.compute(supportIndexes, support) / (BiColumnsT * (columns - t));
	}

	private long binomial(int n, int k) {
		if (k > n - k)
			k = n - k;

		long b = 1;
		for (int i = 1, m = n; i <= k; i++, m--)
			b = b * m / i;
		return b;
	}

}
