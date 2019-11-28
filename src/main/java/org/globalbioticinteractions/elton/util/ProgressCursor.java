package org.globalbioticinteractions.elton.util;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressCursor {
    private final AtomicInteger position = new AtomicInteger(0);
    private final int width = ProgressUtil.getWidthOrDefault();
    private volatile boolean printDot = true;
    private final PrintStream out;

    public ProgressCursor(PrintStream out) {
        this.out = out;
    }

    public void increment() {
        int pos = position.incrementAndGet();
        if (pos >= width) {
            position.set(0);
            out.print('\r');
            printDot = !printDot;
        }
        out.print(printDot ? '.' : '+');
    }
}
