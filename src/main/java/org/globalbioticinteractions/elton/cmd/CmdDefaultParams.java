package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;

import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

abstract class CmdDefaultParams implements Runnable {
    @Parameter(names = {"--cache-dir", "-c"}, description = "cache directory")
    private String cacheDir = "./datasets";


    private PrintStream stderr = System.err;
    private PrintStream stdout = System.out;
    private URI workDir = null;

    String getCacheDir() {
        return cacheDir;
    }

    @Parameter(description = "[namespace1] [namespace2] ...")
    private List<String> namespaces = new ArrayList<>();

    List<String> getNamespaces() {
        return namespaces;
    }

    public PrintStream getStdout() {
        return stdout;
    }

    public PrintStream getStderr() {
        return stderr;
    }

    public void setStderr(PrintStream err) {
        this.stderr = err;
    }

    public void setStdout(PrintStream out) {
        this.stdout = out;
    }

    public URI getWorkDir() {
        return workDir == null
                ? Paths.get("").toUri()
                : workDir;
    }

    public void setWorkDir(URI workingDir) {
        this.workDir = workingDir;
    }
}
