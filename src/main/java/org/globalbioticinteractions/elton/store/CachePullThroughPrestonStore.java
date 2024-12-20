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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CachePullThroughPrestonStore extends CachePullThrough {

    private final ResourceService resourceService;
    private final ActivityContext ctx;
    private final Supplier<IRI> iriFactory;
    private final BlobStoreAppendOnly blobStore;

    private final ActivityListener dereferenceListener;
    private final KeyTo1LevelPath keyToPath;

    private final AtomicReference<IRI> currentContext = new AtomicReference<>(null);
    private final Map<IRI, IRI> successfullyRequested = Collections.synchronizedMap(new HashMap<>());
    private final LocalPathToHashIRI translateToHashSpace;


    public CachePullThroughPrestonStore(String namespace,
                                        ResourceService resourceService,
                                        ContentPathFactory contentPathFactory,
                                        String dataDir,
                                        String provDir,
                                        ActivityListener dereferenceListener,
                                        ActivityContext ctx,
                                        Supplier<IRI> iriFactory) {
        super(namespace,
                resourceService,
                contentPathFactory,
                dataDir,
                provDir
        );
        this.resourceService = resourceService;
        this.dereferenceListener = dereferenceListener;
        this.ctx = ctx;
        this.iriFactory = iriFactory;
        File dataFolder = new File(dataDir, namespace);

        this.translateToHashSpace = new LocalPathToHashIRI(dataFolder);
        this.keyToPath = new KeyTo1LevelPath(dataFolder.toURI());
        this.blobStore = new BlobStoreAppendOnly(
                new KeyValueStoreLocalFileSystem(
                        dataFolder,
                        keyToPath,
                        new ValidatingKeyValueStreamContentAddressedFactory()
                ),
                true,
                HashType.sha256
        );

    }

    @Override
    public InputStream retrieve(URI resourceURI) throws IOException {
        DereferencerContentAddressed dca = new DereferencerContentAddressed(
                iri -> resourceService.retrieve(URI.create(iri.getIRIString())),
                blobStore
        );
        Dereferencer<IRI> derefCas = new Dereferencer<IRI>() {
            @Override
            public IRI get(IRI requested) throws IOException {
                updateContext();

                return successfullyRequested.containsKey(requested)
                        ? successfullyRequested.get(requested)
                        : retrieve(requested);
            }

            private IRI retrieve(IRI request) throws IOException {
                IRI parentActivityId = ctx.getActivity();
                IRI activityId = iriFactory.get();

                dereferenceListener.onStarted(
                        parentActivityId,
                        activityId,
                        translateToHashSpace.get(request)
                );

                IRI response = dca.get(request);

                dereferenceListener.onCompleted(
                        parentActivityId,
                        activityId,
                        translateToHashSpace.get(request),
                        response,
                        keyToPath.toPath(response)
                );

                successfullyRequested.put(request, response);
                return response;
            }

            private void updateContext() {
                if (currentContext.get() == null
                        || !currentContext.get().equals(ctx.getActivity())) {
                    successfullyRequested.clear();
                    currentContext.set(ctx.getActivity());
                }
            }
        };

        try {
            IRI request = RefNodeFactory.toIRI(resourceURI);
            IRI response = derefCas.get(request);
            return blobStore.get(response);
        } catch (IOException ex) {
            throw new IOException("failed to retrieve [" + resourceURI + "]", ex);
        }
    }


}
