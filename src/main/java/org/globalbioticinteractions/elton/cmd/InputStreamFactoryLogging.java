package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.input.ProxyInputStream;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.elton.util.ProgressCursor;
import org.globalbioticinteractions.elton.util.ProgressUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicLong;

public class InputStreamFactoryLogging implements InputStreamFactory {

    private final AtomicLong byteCounter = new AtomicLong(0);
    private final ProgressCursor cursor;

    public InputStreamFactoryLogging(ProgressCursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public InputStream create(InputStream is) throws IOException {

        return new ProxyInputStream(is) {
            @Override
            protected void afterRead(int n) throws IOException {
                super.afterRead(n);
                long l = byteCounter.addAndGet(n);
                if (l > 1024*128) {
                    cursor.increment();
                    byteCounter.set(0);
                }
            }
        };
    }
}
