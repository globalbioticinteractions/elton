package org.globalbioticinteractions.dataset;

import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFinder;
import org.eol.globi.service.DatasetFinderException;
import org.globalbioticinteractions.cache.CacheLog;
import org.globalbioticinteractions.cache.CacheUtil;
import org.globalbioticinteractions.cache.CachedURI;
import org.globalbioticinteractions.dataset.DatasetWithCache;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;

public class DatasetFinderLogger implements DatasetFinder {
    private final DatasetFinder finder;

    private final String cacheDir;

    public DatasetFinderLogger(DatasetFinder finder, String cacheDir) {
        this.finder = finder;
        this.cacheDir = cacheDir;
    }

    public Collection<String> findNamespaces() throws DatasetFinderException {
        return this.getFinder().findNamespaces();
    }

    public Dataset datasetFor(String namespace) throws DatasetFinderException {
        Dataset dataset = this.getFinder().datasetFor(namespace);

        try {
            String accessedAt = ISODateTimeFormat.dateTime().withZoneUTC().print(new Date().getTime());
            CachedURI meta = new CachedURI(namespace, dataset.getArchiveURI(), null, null, accessedAt);
            meta.setType("application/globi");
            CacheLog.appendAccessLog(meta, CacheLog.getAccessFile(CacheUtil.getCacheDirForNamespace(this.getCacheDir(), namespace)));
        } catch (IOException var4) {
            throw new DatasetFinderException("failed to record access", var4);
        }

        return dataset;
    }

    private DatasetFinder getFinder() {
        return this.finder;
    }

    private String getCacheDir() {
        return cacheDir;
    }



}