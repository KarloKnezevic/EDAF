package hr.fer.zemris.edaf.fitness.disjunctmatrices;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TFDeviation implements IMDeviation {

	private int f;

	public TFDeviation(int f) {
		this.f = f;
	}

	@Override
	public double compute(List<Integer> supportIndexes, List<Set<Integer>> support) {
		Set<Integer> union = new HashSet<Integer>();

		// make union
		for (Integer i : supportIndexes) {
			union.addAll(support.get(i));
		}

		int delta = 0;
		for (int i = 0; i < support.size(); i++) {
			if (!supportIndexes.contains(i) && union.containsAll(support.get(i))) {
				delta++;
			}
		}

		return delta > f ? 1 : 0;
	}

}
