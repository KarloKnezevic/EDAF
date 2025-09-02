package com.knezevic.edaf.factory.selection;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.selection.MuPlusLambdaSelection;

import java.util.Random;

/**
 * A factory for creating {@link MuPlusLambdaSelection} objects.
 */
public class MuPlusLambdaSelectionFactory implements SelectionFactory {
    @Override
    public Selection create(Configuration config, Random random) throws Exception {
        return new MuPlusLambdaSelection(config.getAlgorithm().getSelection().getMu());
    }
}
