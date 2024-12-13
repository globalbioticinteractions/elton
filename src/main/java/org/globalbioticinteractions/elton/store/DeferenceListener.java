package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.StatementListener;
import bio.guoda.preston.store.KeyTo1LevelPath;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.util.DateUtil;
import org.globalbioticinteractions.cache.ContentProvenance;
import org.globalbioticinteractions.cache.ProvenanceLog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public class DeferenceListener implements ActivityListener {
    private final KeyTo1LevelPath keyToPath;
    private final String namespace;
    private final StatementListener listener;
    private final String provDir;

    public DeferenceListener(String namespace, StatementListener listener, String dataDir, String provDir) {
        File dataFolder = new File(dataDir, namespace);
        this.keyToPath = new KeyTo1LevelPath(dataFolder.toURI());
        this.namespace = namespace;
        this.listener = listener;
        this.provDir = provDir;
    }

    public static KeyTo1LevelPath getKeyTo1LevelPath(String namespace, String dataDir) {
        File dataFolder = new File(dataDir, namespace);
        return new KeyTo1LevelPath(dataFolder.toURI());
    }

    @Override
    public void onStarted(UUID activityId, IRI request) {

    }

    @Override
    public void onCompleted(UUID activityId, IRI request, IRI response) {
        streamProvenance(request, response, listener);
        try {
            recordProvenance(URI.create(request.getIRIString()), response, keyToPath);
        } catch (IOException e) {
            throw new RuntimeException("failed to record provenance", e);
        }

    }

    private void streamProvenance(IRI request, IRI response, StatementListener statementListener) {
        if (statementListener != null) {
            Quad quad = RefNodeFactory.toStatement(
                    request,
                    RefNodeConstants.HAS_VERSION,
                    response
            );
            statementListener.on(quad);
        }
    }

    private void recordProvenance(URI resourceURI, IRI dereferenced, KeyTo1LevelPath keyToPath) throws IOException {
        URI localPathURI = keyToPath.toPath(dereferenced);
        ContentProvenance contentProvenanceWithNamespace
                = new ContentProvenance(namespace,
                resourceURI,
                localPathURI,
                StringUtils.replace(dereferenced.getIRIString(), "hash://sha256/", ""),
                DateUtil.nowDateString()
        );

        ProvenanceLog.appendProvenanceLog(
                new File(provDir),
                contentProvenanceWithNamespace
        );
    }

}
