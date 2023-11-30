package org.globalbioticinteractions.elton.cmd;

import org.globalbioticinteractions.elton.util.ProgressCursor;
import org.globalbioticinteractions.elton.util.ProgressCursorFactory;
import org.globalbioticinteractions.elton.util.ProgressCursorRotating;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

abstract class CmdDefaultParams implements Runnable {
    @CommandLine.Option (names = {"--cache-dir", "-c"},
            description = "Cache directory"
    )
    private String cacheDir = "./datasets";

    @CommandLine.Option (names = {"--work-dir", "-w"},
            description = "Work directory"
    )
    private String workDir = ".";

    @CommandLine.Option (names = {"--no-progress"},
            description = "Do not show progress indicator"
    )
    private boolean noProgress = false;

    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }

    @CommandLine.Parameters (
            description = "[namespace1] [namespace2] ..."
    )
    private List<String> namespaces = new ArrayList<>();

    private PrintStream stderr = System.err;
    private PrintStream stdout = System.out;
    private InputStream stdin = System.in;

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

    public void setStdin(InputStream stdin) {
        this.stdin = stdin;
    }

    public InputStream getStdin() {
        return this.stdin;
    }

    public URI getWorkDir() {
        return workDir == null
                ? Paths.get("").toUri()
                : Paths.get(workDir).toUri();

    }

    public String getCacheDir() {
        return cacheDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public void setWorkDir(String workingDir) {
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
