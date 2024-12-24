package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.StatementsEmitterAdapter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.domain.LogContext;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.store.ProvUtil;
import org.globalbioticinteractions.elton.util.ProgressUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public abstract class CmdTabularWriterParams extends CmdDefaultParams {


    @CommandLine.Option(
            names = {"--skip-header", "-s", "--no-header"},
            description = "Skip header (default: ${DEFAULT-VALUE})"
    )

    private boolean skipHeader = false;
    private Set<String> dependencies = Collections.synchronizedSet(new TreeSet<>());

    private File dataSink;

    boolean shouldSkipHeader() {
        return skipHeader;
    }

    public void setSkipHeader(boolean skipHeader) {
        this.skipHeader = skipHeader;
    }

    public abstract String getRecordType();

    protected NodeFactory getNodeFactoryForProv(final PrintStream out) {
        PrintStream dataOut = out;
        if (getEnableProvMode()) {
            try {
                File datafile = File.createTempFile(new File(getWorkDir()).getAbsolutePath(), "interactions");
                dataOut = new PrintStream(datafile);
                setDataSink(datafile);
            } catch (IOException e) {
                throw new RuntimeException("failed to create tmp file", e);
            }
        }

        return WriterUtil.getNodeFactoryForType(getRecordType(), !shouldSkipHeader(), dataOut, getLogger());
    }

    protected DatasetRegistry getDatasetRegistryWithProv() {
        return getDatasetRegistry(new ActivityListener() {
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
            });
    }

    @Override
    protected void stop() {
        emitProcessDescription();
        super.stop();
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

    protected ImportLogger getLogger() {
        return new ImportLogger() {
            final AtomicLong lineCounter = new AtomicLong(0);

            @Override
            public void warn(LogContext ctx, String message) {
                reportProgress();
            }

            @Override
            public void info(LogContext ctx, String message) {
                reportProgress();
            }

            @Override
            public void severe(LogContext ctx, String message) {
                reportProgress();
            }

            private void reportProgress() {
                long l = lineCounter.incrementAndGet();
                if (l % ProgressUtil.LOG_ACTIVITY_PROGRESS_BATCH_SIZE == 0) {
                    getProgressCursorFactory().createProgressCursor().increment();
                }
            }
        };
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
}
