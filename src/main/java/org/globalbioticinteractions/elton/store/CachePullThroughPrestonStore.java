package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.Dereferencer;
import bio.guoda.preston.store.DereferencerContentAddressed;
import bio.guoda.preston.store.KeyTo1LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
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

    public CachePullThroughPrestonStore(
            String namespace,
            String cachePath,
            ResourceService resourceService
    ) {
        super(namespace, cachePath,
                resourceService);
        this.namespace = namespace;
        this.cachePath = cachePath;
        this.remote = resourceService;
    }

    public InputStream retrieve(URI resourceURI) throws IOException {
        CacheUtil.findOrMakeCacheDirForNamespace(cachePath, namespace);

        File cacheDir = new File(cachePath, namespace);
        KeyTo1LevelPath keyToPath = new KeyTo1LevelPath(cacheDir.toURI(), HashType.sha256);
        BlobStoreAppendOnly blobStore = new BlobStoreAppendOnly(
                new KeyValueStoreLocalFileSystem(
                        cacheDir,
                        keyToPath,
                        new KeyValueStoreLocalFileSystem.KeyValueStreamFactoryValues(HashType.sha256)
                ),
                true,
                HashType.sha256

        );

        Dereferencer<InputStream> deref
                = iri -> remote.retrieve(URI.create(iri.getIRIString()));

        DereferencerContentAddressed derefCas = new DereferencerContentAddressed(deref, blobStore);


        IRI dereferenced = derefCas.get(RefNodeFactory.toIRI(resourceURI));

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
        return blobStore.get(dereferenced);
    }


}
