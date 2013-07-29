package hr.fer.zemris.edaf.main;

import hr.fer.zemris.edaf.FrameworkExecutor;
import hr.fer.zemris.edaf.xml.ClassPathFrameworkContext;

/**
 * ESTIMATION OF DISTRIBUTION ALGORITHMS FRAMEWORK Estimation of distribution
 * algorithms (EDAs), sometimes called probabilistic model-building genetic
 * algorithms (PMBGAs), are stochastic optimization methods that guide the
 * search for the optimum by building and sampling explicit probabilistic models
 * of promising candidate solutions. Optimization is viewed as a series of
 * incremental updates of a probabilistic model, starting with the model
 * encoding the uniform distribution over admissible solutions and ending with
 * the model that generates only the global optima.
 * 
 * This framework is developed by Karlo Knezevic. It is written as practical
 * part of Master Thesis in University of Zagreb, faculty of Electrical
 * Engineering and Computing.
 * 
 * Purpose of this framework is EDA development and comparing with other
 * evolutionary or stohastic or machine learning algorithms.
 * 
 * User package is workenvironment and there each class must Evaluation class
 * extend.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @version 1.0
 * 
 */
public class EDAFramework {

	/**
	 * Main method of EDAF.
	 * 
	 * @param args
	 *            1st arg is initialize file; other args are optional and are
	 *            captured and processed in workenvironment
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// 1st argument is framework initialize file and other are user
		// arguments (args[1], ...)
		new FrameworkExecutor(new ClassPathFrameworkContext(args[0]), args)
				.excute();

	}

}
