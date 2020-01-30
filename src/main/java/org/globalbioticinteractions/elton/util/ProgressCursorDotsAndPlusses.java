package org.globalbioticinteractions.elton.util;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressCursorDotsAndPlusses implements ProgressCursor {
    private final AtomicInteger position = new AtomicInteger(0);

    private final int width = ProgressUtil.getWidthOrDefault();
    private final AtomicBoolean printDot = new AtomicBoolean(true);
    private final PrintStream out;

    public ProgressCursorDotsAndPlusses(PrintStream out) {
        this.out = out;
    }

    @Override
    public void increment() {
        int pos = position.incrementAndGet();
        if (pos >= width) {
            position.set(0);
            out.print('\r');
            printDot.set(!printDot.get());
        }
        out.print(printDot.get() ? '.' : '+');
    }

    public int getWidth() {
        return width;
    }


}
