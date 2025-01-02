package org.globalbioticinteractions.elton.cmd;

import java.io.IOException;

public interface LineHandler {

    boolean processLine(String line, boolean isFirstLine) throws IOException;
}
