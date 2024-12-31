package org.globalbioticinteractions.elton.cmd;

import picocli.CommandLine;

abstract class CmdOnlineParams extends CmdRegistry {

    @CommandLine.Option(
            names = {"--online", "-o"},
            description = "use online data registries (default: ${DEFAULT-VALUE})"
    )
    private boolean online = false;

    boolean isOnline() {
        return online;
    }

    void setOnline(boolean online) {
        this.online = online;
    }
}
