package hr.fer.zemris.edaf.fitness.disjunctmatrices;

import java.util.List;
import java.util.Set;

public interface IMDeviation {

	public double compute(List<Integer> supportIndexes, List<Set<Integer>> support);

}
