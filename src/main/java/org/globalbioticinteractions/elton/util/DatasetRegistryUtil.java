package org.globalbioticinteractions.elton.util;

import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetImpl;
import org.eol.globi.service.DatasetRegistry;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.cache.CacheLocalReadonly;
import org.globalbioticinteractions.cache.CachePullThrough;
import org.globalbioticinteractions.dataset.DatasetRegistryLocal;
import org.globalbioticinteractions.dataset.DatasetWithCache;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

public class DatasetRegistryUtil {

    public static final String NAMESPACE_LOCAL = "local";

    public static DatasetRegistry forLocalDir(final URI localDir, final String localCacheDir, InputStreamFactory streamFactory) {
        return new DatasetRegistry() {
            private final String localStaticNamespace = NAMESPACE_LOCAL;

            @Override
            public Collection<String> findNamespaces() throws DatasetFinderException {
                return Collections.singletonList(localStaticNamespace);
            }

            @Override
            public Dataset datasetFor(String namespace) throws DatasetFinderException {
                DatasetImpl local = new DatasetImpl(NAMESPACE_LOCAL, localDir, streamFactory);

                return new DatasetWithCache(local,
                        new CachePullThrough(NAMESPACE_LOCAL, localCacheDir, streamFactory));
            }
        };
    }

    private static CacheFactory getCacheFactoryLocal(String cacheDir, InputStreamFactory streamFactory) {
        return dataset -> new CacheLocalReadonly(dataset.getNamespace(), cacheDir, streamFactory);
    }

    public static DatasetRegistry forCacheDir(String cacheDir, InputStreamFactory streamFactory) {
        return new DatasetRegistryLocal(cacheDir, getCacheFactoryLocal(cacheDir, streamFactory), streamFactory);
    }

    public static boolean emptyFinder(DatasetRegistry finder) {
        try {
            Collection<String> namespaces = finder.findNamespaces();
            return namespaces.isEmpty();
        } catch (DatasetFinderException e) {
            return false;
        }
    }

    public static DatasetRegistry forCacheDirOrLocalDir(String cacheDir, URI workDir, String tmpDir, InputStreamFactory streamFactory) {
        DatasetRegistry finder = forCacheDir(cacheDir, streamFactory);
        if (emptyFinder(finder)) {
            finder = forLocalDir(workDir, tmpDir, streamFactory);
        }
        return finder;
    }
}
