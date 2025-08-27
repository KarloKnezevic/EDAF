package hr.fer.zemris.edaf.statistics.distributions;

import java.util.Random;

/**
 * Distribution.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Distribution {

	protected Random rand;

	public Distribution(Random rand) {
		this.rand = rand;
	}

	public GaussianDistribution getGaussianDistribution() {
		return new GaussianDistribution(rand);
	}

}
