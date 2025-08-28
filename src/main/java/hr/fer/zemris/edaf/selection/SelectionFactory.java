package hr.fer.zemris.edaf.selection;

import hr.fer.zemris.edaf.IFrameworkContext;
import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.selection.proportional.SimpleProportionalSelection;
import hr.fer.zemris.edaf.selection.proportional.StohasticUniversalSampling;
import hr.fer.zemris.edaf.selection.rank.sort.TruncationSelection;
import hr.fer.zemris.edaf.selection.rank.tournament.KTournamentSelection;

/**
 * SelectionFactory.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class SelectionFactory {

	private Selection selection;

	public SelectionFactory(IFrameworkContext context) {

		initialize(context);

	}

	private void initialize(IFrameworkContext context) {

		if (context.getSelection().equals("simpleProp")) {
			selection = new SimpleProportionalSelection(context.getRand(),
					context.getSelectionRatio());
		} else if (context.getSelection().equals("stohasticUnivSamp")) {
			selection = new StohasticUniversalSampling(context.getRand(),
					context.getSelectionRatio(),
					(int) context.getSelectionParam());
		} else if (context.getSelection().equals("truncation")) {
			selection = new TruncationSelection(context.getRand(),
					context.getSelectionRatio());
		} else if (context.getSelection().equals("kTournament")) {
			selection = new KTournamentSelection(context.getRand(),
					context.getSelectionRatio(),
					(int) context.getSelectionParam());
		} else {
			MSGPrinter.printERROR(System.err, "Unsupported selection.", true,
					-1);
		}

	}

	public Selection getSelection() {
		return selection;
	}
}