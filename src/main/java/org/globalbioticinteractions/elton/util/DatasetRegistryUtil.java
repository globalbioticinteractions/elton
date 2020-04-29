package org.globalbioticinteractions.elton.util;

import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.cache.CacheLocalReadonly;
import org.globalbioticinteractions.dataset.DatasetRegistryLocal;

import java.net.URI;
import java.util.Collection;

public class DatasetRegistryUtil {

    public static final String NAMESPACE_LOCAL = "local";

    public static DatasetRegistry forLocalDir(final URI localArchiveDir, final String cacheDir, InputStreamFactory streamFactory) {
        return new DatasetRegistrySingleDir(localArchiveDir, cacheDir, streamFactory);
    }

    private static CacheFactory getCacheFactoryLocal(String cacheDir, InputStreamFactory streamFactory) {
        return dataset -> new CacheLocalReadonly(dataset.getNamespace(), cacheDir, streamFactory);
    }

    public static DatasetRegistry forCacheDir(String cacheDir, InputStreamFactory streamFactory) {
        return new DatasetRegistryLocal(
                cacheDir,
                getCacheFactoryLocal(cacheDir, streamFactory),
                streamFactory);
    }

    private static boolean isEmpty(DatasetRegistry registry) {
        try {
            Collection<String> namespaces = registry.findNamespaces();
            return namespaces.isEmpty();
        } catch (DatasetRegistryException e) {
            return false;
        }
    }

    public static DatasetRegistry forCacheDirOrLocalDir(String cacheDir, URI workDir, InputStreamFactory streamFactory) {
        DatasetRegistry registry = forCacheDir(cacheDir, streamFactory);
        if (isEmpty(registry)) {
            registry = forLocalDir(workDir, cacheDir, streamFactory);
        }
        return registry;
    }

}
