package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;

abstract class CmdOfflineParams extends CmdDefaultParams {
    @Parameter(names = {"--offline", "-o"}, description = "offline")
    private boolean offline = true;

    boolean isOffline() {
        return offline;
    }
}
