package hr.fer.zemris.edaf;

import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.algorithm.AlgorithmFactory;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.genotype.GenotypeFactory;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.selection.SelectionFactory;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

import java.lang.reflect.Constructor;
import java.util.Observable;
import java.util.Observer;

/**
 * FrameworkExecutor.
 * 
 * OVERSEEING THE FRAMEWORK WORK. CONNECTS DAO LAYER AND APPLICATIONS.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class FrameworkExecutor implements Observer {

	/**
	 * HARDCODED FRAMEWORK ENVIRONMENT PACKAGE NAME. It is not changable. If
	 * user want to change this variable bahavior or visibility, it must do it
	 * by self but it is not recomended.
	 */
	private final String workEnvironmentPackageName = "hr.fer.zemris.edaf.workenvironment.";

	private final IFrameworkContext context;

	private Evaluation evaluationEnvoronment;

	private Algorithm algorithm;

	private final String[] args;

	public FrameworkExecutor(IFrameworkContext frameworkContext, String[] args) {

		context = frameworkContext;

		this.args = args;

		initializeFramework();

		registerObservers();
	}

	public void excute() {

		evaluationEnvoronment.init(algorithm);

	}

	private void initializeFramework() {

		final Genotype genotype = new GenotypeFactory(context).getGenotype();

		final Selection selection = new SelectionFactory(context)
				.getSelection();

		evaluationEnvoronment = loadEvaluationEnvironment();

		algorithm = new AlgorithmFactory(context, genotype, selection,
				evaluationEnvoronment).getAlgorithm();

	}

	public IFrameworkContext getFrameworkContext() {
		return context;
	}

	public Evaluation getEvaluationEnvoronment() {
		return evaluationEnvoronment;
	}

	// OVO JE PROBA
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

	private void registerObservers() {
		algorithm.addObserver(this);
		evaluationEnvoronment.addObserver(this);
	}

	private Evaluation loadEvaluationEnvironment() {
		Evaluation eval = null;
		try {
			final Constructor<?> constructor = Class.forName(
					workEnvironmentPackageName + context.getWorkEnvironment())
					.getConstructor(new Class[] { String[].class });
			eval = (Evaluation) constructor.newInstance(new Object[] { args });
		} catch (final Exception e) {
			MSGPrinter.printERROR(System.err, e.toString(), true, -1);
		}
		return eval;
	}

}