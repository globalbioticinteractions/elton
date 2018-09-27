package org.globalbioticinteractions.elton.util;

import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFinder;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetImpl;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.cache.CacheLocalReadonly;
import org.globalbioticinteractions.dataset.DatasetFinderLocal;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

public class DatasetFinderUtil {
    public static DatasetFinder forLocalDir(URI localDir) {
        return new DatasetFinder() {
            private final String localStaticNamespace = "local";

            @Override
                public Collection<String> findNamespaces() throws DatasetFinderException {
                return Collections.singletonList(localStaticNamespace);
                }

                @Override
                public Dataset datasetFor(String namespace) throws DatasetFinderException {
                    return new DatasetImpl("local", localDir);
                }
            };
    }

    private static CacheFactory getCacheFactoryLocal(String cacheDir) {
        return dataset -> new CacheLocalReadonly(dataset.getNamespace(), cacheDir);
    }

    public static DatasetFinder forCacheDir(String cacheDir) {
        return new DatasetFinderLocal(cacheDir, getCacheFactoryLocal(cacheDir));
    }
}
