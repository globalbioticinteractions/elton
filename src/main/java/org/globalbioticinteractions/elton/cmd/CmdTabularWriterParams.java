package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.domain.LogContext;
import org.globalbioticinteractions.elton.util.ProgressUtil;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicLong;

public abstract class CmdTabularWriterParams extends CmdDefaultParams {


    @CommandLine.Option(
            names = {"--skip-header", "-s", "--no-header"},
            description = "Skip header (default: ${DEFAULT-VALUE})"
    )

    private boolean skipHeader = false;

    boolean shouldSkipHeader() {
        return skipHeader;
    }

    public void setSkipHeader(boolean skipHeader) {
        this.skipHeader = skipHeader;
    }

    public abstract String getRecordType();

    protected NodeFactory getNodeFactoryForProv(final PrintStream out) {
        PrintStream dataOut = out;
        dataOut = getDataSink(dataOut);

        return WriterUtil.getNodeFactoryForType(getRecordType(), !shouldSkipHeader(), dataOut, getLogger());
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

}
