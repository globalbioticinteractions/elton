package org.globalbioticinteractions.elton.util;

import bio.guoda.preston.HashType;
import bio.guoda.preston.cmd.ActivityContext;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.cache.ContentPathFactory;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetImpl;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetWithCache;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.store.CachePullThroughPrestonStore;

import java.net.URI;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DatasetRegistrySingleDir implements DatasetRegistry {
    private final URI localArchiveDir;
    private final String provDir;
    private final String dataDir;
    private ResourceService resourceService;
    private ContentPathFactory contentPathFactory;
    private ActivityListener dereferenceListener;
    private ActivityContext ctx;
    private Supplier<IRI> activityIdFactory;
    private HashType hashType;

    public DatasetRegistrySingleDir(URI localArchiveDir,
                                    ResourceService resourceService,
                                    ContentPathFactory contentPathFactory,
                                    String dataDir,
                                    String provDir,
                                    ActivityListener dereferenceListener,
                                    ActivityContext ctx,
                                    Supplier<IRI> activityIdFactory,
                                    HashType hashType) {
        this.localArchiveDir = localArchiveDir;
        this.resourceService = resourceService;
        this.contentPathFactory = contentPathFactory;
        this.dataDir = dataDir;
        this.provDir = provDir;
        this.dereferenceListener = dereferenceListener;
        this.ctx = ctx;
        this.activityIdFactory = activityIdFactory;
        this.hashType = hashType;
    }

    @Override
    public Iterable<String> findNamespaces() throws DatasetRegistryException {
        return Collections.singletonList(DatasetRegistryUtil.NAMESPACE_LOCAL);
    }

    @Override
    public void findNamespaces(Consumer<String> consumer) throws DatasetRegistryException {
        findNamespaces().forEach(consumer);
    }

    @Override
    public Dataset datasetFor(String namespace) throws DatasetRegistryException {
        DatasetImpl local = new DatasetImpl(
                DatasetRegistryUtil.NAMESPACE_LOCAL,
                resourceService,
                localArchiveDir
        );

        return new DatasetWithCache(local,
                new CachePullThroughPrestonStore(
                        DatasetRegistryUtil.NAMESPACE_LOCAL,
                        this.resourceService,
                        contentPathFactory,
                        dataDir,
                        provDir,
                        dereferenceListener,
                        ctx,
                        activityIdFactory,
                        hashType
                )
        );
    }
}
