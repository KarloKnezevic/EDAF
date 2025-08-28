package hr.fer.zemris.edaf.genotype.binary.crossing;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.Crossing;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.Binary;

import java.util.Random;

/**
 * BINARY ONE POINT CROSSING.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class CrxOnePoint extends Crossing {

	public CrxOnePoint(double crossingProbability, Random rand) {
		super("CrxOnePoint", crossingProbability, rand);
	}

	@Override
	public Individual[] cross(Individual parent1, Individual parent2) {

		if (!(parent1 instanceof Binary) || !(parent2 instanceof Binary)) {
			MSGPrinter
					.printERROR(System.out,
							"CrxOnePoint: individual not instance of binary.",
							true, -1);
		}

		final Binary p1 = (Binary) parent1;
		final Binary p2 = (Binary) parent2;
		final Binary c1 = new Binary(p1.getDecoder(), p1.getRand(),
				p1.getPopulationLen());
		final Binary c2 = new Binary(p2.getDecoder(), p2.getRand(),
				p2.getPopulationLen());

		if (rand.nextDouble() <= crossingProbability) {
			final int hiasma = rand
					.nextInt(p1.getDecoder().getBitsNumber() - 1) + 1;

			for (int i = 0; i < hiasma; i++) {
				c1.setBits(i, p1.getBits()[i]);
				c2.setBits(i, p2.getBits()[i]);
			}

			for (int i = hiasma; i < p1.getDecoder().getBitsNumber(); i++) {
				c1.setBits(i, p2.getBits()[i]);
				c2.setBits(i, p1.getBits()[i]);
			}
		} else {
			for (int i = 0; i < p1.getDecoder().getBitsNumber(); i++) {
				c1.setBits(i, p1.getBits()[i]);
				c2.setBits(i, p2.getBits()[i]);
			}
		}

		return new Individual[] { c1, c2 };
	}
}