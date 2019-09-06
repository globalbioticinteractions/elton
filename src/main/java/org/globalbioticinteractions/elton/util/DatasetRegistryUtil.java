package org.globalbioticinteractions.elton.util;

import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetImpl;
import org.eol.globi.service.DatasetRegistry;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.cache.CacheLocalReadonly;
import org.globalbioticinteractions.cache.CachePullThrough;
import org.globalbioticinteractions.dataset.DatasetRegistryLocal;
import org.globalbioticinteractions.dataset.DatasetWithCache;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

public class DatasetRegistryUtil {
    public static DatasetRegistry forLocalDir(URI localDir) {
        return new DatasetRegistry() {
            private final String localStaticNamespace = "local";

            @Override
            public Collection<String> findNamespaces() throws DatasetFinderException {
                return Collections.singletonList(localStaticNamespace);
            }

            @Override
            public Dataset datasetFor(String namespace) throws DatasetFinderException {
                DatasetImpl local = new DatasetImpl("local", localDir);
                return new DatasetWithCache(local,
                        new CachePullThrough("local", ".elton"));
            }
        };
    }

    private static CacheFactory getCacheFactoryLocal(String cacheDir) {
        return dataset -> new CacheLocalReadonly(dataset.getNamespace(), cacheDir);
    }

    public static DatasetRegistry forCacheDir(String cacheDir) {
        return new DatasetRegistryLocal(cacheDir, getCacheFactoryLocal(cacheDir));
    }

    public static boolean emptyFinder(DatasetRegistry finder) {
        try {
            Collection<String> namespaces = finder.findNamespaces();
            return namespaces.isEmpty();
        } catch (DatasetFinderException e) {
            return false;
        }
    }

    public static DatasetRegistry forCacheDirOrLocalDir(String cacheDir, URI workDir) {
        DatasetRegistry finder = forCacheDir(cacheDir);
        if (emptyFinder(finder)) {
            finder = forLocalDir(workDir);
        }
        return finder;
    }
}
