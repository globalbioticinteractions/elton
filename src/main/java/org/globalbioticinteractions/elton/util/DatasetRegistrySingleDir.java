package org.globalbioticinteractions.elton.util;

import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetImpl;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetWithCache;
import org.globalbioticinteractions.elton.store.CachePullThroughPrestonStore;

import java.net.URI;
import java.util.Collections;
import java.util.function.Consumer;

public class DatasetRegistrySingleDir implements DatasetRegistry {
    private final URI localArchiveDir;
    private final String cacheDir;
    private ResourceService resourceService;

    public DatasetRegistrySingleDir(URI localArchiveDir,
                                    String cacheDir,
                                    ResourceService resourceService) {
        this.localArchiveDir = localArchiveDir;
        this.cacheDir = cacheDir;
        this.resourceService = resourceService;
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
                        cacheDir,
                        this.resourceService
                )
        );
    }
}
