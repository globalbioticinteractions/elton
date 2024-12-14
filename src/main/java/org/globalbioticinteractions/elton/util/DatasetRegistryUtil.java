package org.globalbioticinteractions.elton.util;

import bio.guoda.preston.process.StatementListener;
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
import org.globalbioticinteractions.elton.store.AccessLogger;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.store.ActivityProxy;
import org.globalbioticinteractions.elton.store.ProvLogger;

import java.io.File;
import java.net.URI;
import java.util.Arrays;

public class DatasetRegistryUtil {

    public static final String NAMESPACE_LOCAL = "local";
    public static final String NAMESPACE_ZENODO = "zenodo";
    public static final String NAMESPACE_GITHUB = "github";

    public static DatasetRegistry forLocalDir(final URI localArchiveDir,
                                              ResourceService resourceServiceRemote,
                                              ContentPathFactory contentPathFactory,
                                              String dataDir,
                                              String provDir,
                                              ActivityListener dereferenceListener) {
        return new DatasetRegistrySingleDir(
                localArchiveDir,
                resourceServiceRemote,
                contentPathFactory,
                dataDir,
                provDir,
                dereferenceListener
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
        return new DatasetRegistryLocal(
                provDir,
                getCacheFactoryLocal(dataDir, provDir, resourceServiceLocal, contentPathFactory, provenancePathFactory),
                resourceServiceLocal);
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
                                                     StatementListener listener) {
        return forCacheOrLocalDir(
                dataDir,
                provDir,
                workDir,
                new ResourceServiceLocal(streamFactory),
                new ResourceServiceLocalAndRemote(streamFactory, new File(dataDir)),
                contentPathFactory,
                provenancePathFactory,
                listener);
    }

    public static DatasetRegistry forCacheOrLocalDir(String dataDir,
                                                     String provDir,
                                                     URI workDir,
                                                     ResourceService resourceServiceLocal,
                                                     ResourceService resourceServiceRemote,
                                                     ContentPathFactory contentPathFactory,
                                                     ProvenancePathFactory provenancePathFactory,
                                                     StatementListener listener) {
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
                    new ActivityProxy(
                            Arrays.asList(
                                    new ProvLogger(listener),
                                    new AccessLogger(NAMESPACE_LOCAL, provDir)
                            )
                    )
            );
        }
        return registry;
    }

}
