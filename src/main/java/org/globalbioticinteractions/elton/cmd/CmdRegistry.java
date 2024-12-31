package org.globalbioticinteractions.elton.cmd;

import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

public abstract class CmdRegistry extends CmdDefaultParams {
    @CommandLine.Option(names = {"--registries", "--registry"},
            description = "[registry1],[registry2],..."
    )
    private List<String> registryNames = new ArrayList<String>() {{
        add("zenodo");
        add("github");
    }};

    public void setRegistryNames(List<String> registryNames) {
        this.registryNames = registryNames;
    }

    public List<String> getRegistryNames() {
        return registryNames;
    }
}
