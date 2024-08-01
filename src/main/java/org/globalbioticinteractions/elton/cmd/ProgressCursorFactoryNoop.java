package org.globalbioticinteractions.elton.cmd;

import org.globalbioticinteractions.elton.util.ProgressCursor;
import org.globalbioticinteractions.elton.util.ProgressCursorFactory;

public class ProgressCursorFactoryNoop implements ProgressCursorFactory {
    @Override
    public ProgressCursor createProgressCursor() {
        return new ProgressCursor() {
            @Override
            public void increment() {
                //
            }
        };
    }
}
