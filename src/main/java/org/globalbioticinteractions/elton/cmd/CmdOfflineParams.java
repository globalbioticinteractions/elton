package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;

abstract class CmdOfflineParams extends CmdDefaultParams {
    @Parameter(names = {"--offline", "-o"}, description = "offline")
    private boolean offline = false;

    boolean isOffline() {
        return offline;
    }
}
