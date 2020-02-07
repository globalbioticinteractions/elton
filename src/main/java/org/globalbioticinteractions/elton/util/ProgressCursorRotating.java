package org.globalbioticinteractions.elton.util;

import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressCursorRotating implements ProgressCursor {
    private final AtomicInteger position = new AtomicInteger(0);
    private final PrintStream out;

    private final char[] rotatingCharacters = new char[] {
            '-', '\\', '|', '/', '-', '\\', '|'
    };

    public ProgressCursorRotating(PrintStream out) {
        this.out = out;
    }

    @Override
    public void increment() {
        int pos = position.get();
        out.print(rotatingCharacters[pos]);
        out.print('\b');
        position.set((pos + 1) % 7);
    }

}
