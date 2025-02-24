package org.globalbioticinteractions.dataset;

import org.globalbioticinteractions.cache.CacheUtil;
import org.globalbioticinteractions.cache.ContentProvenance;
import org.globalbioticinteractions.cache.ProvenanceLog;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.function.Consumer;

public class DatasetRegistryLogIntoAccessTable implements DatasetRegistry {
    private final DatasetRegistry registry;

    private final String provDir;

    public DatasetRegistryLogIntoAccessTable(DatasetRegistry registry, String provDir) {
        this.registry = registry;
        this.provDir = provDir;
    }

    public Iterable<String> findNamespaces() throws DatasetRegistryException {
        return this.getRegistry().findNamespaces();
    }

    @Override
    public void findNamespaces(Consumer<String> consumer) throws DatasetRegistryException {
        this.getRegistry().findNamespaces(consumer);
    }

    public Dataset datasetFor(String namespace) throws DatasetRegistryException {
        Dataset dataset = this.getRegistry().datasetFor(namespace);

        try {
            String accessedAt = ISODateTimeFormat.dateTime().withZoneUTC().print(new Date().getTime());
            ContentProvenance prov = new ContentProvenance(namespace, dataset.getArchiveURI(), null, null, accessedAt);
            prov.setType(CacheUtil.MIME_TYPE_GLOBI);
            ProvenanceLog.appendProvenanceLog(new File(getProvDir()), prov);
        } catch (IOException var4) {
            throw new DatasetRegistryException("failed to record access", var4);
        }

        return dataset;
    }

    private DatasetRegistry getRegistry() {
        return this.registry;
    }

    private String getProvDir() {
        return provDir;
    }



}