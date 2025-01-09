package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.ActivityUtil;
import bio.guoda.preston.process.StatementListener;
import bio.guoda.preston.process.StatementsEmitterAdapter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.cache.ContentPathFactory;
import org.globalbioticinteractions.cache.ContentPathFactoryDepth0;
import org.globalbioticinteractions.cache.ProvenancePathFactory;
import org.globalbioticinteractions.cache.ProvenancePathFactoryImpl;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.store.AccessLogger;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.store.ActivityProxy;
import org.globalbioticinteractions.elton.store.ProvLogger;
import org.globalbioticinteractions.elton.store.ProvUtil;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.ProgressCursor;
import org.globalbioticinteractions.elton.util.ProgressCursorFactory;
import org.globalbioticinteractions.elton.util.ProgressCursorRotating;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.globalbioticinteractions.elton.Elton.getEltonDescription;

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

    public boolean isNoProgress() {
        return noProgress;
    }

    @CommandLine.Option(names = {"--no-progress", "--silent"},
            description = "Do not show progress indicator (default: ${DEFAULT-VALUE})"
    )
    private boolean noProgress = false;

    @CommandLine.Option(names = {"--prov-mode"},
            description = "Log provenance activity (default: ${DEFAULT-VALUE})"
    )
    private boolean enableProvMode = false;
    private Set<String> dependencies = Collections.synchronizedSet(new TreeSet<>());
    private File dataSink;


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
        return this.contentPathFactory;
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
            if (enableProvMode && quad != null) {
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
        getEltonDescription(getActivityContext()).forEach(q -> getStatementListener().on(q));
    }

    protected abstract void doRun();

    public abstract String getDescription();

    private ActivityContext getActivity() {
        return ActivityUtil.createNewActivityContext(CmdDefaultParams.this.getDescription());
    }

    protected PrintStream getDataSink(PrintStream dataOut) {
        if (getEnableProvMode()) {
            try {
                File datafile = File.createTempFile(new File(getWorkDir()).getAbsolutePath(), "interactions");
                dataOut = new PrintStream(datafile);
                setDataSink(datafile);
            } catch (IOException e) {
                throw new RuntimeException("failed to create tmp file", e);
            }
        }
        return dataOut;
    }

    protected DatasetRegistry getDatasetRegistryWithProv() {
        return getDatasetRegistry(getActivityListenerWithProv());
    }

    protected ActivityListener getActivityListenerWithProv() {
        return new ActivityListener() {
            @Override
            public void onStarted(IRI parentActivityId, IRI activityId, IRI request) {
                // may be attempting to retrieve resources that do not exist
            }

            @Override
            public void onCompleted(IRI parentActivityId, IRI activityId, IRI request, IRI response, URI localPathOfResponseData) {
                if (response != null && getEnableProvMode()) {
                    getDependencies().add(response.getIRIString());
                }
            }
        };
    }

    protected void stop() {
        emitProcessDescription();
        getStatementListener().on(
                RefNodeFactory.toStatement(
                        getActivityContext().getActivity(),
                        getActivityContext().getActivity(),
                        RefNodeConstants.ENDED_AT_TIME,
                        RefNodeFactory.nowDateTimeLiteral()
                )
        );

    }


    protected ActivityListener getActivityListener(String namespaceLocal) {
        AccessLogger accessLogger = new AccessLogger(namespaceLocal, getProvDir());
        return new ActivityProxy(getEnableProvMode()
                ? Arrays.asList(new ProvLogger(getStatementListener()), accessLogger)
                : Collections.singletonList(accessLogger));
    }

    protected ActivityListener getActivityListener() {
        return getActivityListener(DatasetRegistryUtil.NAMESPACE_LOCAL);
    }

    protected DatasetRegistry getDatasetRegistry() {
        return getDatasetRegistry(createInputStreamFactory(), getActivityListener());
    }

    protected DatasetRegistry getDatasetRegistry(ActivityListener activityListener) {
        return getDatasetRegistry(createInputStreamFactory(), activityListener);
    }

    protected DatasetRegistry getDatasetRegistry(InputStreamFactory inputStreamFactory, ActivityListener activityListener) {
        return DatasetRegistryUtil.forCacheOrLocalDir(
                getDataDir(),
                getProvDir(),
                getWorkDir(),
                inputStreamFactory,
                getContentPathFactory(),
                getProvenancePathFactory(),
                activityListener,
                getActivityContext(),
                getActivityIdFactory()
        );
    }

    protected ActivityContext getActivityContext() {
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

    public void setEnableProvMode(boolean enableProvMode) {
        this.enableProvMode = enableProvMode;
    }

    public boolean getEnableProvMode() {
        return this.enableProvMode;
    }

    protected NamespaceHandler getNamespaceHandler(DatasetRegistry registry, NodeFactory nodeFactory, File workDir, ImportLogger logger) {
        return new NamespaceHandlerImpl(registry, nodeFactory, logger, workDir);
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public File getDataSink() {
        return dataSink;
    }

    public void setDataSink(File datafile) {
        this.dataSink = datafile;
    }

    protected void emitProcessDescription() {
        File dataSink = getDataSink();
        if (getEnableProvMode() && dataSink != null) {
            try (FileInputStream fis = new FileInputStream(dataSink)) {
                IRI iri = Hasher.calcHashIRI(fis, NullOutputStream.INSTANCE, true, HashType.sha256);
                ProvUtil.saveGeneratedContentIfNeeded(dataSink, iri, getDataDir());
                ProvUtil.emitDataGenerationActivity(
                        getDependencies().stream().map(RefNodeFactory::toIRI).collect(Collectors.toList()),
                        RefNodeFactory.toIRI(UUID.randomUUID()),
                        iri,
                        new StatementsEmitterAdapter() {
                            @Override
                            public void emit(Quad quad) {
                                getStatementListener().on(quad);
                            }
                        },
                        Optional.of(getActivityContext().getActivity())
                );
            } catch (IOException e) {
                throw new RuntimeException("failed to persist data stream to", e);
            }
        }
    }

}
