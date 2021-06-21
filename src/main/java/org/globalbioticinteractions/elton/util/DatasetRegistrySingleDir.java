package org.globalbioticinteractions.elton.util;

import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetImpl;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetWithCache;
import org.globalbioticinteractions.elton.store.CachePullThroughPrestonStore;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

public class DatasetRegistrySingleDir implements DatasetRegistry {
    private final URI localArchiveDir;
    private final InputStreamFactory streamFactory;
    private final String cacheDir;

    public DatasetRegistrySingleDir(URI localArchiveDir, String cacheDir, InputStreamFactory streamFactory) {
        this.localArchiveDir = localArchiveDir;
        this.cacheDir = cacheDir;
        this.streamFactory = streamFactory;
    }

    @Override
    public Collection<String> findNamespaces() throws DatasetRegistryException {
        return Collections.singletonList(DatasetRegistryUtil.NAMESPACE_LOCAL);
    }

    @Override
    public Dataset datasetFor(String namespace) throws DatasetRegistryException {
        DatasetImpl local = new DatasetImpl(
                DatasetRegistryUtil.NAMESPACE_LOCAL,
                localArchiveDir,
                streamFactory
        );

        return new DatasetWithCache(local,
                new CachePullThroughPrestonStore(
                        DatasetRegistryUtil.NAMESPACE_LOCAL,
                        cacheDir,
                        streamFactory
                )
        );
    }
}
