package hr.fer.zemris.edaf.workenvironment.disjunctmatrices;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.fitness.disjunctmatrices.MatrixDeviation;
import hr.fer.zemris.edaf.fitness.disjunctmatrices.TDeviation;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.Binary;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

public class Problem1 extends Evaluation {

	private MatrixDeviation fit;

	private int columns;

	public Problem1(String[] args) {
		super(args);
	}

	@Override
	public void init(Algorithm algorithm) {

		int t = Integer.parseInt(cmdArgs[0]);

		columns = Integer.parseInt(cmdArgs[1]);

		// fitness
		fit = new MatrixDeviation(new TDeviation(), t);

		algorithm.run();
	}

	@Override
	public void evaluate(Individual individual) {

		final Binary b = (Binary) individual;

		individual.setFitness(fit.computeDeviation(b.getBits(), columns));

	}

}
