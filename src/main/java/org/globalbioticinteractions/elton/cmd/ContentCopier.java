package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.function.Consumer;

public class ContentCopier implements Consumer<String> {

    private final BlobStoreAppendOnly blobStore;
    private final Collection<String> writeCache;
    private File dataDir;
    private PrintStream stderr;
    private HashType contentType;

    public ContentCopier(BlobStoreAppendOnly blobStore, String dataDir, PrintStream stderr, HashType contentType) {
        this.blobStore = blobStore;
        this.dataDir = new File(dataDir);
        this.stderr = stderr;
        this.contentType = contentType;
        this.writeCache = new CircularFifoQueue<>(1024);
    }

    @Override
    public void accept(String contentIdCandidate) {
        if (!writeCache.contains(contentIdCandidate)) {
            copy(contentIdCandidate);
            writeCache.add(contentIdCandidate);
        }
    }

    private void copy(String contentIdCandidate) {
        Collection<File> files = FileUtils.listFiles(dataDir,
                new NameFileFilter(StringUtils.removeStart(contentIdCandidate, contentType.getPrefix())),
                TrueFileFilter.TRUE
        );

        for (File file : files) {
            try {
                IRI contentId = blobStore.put(new FileInputStream(file));
                if (StringUtils.endsWith(contentId.getIRIString(), contentIdCandidate)) {
                    stderr.println("copied [" + contentId + "]");
                }
            } catch (IOException ex) {
                stderr.println("failed to copy [" + file.getAbsolutePath() + "]");
                ex.printStackTrace(stderr);
            }
        }
    }
}
