package org.globalbioticinteractions.elton.util;

import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.service.ResourceService;
import org.eol.globi.util.InputStreamFactory;
import org.eol.globi.util.ResourceServiceLocal;
import org.eol.globi.util.ResourceServiceLocalAndRemote;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.cache.CacheLocalReadonly;
import org.globalbioticinteractions.cache.ContentPathFactory;
import org.globalbioticinteractions.cache.ProvenancePathFactory;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryLocal;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.store.LocalPathToHashIRI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Supplier;

public class DatasetRegistryUtil {

    public static final String NAMESPACE_LOCAL = "local";
    public static final String NAMESPACE_ZENODO = "zenodo";
    public static final String NAMESPACE_GITHUB = "github";

    public static DatasetRegistry forLocalDir(final URI localArchiveDir,
                                              ResourceService resourceServiceRemote,
                                              ContentPathFactory contentPathFactory,
                                              String dataDir,
                                              String provDir,
                                              ActivityListener dereferenceListener,
                                              ActivityContext ctx,
                                              Supplier<IRI> activityIdFactory) {
        return new DatasetRegistrySingleDir(
                localArchiveDir,
                resourceServiceRemote,
                contentPathFactory,
                dataDir,
                provDir,
                dereferenceListener,
                ctx,
                activityIdFactory
        );
    }

    private static CacheFactory getCacheFactoryLocal(String dataDir,
                                                     String provDir,
                                                     ResourceService resourceServiceLocal,
                                                     ContentPathFactory contentPathFactory,
                                                     ProvenancePathFactory provenancePathFactory) {
        return dataset -> new CacheLocalReadonly(
                dataset.getNamespace(),
                dataDir,
                provDir,
                resourceServiceLocal,
                contentPathFactory,
                provenancePathFactory
        );
    }

    public static DatasetRegistry forCache(String dataDir,
                                           String provDir,
                                           ResourceService resourceServiceLocal,
                                           ContentPathFactory contentPathFactory,
                                           ProvenancePathFactory provenancePathFactory) {

        CacheFactory cacheFactory = getCacheFactoryLocal(
                dataDir,
                provDir,
                resourceServiceLocal,
                contentPathFactory,
                provenancePathFactory
        );

        return new DatasetRegistryLocal(
                provDir,
                cacheFactory,
                resourceServiceLocal
        );
    }

    private static boolean isEmpty(DatasetRegistry registry) {
        try {
            return !registry.findNamespaces().iterator().hasNext();
        } catch (DatasetRegistryException e) {
            return false;
        }
    }

    public static DatasetRegistry forCacheOrLocalDir(String dataDir,
                                                     String provDir,
                                                     URI workDir,
                                                     InputStreamFactory streamFactory,
                                                     ContentPathFactory contentPathFactory,
                                                     ProvenancePathFactory provenancePathFactory,
                                                     ActivityListener activityListener,
                                                     ActivityContext ctx,
                                                     Supplier<IRI> activityIdFactory) {
        File dataFolder = new File(dataDir);
        ResourceServiceListening resourceServiceLocal
                = new ResourceServiceListening(activityIdFactory, activityListener, ctx, new ResourceServiceLocal(streamFactory), new LocalPathToHashIRI(dataFolder));
        ResourceServiceListening resourceServiceRemote
                = new ResourceServiceListening(activityIdFactory, activityListener, ctx, new ResourceServiceLocalAndRemote(streamFactory, dataFolder), new LocalPathToHashIRI(dataFolder));
        return forCacheOrLocalDir(
                dataDir,
                provDir,
                workDir,
                resourceServiceLocal,
                resourceServiceRemote,
                contentPathFactory,
                provenancePathFactory,
                activityListener,
                ctx,
                activityIdFactory
        );
    }

    private static DatasetRegistry forCacheOrLocalDir(String dataDir,
                                                      String provDir,
                                                      URI workDir,
                                                      ResourceService resourceServiceLocal,
                                                      ResourceService resourceServiceRemote,
                                                      ContentPathFactory contentPathFactory,
                                                      ProvenancePathFactory provenancePathFactory,
                                                      ActivityListener dereferenceListener,
                                                      ActivityContext ctx,
                                                      Supplier<IRI> activityIdFactory) {
        DatasetRegistry registry = forCache(
                dataDir,
                provDir,
                resourceServiceLocal,
                contentPathFactory,
                provenancePathFactory
        );
        if (isEmpty(registry)) {
            registry = forLocalDir(
                    workDir,
                    resourceServiceRemote,
                    contentPathFactory,
                    dataDir,
                    provDir,
                    dereferenceListener,
                    ctx,
                    activityIdFactory
            );
        }
        return registry;
    }

    private static class ResourceServiceListening implements ResourceService {
        private final Supplier<IRI> activityIdFactory;
        private final ActivityListener activityListener;
        private final ActivityContext ctx;
        private final ResourceService service;
        private final LocalPathToHashIRI localPathToHashIRI;

        public ResourceServiceListening(
                Supplier<IRI> activityIdFactory,
                ActivityListener activityListener,
                ActivityContext ctx,
                ResourceService service,
                LocalPathToHashIRI localPathToHashIRI
        ) {
            this.activityIdFactory = activityIdFactory;
            this.activityListener = activityListener;
            this.ctx = ctx;
            this.service = service;
            this.localPathToHashIRI = localPathToHashIRI;
        }

        @Override
        public InputStream retrieve(URI uri) throws IOException {
            IRI accessId = activityIdFactory.get();
            activityListener.onStarted(ctx.getActivity(), accessId, RefNodeFactory.toIRI(uri));
            InputStream is = service.retrieve(uri);

            return new ProxyInputStream(is) {
                @Override
                public void close() throws IOException {
                    IRI request = RefNodeFactory.toIRI(uri);
                    IRI response = RefNodeFactory.toIRI(uri);
                    activityListener.onCompleted(
                            ctx.getActivity(),
                            accessId,
                            localPathToHashIRI.get(request),
                            localPathToHashIRI.get(response),
                            null
                    );
                    IOUtils.close(in, this::handleIOException);
                }
            };
        }
    }
}
