package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;

public abstract class CmdTabularWriterParams extends CmdDefaultParams {

    @Parameter(names = {"--skip-header", "-s", "--no-header"}, description = "skip header")
    private boolean skipHeader = false;

    boolean shouldSkipHeader() {
        return skipHeader;
    }

}
