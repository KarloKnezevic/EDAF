package com.knezevic.edaf.selection;

import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.spi.SelectionProvider;

public class SimpleTournamentSelectionProvider implements SelectionProvider {
    @Override
    public String id() {
        return "simple-tournament";
    }

    @Override
    public Selection<?> create() {
        return new SimpleTournamentSelection(new java.util.Random());
    }
}


