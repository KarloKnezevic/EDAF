package hr.fer.zemris.edaf.workenvironment.deceptive;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.fitness.deceptive.BitLenTrapOnesDeception;
import hr.fer.zemris.edaf.fitness.deceptive.IDeceptive;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.Binary;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

/**
 * TEST Expertiments for Master Thesis Deceptive function Function: one bit trap
 * Bits: 256 Dimension: 1
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TESTd extends Evaluation {

	IDeceptive deceptive;

	public TESTd(String[] args) {
		super(args);
	}

	@Override
	public void init(Algorithm algorithm) {

		deceptive = new BitLenTrapOnesDeception();

		algorithm.run();

	}

	@Override
	public void evaluate(Individual individual) {

		if (!(individual instanceof Binary)) {
			MSGPrinter.printERROR(System.err,
					"Deceptive functions only for binary genotype", true, -1);
		}

		final Binary b = (Binary) individual;

		individual.setFitness(deceptive.computeDeceptive(b.getBits()));

	}

}
