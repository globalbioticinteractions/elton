package org.globalbioticinteractions.elton.cmd;

import org.globalbioticinteractions.dataset.Dataset;

import java.io.IOException;

public interface LineHandler {

    Dataset extractDataset(String line, boolean isFirstLine) throws IOException;
}
