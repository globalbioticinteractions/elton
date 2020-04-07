package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;
import org.globalbioticinteractions.elton.util.ProgressCursor;
import org.globalbioticinteractions.elton.util.ProgressCursorFactory;
import org.globalbioticinteractions.elton.util.ProgressCursorRotating;

import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

abstract class CmdDefaultParams implements Runnable {
    @Parameter(names = {"--cache-dir", "-c"}, description = "cache directory")
    private String cacheDir = "./datasets";

    @Parameter(names = {"--no-progress"}, description = "do not show progress indicator")
    private boolean noProgress = false;

    @Parameter(description = "[namespace1] [namespace2] ...")
    private List<String> namespaces = new ArrayList<>();

    private URI workDir;

    private PrintStream stderr = System.err;
    private PrintStream stdout = System.out;

    final private ProgressCursorFactory cursorFactory = new ProgressCursorFactory() {
        private final ProgressCursor cursor = new ProgressCursorRotating(stderr);
        @Override
        public ProgressCursor createProgressCursor() {
            return cursor;
        }
    };

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

    public void setWorkDir(URI workingDir) {
        this.workDir = workingDir;
    }

    public ProgressCursorFactory getProgressCursorFactory() {
        return noProgress
                ? () -> () -> {}
                 : cursorFactory;
    }

    public InputStreamFactoryLogging createInputStreamFactory() {
        return new InputStreamFactoryLogging(getProgressCursorFactory());
    }


}
