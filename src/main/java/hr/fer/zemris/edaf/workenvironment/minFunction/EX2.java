package hr.fer.zemris.edaf.workenvironment.minFunction;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.fitness.deceptive.BitLenTrapOnesDeception;
import hr.fer.zemris.edaf.fitness.deceptive.IDeceptive;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.Binary;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

/**
 * EX2.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class EX2 extends Evaluation {

	private IDeceptive f;

	public EX2(String[] args) {
		super(args);
	}

	@Override
	public void init(Algorithm algorithm) {

		f = new BitLenTrapOnesDeception();

		algorithm.run();

	}

	@Override
	public void evaluate(Individual individual) {

		if (!(individual instanceof Binary)) {
			MSGPrinter.printERROR(System.err,
					"Deceptive functions only for binary genotype", true, -1);
		}

		final Binary b = (Binary) individual;

		individual.setFitness(f.computeDeceptive(b.getBits()));

	}

}
