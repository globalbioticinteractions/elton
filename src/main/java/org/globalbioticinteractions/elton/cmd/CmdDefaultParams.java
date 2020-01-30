package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;
import org.apache.commons.lang.StringUtils;
import org.globalbioticinteractions.elton.util.ProgressCursor;
import org.globalbioticinteractions.elton.util.ProgressCursorDotsAndPlusses;

import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

abstract class CmdDefaultParams implements Runnable {
    @Parameter(names = {"--cache-dir", "-c"}, description = "cache directory")
    private String cacheDir = "./datasets";

    @Parameter(names = {"--tmp-dir"}, description = "tmp directory")
    private String tmpDir = "./.elton/tmp";

    private URI workDir;

    private PrintStream stderr = System.err;
    private PrintStream stdout = System.out;
    private ProgressCursor cursor = new ProgressCursorDotsAndPlusses(stderr);

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

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public String getTmpDir() {
        return tmpDir;
    }

    public String setTmpDir(String tmpDir) {
        return this.tmpDir = tmpDir;
    }

    public void setWorkDir(URI workingDir) {
        this.workDir = workingDir;
    }

    public ProgressCursor getProgressCursor() {
        return this.cursor;
    }

    public InputStreamFactoryLogging createInputStreamFactory() {
        return new InputStreamFactoryLogging(getProgressCursor());
    }


}
