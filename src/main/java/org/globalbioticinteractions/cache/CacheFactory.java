package org.globalbioticinteractions.cache;

import org.eol.globi.service.Dataset;
import org.globalbioticinteractions.cache.Cache;

public interface CacheFactory {

    Cache cacheFor(Dataset dataset);

}
