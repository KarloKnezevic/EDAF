package hr.fer.zemris.edaf.workenvironment;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.genotype.Individual;

import java.util.Observable;

/**
 * Evaluation.
 * 
 * INFORMS (FRAMEWORK EXECUTOR) OF CHANGES.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public abstract class Evaluation extends Observable {

	protected String[] cmdArgs;

	public Evaluation(String[] args) {
		cmdArgs = args;
	}

	public abstract void init(Algorithm algorithm);

	public abstract void evaluate(Individual individual);

	public void evaluate(Individual[] individuals) {

		for (int i = 0; i < individuals.length; i++) {
			evaluate(individuals[i]);
		}

	}

}