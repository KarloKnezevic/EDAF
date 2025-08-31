package com.knezevic.edaf.factory.selection;

import com.knezevic.edaf.configuration.pojos.Configuration;

public class SelectionFactoryProvider {
    public static SelectionFactory getFactory(Configuration config) {
        if (config.getAlgorithm().getSelection() == null) {
            return null;
        }
        String selectionName = config.getAlgorithm().getSelection().getName();
        if ("tournament".equals(selectionName)) {
            return new TournamentSelectionFactory();
        } else if ("rouletteWheel".equals(selectionName)) {
            return new RouletteWheelSelectionFactory();
        }
        return null;
    }
}
