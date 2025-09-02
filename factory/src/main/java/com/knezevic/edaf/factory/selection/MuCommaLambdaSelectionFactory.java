package com.knezevic.edaf.factory.selection;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.selection.MuCommaLambdaSelection;

import java.util.Random;

/**
 * A factory for creating {@link MuCommaLambdaSelection} objects.
 */
public class MuCommaLambdaSelectionFactory implements SelectionFactory {
    @Override
    public Selection create(Configuration config, Random random) throws Exception {
        return new MuCommaLambdaSelection(random);
    }
}
