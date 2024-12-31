package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import com.google.common.hash.BloomFilter;
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
    private String dataDir;
    private PrintStream stderr;

    public ContentCopier(BlobStoreAppendOnly blobStore, String dataDir, PrintStream stderr) {
        this.blobStore = blobStore;
        this.dataDir = dataDir;
        this.stderr = stderr;
    }

    @Override
    public void accept(String contentIdCandidate) {
        File dataDir = new File(this.dataDir);
        Collection<File> files = FileUtils.listFiles(dataDir,
                new NameFileFilter(StringUtils.removeStart(contentIdCandidate, HashType.sha256.getPrefix())),
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
