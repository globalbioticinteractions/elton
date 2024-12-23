package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.domain.LogContext;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.ProgressUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

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
