package com.knezevic.edaf.selection;

import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.spi.SelectionProvider;

public class TournamentSelectionProvider implements SelectionProvider {
    @Override
    public String id() {
        return "tournament";
    }

    @Override
    public Selection<?> create() {
        return new TournamentSelection(new java.util.Random(), 2);
    }
}


