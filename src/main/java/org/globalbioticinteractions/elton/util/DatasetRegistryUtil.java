package org.globalbioticinteractions.elton.util;

import org.eol.globi.service.ResourceService;
import org.eol.globi.util.InputStreamFactory;
import org.eol.globi.util.ResourceServiceLocal;
import org.eol.globi.util.ResourceServiceLocalAndRemote;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.cache.CacheLocalReadonly;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryLocal;

import java.net.URI;

public class DatasetRegistryUtil {

    public static final String NAMESPACE_LOCAL = "local";
    public static final String NAMESPACE_ZENODO = "zenodo";
    public static final String NAMESPACE_GITHUB = "github";

    public static DatasetRegistry forLocalDir(final URI localArchiveDir,
                                              final String cacheDir,
                                              ResourceService resourceServiceRemote) {
        return new DatasetRegistrySingleDir(
                localArchiveDir,
                cacheDir,
                resourceServiceRemote
        );
    }

    private static CacheFactory getCacheFactoryLocal(String cacheDir,
                                                     ResourceService resourceServiceLocal) {
        return dataset -> new CacheLocalReadonly(
                dataset.getNamespace(),
                cacheDir,
                resourceServiceLocal
        );
    }

    public static DatasetRegistry forCacheDir(String cacheDir,
                                              ResourceService resourceServiceLocal) {
        return new DatasetRegistryLocal(
                cacheDir,
                getCacheFactoryLocal(cacheDir, resourceServiceLocal),
                resourceServiceLocal);
    }

    private static boolean isEmpty(DatasetRegistry registry) {
        try {
            return !registry.findNamespaces().iterator().hasNext();
        } catch (DatasetRegistryException e) {
            return false;
        }
    }

    public static DatasetRegistry forCacheDirOrLocalDir(String cacheDir, URI workDir, InputStreamFactory streamFactory) {
        return forCacheDirOrLocalDir(cacheDir, workDir, new ResourceServiceLocal(streamFactory), new ResourceServiceLocalAndRemote(streamFactory));
    }

    public static DatasetRegistry forCacheDirOrLocalDir(String cacheDir, URI workDir, ResourceService resourceServiceLocal, ResourceService resourceServiceRemote) {
        DatasetRegistry registry = forCacheDir(cacheDir, resourceServiceLocal);
        if (isEmpty(registry)) {
            registry = forLocalDir(
                    workDir,
                    cacheDir,
                    resourceServiceRemote
            );
        }
        return registry;
    }

}
