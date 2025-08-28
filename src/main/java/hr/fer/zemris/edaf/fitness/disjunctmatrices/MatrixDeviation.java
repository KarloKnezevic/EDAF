package hr.fer.zemris.edaf.fitness.disjunctmatrices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MatrixDeviation {

	private int t;
	private List<Integer> indexes;
	private List<Set<Integer>> support;

	private IMDeviation deviation;

	public MatrixDeviation(IMDeviation deviation, int t) {
		this.t = t;
		indexes = new ArrayList<Integer>(t);

		this.deviation = deviation;

		for (int i = 0; i < t; i++) {
			indexes.add(0);
		}

		support = new ArrayList<Set<Integer>>();
	}

	public double computeDeviation(byte[] matrix, int columns) {
		computeSupport(matrix, columns);

		return calculate(columns, t, 0, indexes);
	}

	private void computeSupport(byte[] matrix, int columns) {

		// if any element in it, clear
		support.clear();

		// make support vectors
		int rows = matrix.length / columns;
		for (int column = 0; column < columns; column++) {

			Set<Integer> columnSupport = new HashSet<Integer>();
			for (int row = 0; row < rows; row++) {

				if (matrix[rows * column + row] == 1) {
					columnSupport.add(row);
				}
			}

			support.add(columnSupport);
		}
	}

	private double calculate(int m, int n, int startPosition, List<Integer> supportIndexes) {
		if (n == 0) {
			return deviation.compute(supportIndexes, support);
		}

		int sum = 0;

		/*
		 * Calculate all t permutations
		 */
		for (int i = startPosition; i <= m - n; i++) {
			supportIndexes.set(supportIndexes.size() - n, i);
			sum += calculate(m, n - 1, i + 1, supportIndexes);
		}

		return sum;
	}

}
