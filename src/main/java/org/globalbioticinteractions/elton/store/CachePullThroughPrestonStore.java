package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.StatementListener;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.Dereferencer;
import bio.guoda.preston.store.DereferencerContentAddressed;
import bio.guoda.preston.store.KeyTo1LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.service.ResourceService;
import org.eol.globi.util.DateUtil;
import org.globalbioticinteractions.cache.CachePullThrough;
import org.globalbioticinteractions.cache.CacheUtil;
import org.globalbioticinteractions.cache.ContentProvenance;
import org.globalbioticinteractions.cache.ProvenanceLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class CachePullThroughPrestonStore extends CachePullThrough {

    private final String namespace;
    private final String cachePath;
    private final ResourceService remote;
    private final StatementListener listener;

    public CachePullThroughPrestonStore(
            String namespace,
            String cachePath,
            ResourceService resourceService
    ) {
        this(namespace, cachePath, resourceService, new StatementListener() {
            @Override
            public void on(Quad quad) {
                // do nothing
            }
        });
    }

    public CachePullThroughPrestonStore(
            String namespace,
            String cachePath,
            ResourceService resourceService,
            StatementListener listener
    ) {
        super(namespace, cachePath,
                resourceService);
        this.namespace = namespace;
        this.cachePath = cachePath;
        this.remote = resourceService;
        this.listener = listener;
    }

    public InputStream retrieve(URI resourceURI) throws IOException {
        CacheUtil.findOrMakeCacheDirForNamespace(cachePath, namespace);

        File cacheDir = new File(cachePath, namespace);
        KeyTo1LevelPath keyToPath = new KeyTo1LevelPath(cacheDir.toURI());
        BlobStoreAppendOnly blobStore = new BlobStoreAppendOnly(
                new KeyValueStoreLocalFileSystem(
                        cacheDir,
                        keyToPath,
                        new ValidatingKeyValueStreamContentAddressedFactory()
                ),
                true,
                HashType.sha256

        );

        Dereferencer<InputStream> deref
                = iri -> remote.retrieve(URI.create(iri.getIRIString()));

        DereferencerContentAddressed derefCas = new DereferencerContentAddressed(deref, blobStore);


        IRI dereferenced = derefCas.get(RefNodeFactory.toIRI(resourceURI));

        streamProvenance(resourceURI, dereferenced, listener);
        recordProvenance(resourceURI, keyToPath, dereferenced);
        return blobStore.get(dereferenced);
    }

    private void streamProvenance(URI resourceURI, IRI dereferenced, StatementListener statementListener) {
        Quad quad = RefNodeFactory.toStatement(
                RefNodeFactory.toIRI(resourceURI),
                RefNodeConstants.HAS_VERSION,
                dereferenced
        );
        statementListener.on(quad);
    }

    private void recordProvenance(URI resourceURI, KeyTo1LevelPath keyToPath, IRI dereferenced) throws IOException {
        URI localPathURI = keyToPath.toPath(dereferenced);
        ContentProvenance contentProvenanceWithNamespace
                = new ContentProvenance(namespace,
                resourceURI,
                localPathURI,
                StringUtils.replace(dereferenced.getIRIString(), "hash://sha256/", ""),
                DateUtil.nowDateString());

        ProvenanceLog.appendProvenanceLog(
                new File(cachePath),
                contentProvenanceWithNamespace
        );
    }


}
