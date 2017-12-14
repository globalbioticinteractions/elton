package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

abstract class CmdDefaultParams implements Runnable {
    @Parameter(names = {"--cache-dir", "-c"}, description = "cache directory")
    private String cacheDir = "./datasets";

    @Parameter(names = {"--verbose", "-v"}, description = "verbose logging")
    private boolean verbose = false;

    String getCacheDir() {
        return cacheDir;
    }

    boolean isVerbose() {
        return verbose;
    }

    @Parameter(description = "[namespace1] [namespace2] ...")
    private List<String> namespaces = new ArrayList<>();


    List<String> getNamespaces() {
        return namespaces;
    }
}
