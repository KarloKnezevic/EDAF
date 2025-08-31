package com.knezevic.edaf.factory.selection;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.impl.TournamentSelection;

import java.util.Random;

public class TournamentSelectionFactory implements SelectionFactory {
    @Override
    public Selection create(Configuration config, Random random) throws Exception {
        return new TournamentSelection(random, config.getAlgorithm().getSelection().getSize());
    }
}
