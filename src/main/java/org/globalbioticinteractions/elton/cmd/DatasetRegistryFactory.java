package org.globalbioticinteractions.elton.cmd;

import org.globalbioticinteractions.dataset.DatasetFinderException;
import org.globalbioticinteractions.dataset.DatasetRegistry;

public interface DatasetRegistryFactory {

    DatasetRegistry createRegistryByName(String name) throws DatasetFinderException;
}
