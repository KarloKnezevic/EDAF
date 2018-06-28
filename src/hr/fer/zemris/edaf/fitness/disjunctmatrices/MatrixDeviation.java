package hr.fer.zemris.edaf.fitness.disjunctmatrices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MatrixDeviation {

	private int t;
	private List<Integer> indexes;
	private List<Set<Integer>> X;

	public MatrixDeviation(int t) {
		this.t = t;
		indexes = new ArrayList<Integer>(t);
		
		for (int i = 0; i < t; i++) {
			indexes.add(0);
		}
		
		X = new ArrayList<Set<Integer>>();
	}

	public double computeDeviation(byte[] matrix, int columns) {
		
		//if any element in it, clear
		X.clear();
		
		//make support vectors
		int rows = matrix.length / columns;
		for (int column = 0; column < columns; column++) {
			
			Set<Integer> support = new HashSet<Integer>();
			for (int row = 0; row < rows; row++) {
				
				if (matrix[rows*column + row] == 1) {
					support.add(row);
				}
			}
			
			X.add(support);
		}
		
		return deviation(columns, t, 0, indexes);
	}

	private int deviation(int m, int n, int startPosition, List<Integer> support) {
		if (n == 0) {
			return deltaDeviation(support);
		}

		int sum = 0;
		
		/*
		 * Calculate all t permutations
		 */
		for (int i = startPosition; i <= m - n; i++) {
			support.set(support.size() - n, i);
			sum += deviation(m, n - 1, i + 1, support);
		}

		return sum;
	}

	private int deltaDeviation(List<Integer> support) {
		
		Set<Integer> union = new HashSet<Integer>();
		
		//make union
		for (Integer i : support) {
			union.addAll(X.get(i));
		}
		
		int delta = 0;
		for (int i = 0; i < X.size(); i++) {
			if (!support.contains(i) && union.containsAll(X.get(i))) {
				delta++;
			}
		}
		
		return delta;
	}

}
