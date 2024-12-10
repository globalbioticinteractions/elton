package org.globalbioticinteractions.elton.util;

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

import java.io.File;
import java.net.URI;

public class DatasetRegistryUtil {

    public static final String NAMESPACE_LOCAL = "local";
    public static final String NAMESPACE_ZENODO = "zenodo";
    public static final String NAMESPACE_GITHUB = "github";

    public static DatasetRegistry forLocalDir(final URI localArchiveDir,
                                              final String cacheDir,
                                              ResourceService resourceServiceRemote,
                                              ContentPathFactory contentPathFactory) {
        return new DatasetRegistrySingleDir(
                localArchiveDir,
                cacheDir,
                resourceServiceRemote,
                contentPathFactory
        );
    }

    private static CacheFactory getCacheFactoryLocal(String cacheDir,
                                                     String provDir,
                                                     ResourceService resourceServiceLocal,
                                                     ContentPathFactory contentPathFactory,
                                                     ProvenancePathFactory provenancePathFactory) {
        return dataset -> new CacheLocalReadonly(
                dataset.getNamespace(),
                cacheDir,
                provDir,
                resourceServiceLocal,
                contentPathFactory,
                provenancePathFactory
        );
    }

    public static DatasetRegistry forCacheDir(String cacheDir,
                                              String provDir,
                                              ResourceService resourceServiceLocal,
                                              ContentPathFactory contentPathFactory,
                                              ProvenancePathFactory provenancePathFactory) {
        return new DatasetRegistryLocal(
                cacheDir,
                getCacheFactoryLocal(cacheDir, provDir, resourceServiceLocal, contentPathFactory, provenancePathFactory),
                resourceServiceLocal);
    }

    private static boolean isEmpty(DatasetRegistry registry) {
        try {
            return !registry.findNamespaces().iterator().hasNext();
        } catch (DatasetRegistryException e) {
            return false;
        }
    }

    public static DatasetRegistry forCacheDirOrLocalDir(String cacheDir,
                                                        String provDir,
                                                        URI workDir,
                                                        InputStreamFactory streamFactory,
                                                        ContentPathFactory contentPathFactory,
                                                        ProvenancePathFactory provenancePathFactory) {
        return forCacheDirOrLocalDir(
                cacheDir,
                provDir,
                workDir,
                new ResourceServiceLocal(streamFactory),
                new ResourceServiceLocalAndRemote(streamFactory, new File(cacheDir)),
                contentPathFactory,
                provenancePathFactory);
    }

    public static DatasetRegistry forCacheDirOrLocalDir(String cacheDir,
                                                        String provDir,
                                                        URI workDir,
                                                        ResourceService resourceServiceLocal,
                                                        ResourceService resourceServiceRemote,
                                                        ContentPathFactory contentPathFactory,
                                                        ProvenancePathFactory provenancePathFactory) {
        DatasetRegistry registry = forCacheDir(
                cacheDir,
                provDir,
                resourceServiceLocal,
                contentPathFactory,
                provenancePathFactory
        );
        if (isEmpty(registry)) {
            registry = forLocalDir(
                    workDir,
                    cacheDir,
                    resourceServiceRemote,
                    contentPathFactory
            );
        }
        return registry;
    }

}
