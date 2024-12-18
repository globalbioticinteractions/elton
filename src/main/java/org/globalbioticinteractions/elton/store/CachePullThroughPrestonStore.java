package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.Dereferencer;
import bio.guoda.preston.store.DereferencerContentAddressed;
import bio.guoda.preston.store.KeyTo1LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.cache.CachePullThrough;
import org.globalbioticinteractions.cache.ContentPathFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Supplier;

public class CachePullThroughPrestonStore extends CachePullThrough {

    private final String namespace;
    private final ResourceService resourceService;
    private final String dataDir;
    private final ActivityContext ctx;
    private final Supplier<IRI> iriFactory;

    private ActivityListener dereferenceListener;

    public CachePullThroughPrestonStore(String namespace,
                                        ResourceService resourceServiceLocal,
                                        ContentPathFactory contentPathFactory,
                                        String dataDir,
                                        String provDir,
                                        ActivityListener dereferenceListener,
                                        ActivityContext ctx,
                                        Supplier<IRI> iriFactory) {
        super(namespace,
                resourceServiceLocal,
                contentPathFactory,
                dataDir,
                provDir
        );
        this.namespace = namespace;
        this.dataDir = dataDir;
        this.resourceService = resourceServiceLocal;
        this.dereferenceListener = dereferenceListener;
        this.ctx = ctx;
        this.iriFactory = iriFactory;
    }

    @Override
    public InputStream retrieve(URI resourceURI) throws IOException {
        File dataFolder = new File(dataDir, namespace);
        KeyTo1LevelPath keyToPath = new KeyTo1LevelPath(dataFolder.toURI());
        BlobStoreAppendOnly blobStore = new BlobStoreAppendOnly(
                new KeyValueStoreLocalFileSystem(
                        dataFolder,
                        keyToPath,
                        new ValidatingKeyValueStreamContentAddressedFactory()
                ),
                true,
                HashType.sha256

        );

        Dereferencer<IRI> derefCas = new DereferencerContentAddressed(
                iri -> resourceService.retrieve(URI.create(iri.getIRIString())),
                blobStore
        );

        try {
            IRI request = RefNodeFactory.toIRI(resourceURI);
            IRI parentActivityId = ctx.getActivity();
            IRI activityId = iriFactory.get();

            dereferenceListener.onStarted(
                    parentActivityId,
                    activityId,
                    request
            );

            IRI response = derefCas.get(request);

            dereferenceListener.onCompleted(
                    parentActivityId,
                    activityId,
                    request,
                    response,
                    keyToPath.toPath(response)
            );

            return blobStore.get(response);
        } catch (IOException ex) {
            throw new IOException("failed to retrieve [" + resourceURI + "]", ex);
        }
    }


}
