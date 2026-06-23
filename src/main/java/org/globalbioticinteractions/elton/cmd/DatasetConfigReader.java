package org.globalbioticinteractions.elton.cmd;

import org.globalbioticinteractions.dataset.Dataset;

import java.io.Closeable;
import java.io.IOException;

public interface DatasetConfigReader extends Closeable {

    Dataset readConfig(String line) throws IOException;

    Dataset datasetForContextOrReset();
}
