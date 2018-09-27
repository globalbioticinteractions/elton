package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;

abstract class CmdOnlineParams extends CmdDefaultParams {
    @Parameter(names = {"--online", "-o"}, description = "use online data registries")
    private boolean online = false;

    boolean isOnline() {
        return online;
    }
}
