package org.globalbioticinteractions.dataset;

import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetRegistry;
import org.globalbioticinteractions.cache.CacheLog;
import org.globalbioticinteractions.cache.CacheUtil;
import org.globalbioticinteractions.cache.CachedURI;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

public class DatasetRegistryLogger implements DatasetRegistry {
    private final DatasetRegistry finder;

    private final String cacheDir;

    public DatasetRegistryLogger(DatasetRegistry finder, String cacheDir) {
        this.finder = finder;
        this.cacheDir = cacheDir;
    }

    public Collection<String> findNamespaces() throws DatasetFinderException {
        return this.getRegistry().findNamespaces();
    }

    public Dataset datasetFor(String namespace) throws DatasetFinderException {
        Dataset dataset = this.getRegistry().datasetFor(namespace);

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

    private DatasetRegistry getRegistry() {
        return this.finder;
    }

    private String getCacheDir() {
        return cacheDir;
    }



}