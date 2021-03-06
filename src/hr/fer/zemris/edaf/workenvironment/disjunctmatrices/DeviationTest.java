package hr.fer.zemris.edaf.workenvironment.disjunctmatrices;

import hr.fer.zemris.edaf.fitness.disjunctmatrices.MatrixDeviation;
import hr.fer.zemris.edaf.fitness.disjunctmatrices.TDeviation;

public class DeviationTest {
	public static void main(String[] args) {

		MatrixDeviation deviation = new MatrixDeviation(new TDeviation(), 2);

		System.out.println(deviation
				.computeDeviation(new byte[] { 
						1,1,1,0,1,
						0,1,0,0,1,
						0,0,1,1,1,
						1,0,1,0,1
						}, 4));

	}
}
