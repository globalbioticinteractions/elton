package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.ActivityUtil;
import bio.guoda.preston.process.StatementListener;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.cache.ContentPathFactory;
import org.globalbioticinteractions.cache.ContentPathFactoryDepth0;
import org.globalbioticinteractions.cache.ProvenancePathFactory;
import org.globalbioticinteractions.cache.ProvenancePathFactoryImpl;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.Elton;
import org.globalbioticinteractions.elton.store.AccessLogger;
import org.globalbioticinteractions.elton.store.ActivityProxy;
import org.globalbioticinteractions.elton.store.ProvLogger;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.ProgressCursor;
import org.globalbioticinteractions.elton.util.ProgressCursorFactory;
import org.globalbioticinteractions.elton.util.ProgressCursorRotating;
import picocli.CommandLine;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

abstract class CmdDefaultParams implements Runnable {

    @CommandLine.Option(names = {"--cache-dir", "--data-dir", "-c"},
            description = "Data directory (default: ${DEFAULT-VALUE})")
    private String dataDir = "./datasets";

    @CommandLine.Option(names = {"--prov-dir"},
            description = "Provenance directory (default: ${DEFAULT-VALUE})"
    )
    private String provDir = "./datasets";

    @CommandLine.Option(names = {"--work-dir", "-w"},
            description = "Work directory (default: ${DEFAULT-VALUE})"
    )
    private String workDir = ".";

    @CommandLine.Option(names = {"--no-progress", "--silent"},
            description = "Do not show progress indicator (default: ${DEFAULT-VALUE})"
    )
    private boolean noProgress = false;

    @CommandLine.Option(names = {"--enable-prov-logging"},
            description = "Log provenance activity (default: ${DEFAULT-VALUE})"
    )
    private boolean enableProvenanceLogging = false;


    public void setNamespaces(List<String> namespaces) {
        this.namespaces = namespaces;
    }

    @CommandLine.Parameters(
            description = "[namespace1] [namespace2] ..."
    )
    private List<String> namespaces = new ArrayList<>();

    private ContentPathFactory contentPathFactory = new ContentPathFactoryDepth0();
    private ProvenancePathFactory provenancePathFactory = new ProvenancePathFactoryImpl();

    private PrintStream stderr = System.err;
    private PrintStream stdout = System.out;
    private InputStream stdin = System.in;

    private final UUID activityId = UUID.randomUUID();

    public static final Supplier<IRI> ACTIVITY_ID_FACTORY = () -> RefNodeFactory.toIRI(UUID.randomUUID());


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

    public String getDataDir() {
        return dataDir;
    }

    public String getProvDir() {
        return provDir;
    }

    public void setProvDir(String provDir) {
        this.provDir = provDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }


    public ContentPathFactory getContentPathFactory() {
        return (this.contentPathFactory);
    }


    public void setWorkDir(String workingDir) {
        this.workDir = workingDir;
    }

    public ProgressCursorFactory getProgressCursorFactory() {
        return noProgress
                ? () -> () -> {
        }
                : cursorFactory;
    }

    public InputStreamFactoryLogging createInputStreamFactory() {
        return new InputStreamFactoryLogging(getProgressCursorFactory());
    }


    public ProvenancePathFactory getProvenancePathFactory() {
        return provenancePathFactory;
    }

    public StatementListener getStatementListener() {
        return quad -> {
            if (enableProvenanceLogging && quad != null) {
                Quad quadInActivity = RefNodeFactory.toStatement(RefNodeFactory.toIRI(getActivityId()), quad);
                getStdout().println(quadInActivity);
            }
        };
    }

    public UUID getActivityId() {
        return activityId;
    }

    public Supplier<IRI> getActivityIdFactory() {
        return ACTIVITY_ID_FACTORY;
    }

    @Override
    public void run() {
        start();
        try {
            doRun();
        } finally {
            stop();
        }
    }


    private void start() {
        ActivityContext activity = getActivity();
        List<Quad> quads = ActivityUtil.generateSoftwareAgentProcessDescription(
                activity,
                RefNodeFactory.toIRI("https://globalbioticinteractions.org/elton"),
                RefNodeFactory.toIRI("https://doi.org/10.5281/zenodo.998263"),
                "Tobias Kuhn, Jorrit Poelen, & Katrin Leinweber. (2024). globalbioticinteractions/elton: " + Elton.getVersionString() + ". Zenodo. https://doi.org/10.5281/zenodo.13863810",
                "Elton helps to access, review and index existing species interaction datasets.");
        quads.forEach(q -> getStatementListener().on(q));
    }

    protected abstract void doRun();

    public abstract String getDescription();

    private ActivityContext getActivity() {
        return ActivityUtil.createNewActivityContext(CmdDefaultParams.this.getDescription());
    }

    private void stop() {
        getStatementListener().on(
                RefNodeFactory.toStatement(
                        getActivity().getActivity(),
                        getActivity().getActivity(),
                        RefNodeConstants.ENDED_AT_TIME,
                        RefNodeFactory.nowDateTimeLiteral()
                )
        );

    }


    protected ActivityProxy getActivityListener(String namespaceLocal) {
        return new ActivityProxy(
                Arrays.asList(
                        new ProvLogger(getStatementListener()),
                        new AccessLogger(namespaceLocal, getProvDir())
                )
        );
    }

    protected ActivityProxy getActivityListener() {
        return getActivityListener(DatasetRegistryUtil.NAMESPACE_LOCAL);
    }

    protected DatasetRegistry getDatasetRegistry() {
        return getDatasetRegistry(createInputStreamFactory());
    }

    protected DatasetRegistry getDatasetRegistry(InputStreamFactory inputStreamFactory) {
        return DatasetRegistryUtil.forCacheOrLocalDir(
                getDataDir(),
                getProvDir(),
                getWorkDir(),
                inputStreamFactory,
                getContentPathFactory(),
                getProvenancePathFactory(),
                getActivityListener(), new ActivityContext() {
                    @Override
                    public IRI getActivity() {
                        return null;
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }
                }, new Supplier<IRI>() {
                    @Override
                    public IRI get() {
                        return RefNodeFactory.toIRI(UUID.randomUUID());
                    }
                }
        );
    }

    protected ActivityContext getCtx() {
        return new ActivityContext() {
            @Override
            public IRI getActivity() {
                return RefNodeFactory.toIRI(CmdDefaultParams.this.getActivityId());
            }

            @Override
            public String getDescription() {
                return CmdDefaultParams.this.getDescription();
            }
        };
    }
}
