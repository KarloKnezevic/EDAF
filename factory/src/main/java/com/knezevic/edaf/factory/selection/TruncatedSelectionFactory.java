package com.knezevic.edaf.factory.selection;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.selection.TruncatedSelection;

import java.util.Random;

/**
 * A factory for creating {@link TruncatedSelection} objects.
 */
public class TruncatedSelectionFactory implements SelectionFactory {
    @Override
    public Selection create(Configuration config, Random random) throws Exception {
        return new TruncatedSelection(random);
    }
}
