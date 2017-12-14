package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

abstract class CmdDefaultParams implements Runnable {
    @Parameter(names = {"--cache-dir", "-c"}, description = "cache directory")
    private String cacheDir = "./datasets";

    String getCacheDir() {
        return cacheDir;
    }

    @Parameter(description = "[namespace1] [namespace2] ...")
    private List<String> namespaces = new ArrayList<>();

    List<String> getNamespaces() {
        return namespaces;
    }
}
