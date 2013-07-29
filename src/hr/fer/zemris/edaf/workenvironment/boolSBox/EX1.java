package hr.fer.zemris.edaf.workenvironment.boolSBox;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.fitness.bool.BooleanFunctionOptimization;
import hr.fer.zemris.edaf.fitness.bool.IBool;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.Binary;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

/**
 * Boolean function optimization for PhD student in mentorship of Domagoj
 * Jakobovic, PhD.C.S., domagoj.jakobovic@fer.hr
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class EX1 extends Evaluation {

	private IBool bool;

	public EX1(String[] args) {
		super(args);
	}

	@Override
	public void init(Algorithm algorithm) {

		if (!(algorithm.getGenotype().getIndividual() instanceof Binary)) {
			MSGPrinter.printERROR(System.err,
					"Boolean functions only for binary genotype", true, -1);
		}

		final Binary b = (Binary) algorithm.getGenotype().getIndividual();

		final int n = (int) (Math.log(b.getGenotypeLength()) / Math.log(2));

		if (Math.pow(2, n) != b.getGenotypeLength()) {
			MSGPrinter.printERROR(System.err,
					"Genotype length must be potention of 2", true, -1);
		}

		bool = new BooleanFunctionOptimization(n, new int[] { 1, 1, 1, 1, 1, 1,
				1, 1, 1 });

		algorithm.run();

	}

	@Override
	public void evaluate(Individual individual) {

		if (!(individual instanceof Binary)) {
			MSGPrinter.printERROR(System.err,
					"Boolean functions only for binary genotype", true, -1);
		}

		final Binary b = (Binary) individual;

		individual.setFitness(-1 * bool.computeBool(b.getIntBits()));

	}
}