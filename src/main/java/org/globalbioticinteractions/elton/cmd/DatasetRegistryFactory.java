package org.globalbioticinteractions.elton.cmd;

import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;

public interface DatasetRegistryFactory {

    DatasetRegistry createRegistryByName(String name) throws DatasetRegistryException;
}
