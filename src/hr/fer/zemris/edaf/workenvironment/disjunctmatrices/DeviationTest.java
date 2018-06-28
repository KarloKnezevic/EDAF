package hr.fer.zemris.edaf.workenvironment.disjunctmatrices;

import hr.fer.zemris.edaf.fitness.disjunctmatrices.MatrixDeviation;

public class DeviationTest {
	public static void main(String[] args) {

		MatrixDeviation deviation = new MatrixDeviation(3);

		System.out.println(deviation
				.computeDeviation(new byte[] { 
						1,1,1,0,1,
						0,1,0,0,1,
						0,0,1,1,1,
						1,0,1,0,1
						}, 4));

	}
}
