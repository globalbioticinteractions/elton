package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;

abstract class CmdOnlineParams extends CmdDefaultParams {
    @Parameter(names = {"--online", "-o"}, description = "online")
    private boolean online = false;

    boolean isOnline() {
        return online;
    }
}
