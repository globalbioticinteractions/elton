package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.input.ProxyInputStream;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.elton.util.ProgressCursor;
import org.globalbioticinteractions.elton.util.ProgressCursorFactory;
import org.globalbioticinteractions.elton.util.ProgressUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class InputStreamFactoryLogging implements InputStreamFactory {

    private final AtomicLong byteCounter = new AtomicLong(0);
    private final ProgressCursorFactory cursorFactory;

    public InputStreamFactoryLogging(ProgressCursorFactory cursorFactory) {
        this.cursorFactory = cursorFactory;
    }

    @Override
    public InputStream create(InputStream is) throws IOException {
        final ProgressCursor cursor = cursorFactory.createProgressCursor();
        cursor.increment();

        return new ProxyInputStream(is) {

            @Override
            protected void afterRead(int n) throws IOException {
                super.afterRead(n);
                long l = byteCounter.addAndGet(n);
                if (l > ProgressUtil.BYTE_TRANSFER_PROGRESS_BATCH_SIZE) {
                    cursor.increment();
                    byteCounter.set(0);
                }
            }

        };
    }
}
