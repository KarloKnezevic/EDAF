package com.knezevic.edaf.factory.selection;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.selection.KTournamentSelection;

import java.util.Random;

/**
 * A factory for creating {@link KTournamentSelection} objects.
 */
public class KTournamentSelectionFactory implements SelectionFactory {
    @Override
    public Selection create(Configuration config, Random random) throws Exception {
        int k = config.getAlgorithm().getSelection().getK();
        return new KTournamentSelection(random, k);
    }
}
