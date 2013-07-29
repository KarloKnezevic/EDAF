package hr.fer.zemris.edaf.genotype.binary.mutation;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.Mutation;
import hr.fer.zemris.edaf.genotype.binary.Binary;

import java.util.Random;

/**
 * BINARY SIMPLE MUTATION.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class MutSimple extends Mutation {

	public MutSimple(double mutationProbability, Random rand) {
		super("MutSimple", mutationProbability, rand);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void mutate(Individual individual) {

		if (!(individual instanceof Binary)) {
			MSGPrinter
					.printERROR(System.out,
							"CrxOnePoint: individual not instance of binary.",
							true, -1);
		}

		final Binary b = (Binary) individual;

		for (int i = 0; i < b.getDecoder().getBitsNumber(); i++) {
			if (rand.nextDouble() <= mutationProbability) {
				b.setBits(i, (byte) (1 - b.getBits()[i]));
			}
		}
	}
}