package com.knezevic.edaf.factory.selection;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.selection.ProportionalSelection;

import java.util.Random;

/**
 * A factory for creating {@link ProportionalSelection} objects.
 */
public class ProportionalSelectionFactory implements SelectionFactory {
    @Override
    public Selection create(Configuration config, Random random) throws Exception {
        return new ProportionalSelection(random);
    }
}
