package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.ActivityUtil;
import bio.guoda.preston.process.StatementsEmitterAdapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.domain.LogContext;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.util.ProgressUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@CommandLine.Command(
        name = "interactions",
        aliases = {"interaction", "interact"},
        description = CmdInteractions.DESCRIPTION
)
public class CmdInteractions extends CmdTabularWriterParams {

    public static final String DESCRIPTION = "List Interactions";

    @Override
    public void doRun() {
        run(getStdout());
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    void run(PrintStream out) {

        DatasetRegistry registry = getDatasetRegistry(new ActivityListener() {
            @Override
            public void onStarted(IRI parentActivityId, IRI activityId, IRI request) {
                System.out.println("retrieving some data registry resource: [" + request.getIRIString() + "]");
            }

            @Override
            public void onCompleted(IRI parentActivityId, IRI activityId, IRI request, IRI response, URI localPathOfResponseData) {
                System.out.println("retrieved some data registry resource: [" + request.getIRIString() + "]");
            }
        });

        File tmpSourceFile = null;

        PrintStream dataOut = out;
        if (getEnableProvMode()) {
            try {
                tmpSourceFile = File.createTempFile(new File(getWorkDir()).getAbsolutePath(), "interactions");
                dataOut = new PrintStream(tmpSourceFile);
            } catch (IOException e) {
                throw new RuntimeException("failed to create tmp file", e);
            }
        }

        NodeFactory nodeFactory = WriterUtil.nodeFactoryForInteractionWriting(!shouldSkipHeader(), dataOut);

        final File file = new File(getWorkDir());
        CmdUtil.handleNamespaces(
                registry,
                getNamespaces(),
                "listing interactions",
                getStderr(),
                getNamespaceHandler(registry, nodeFactory, file, getLogger())
        );

        if (getEnableProvMode() && tmpSourceFile != null) {
            try (FileInputStream fis = new FileInputStream(tmpSourceFile)) {
                IRI iri = Hasher.calcHashIRI(fis, NullOutputStream.INSTANCE, true, HashType.sha256);
                ActivityUtil.emitDownloadActivity(RefNodeFactory.toIRI(UUID.randomUUID()), iri, new StatementsEmitterAdapter() {
                    @Override
                    public void emit(Quad quad) {
                        getStatementListener().on(quad);
                    }
                }, Optional.of(getActivityContext().getActivity()));
                saveGeneratedContentIfNeeded(tmpSourceFile, iri);
            } catch (IOException e) {
                throw new RuntimeException("failed to persist data stream to", e);
            }
        }
    }

    private ImportLogger getLogger() {
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


    private void saveGeneratedContentIfNeeded(File tmpSourceFile, IRI iri) throws IOException {
        File destFile = new File(getDataDir(), StringUtils.replace(iri.getIRIString(), HashType.sha256.getPrefix(), ""));

        if (destFile.exists()) {
            FileUtils.delete(tmpSourceFile);
        } else {
            FileUtils.moveFile(
                    tmpSourceFile,
                    destFile
            );
        }
    }

}


