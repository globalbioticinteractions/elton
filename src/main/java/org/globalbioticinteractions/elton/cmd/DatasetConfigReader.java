package org.globalbioticinteractions.elton.cmd;

import org.globalbioticinteractions.dataset.Dataset;

import java.io.IOException;

public interface DatasetConfigReader {

    Dataset readConfig(String line) throws IOException;
}
