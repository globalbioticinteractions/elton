package org.globalbioticinteractions.elton.cmd;

import picocli.CommandLine;

abstract class CmdOnlineParams extends CmdDefaultParams {

    @CommandLine.Option(
            names = {"--online", "-o"},
            description = "use online data registries"
    )
    private boolean online = false;

    boolean isOnline() {
        return online;
    }
}
