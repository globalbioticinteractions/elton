package org.globalbioticinteractions.elton.util;

import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class ProgressUtil {

    public static final int LOG_ACTIVITY_PROGRESS_BATCH_SIZE = 500;
    public static final int BYTE_TRANSFER_PROGRESS_BATCH_SIZE = 1024 * 128;
    public static final int SPECIMEN_CREATED_PROGRESS_BATCH_SIZE = 100;

    public static void logProgress(int reportBatchSize, long count, ProgressCursor cursor) {
        if (count % reportBatchSize == 0) {
            cursor.increment();
        }
    }

    public static int getWidthOrDefault() {
        final int widthDefault = 80;
        int width = widthDefault;
        try {
            width = TerminalBuilder.builder().build().getWidth();
        } catch (IOException e) {
            // ignore
        }
        return width > 0 ? width : widthDefault;
    }
}
