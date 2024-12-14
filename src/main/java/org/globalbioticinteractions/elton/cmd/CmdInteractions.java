package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.domain.LogContext;

import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.ProgressUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
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
        run(System.out);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    void run(PrintStream out) {

        DatasetRegistry registry = DatasetRegistryUtil.forCacheOrLocalDir(
                getDataDir(),
                getProvDir(),
                getWorkDir(),
                createInputStreamFactory(),
                getContentPathFactory(),
                getProvenancePathFactory(),
                getActivityListener(DatasetRegistryUtil.NAMESPACE_LOCAL)
        );

        NodeFactory nodeFactory = WriterUtil.nodeFactoryForInteractionWriting(!shouldSkipHeader(), out);

        CmdUtil.handleNamespaces(
                registry,
                nodeFactory,
                getNamespaces(),
                "listing interactions",
                getStderr(),
                new ImportLogger() {
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
                },
                new File(getWorkDir())
        );
    }
}


