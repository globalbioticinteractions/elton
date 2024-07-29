package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.data.ImportLogger;
import org.eol.globi.domain.LogContext;
import org.globalbioticinteractions.elton.util.ProgressCursorFactory;
import org.globalbioticinteractions.elton.util.ProgressUtil;

import java.io.PrintStream;

public class ReviewReportLogger implements ImportLogger {
    private final ReviewReport report;
    private final PrintStream stdout;
    private final Long maxLines;
    private final ProgressCursorFactory factory;

    public ReviewReportLogger(ReviewReport report, PrintStream stdout, Long maxLines, ProgressCursorFactory factory) {
        this.report = report;
        this.stdout = stdout;
        this.maxLines = maxLines;
        this.factory = factory;
    }


    @Override
    public void info(LogContext ctx, String message) {
        logWithCounter(ctx, message, ReviewCommentType.info);
        report.getInfoCounter().incrementAndGet();
    }

    @Override
    public void warn(LogContext ctx, String message) {
        logWithCounter(ctx, message, ReviewCommentType.note);
        report.getNoteCounter().incrementAndGet();
    }

    @Override
    public void severe(LogContext ctx, String message) {
        logWithCounter(ctx, message, ReviewCommentType.note);
        report.getNoteCounter().incrementAndGet();
    }

    private void logWithCounter(LogContext ctx, String message, ReviewCommentType commentType) {
        if (this.maxLines == null || report.getLineCount().get() < this.maxLines) {
            CmdReview.log(ctx, message, commentType, report, stdout);
        }

        long l = report.getLineCount().incrementAndGet();
        if (l % ProgressUtil.LOG_ACTIVITY_PROGRESS_BATCH_SIZE == 0) {
            factory.createProgressCursor().increment();
        }
    }




}
