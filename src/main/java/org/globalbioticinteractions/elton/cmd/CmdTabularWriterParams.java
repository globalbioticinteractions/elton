package org.globalbioticinteractions.elton.cmd;

import picocli.CommandLine;

public abstract class CmdTabularWriterParams extends CmdDefaultParams {


    @CommandLine.Option(
            names = {"--skip-header", "-s", "--no-header"},
            description = "Skip header (default: ${DEFAULT-VALUE})"
    )

    private boolean skipHeader = false;

    boolean shouldSkipHeader() {
        return skipHeader;
    }

    public void setSkipHeader(boolean skipHeader) {
        this.skipHeader = skipHeader;
    }

}
