package org.globalbioticinteractions.elton.util;

import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

public class ProgressUtil {

    public static void logProgress(int reportBatchSize, int count, ProgressCursor cursor) {
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
