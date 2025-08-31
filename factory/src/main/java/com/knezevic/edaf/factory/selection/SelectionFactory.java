package com.knezevic.edaf.factory.selection;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Selection;

import java.util.Random;

public interface SelectionFactory {
    Selection create(Configuration config, Random random) throws Exception;
}
