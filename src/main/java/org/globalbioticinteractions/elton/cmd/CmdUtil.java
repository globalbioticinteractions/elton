package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinder;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.GitHubImporterFactory;
import org.globalbioticinteractions.cache.Cache;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.cache.CacheLocalReadonly;
import org.globalbioticinteractions.cache.CacheProxy;
import org.globalbioticinteractions.cache.CachePullThrough;
import org.globalbioticinteractions.dataset.CitationUtil;
import org.globalbioticinteractions.dataset.DatasetFinderLocal;
import org.globalbioticinteractions.dataset.DatasetFinderLogger;
import org.globalbioticinteractions.dataset.DatasetFinderWithCache;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.StreamUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CmdUtil {
    private static final Log LOG = LogFactory.getLog(CmdUtil.class);

    static void handleNamespaces(DatasetFinder finder, NamespaceHandler handler, List<String> namespaces) throws DatasetFinderException {
        List<String> selectedNamespaces = new ArrayList<>(namespaces);
        if (selectedNamespaces.isEmpty()) {
            selectedNamespaces = new ArrayList<>(finder.findNamespaces());
        }

        for (String namespace : selectedNamespaces) {
            try {
                handler.onNamespace(namespace);
            } catch (Exception e) {
                LOG.error("failed to handle namespace [" + namespace + "]", e);
            }
        }
    }

    static DatasetFinderWithCache createDataFinderLoggingCaching(DatasetFinder finder, String namespace, String cacheDir) {
        return new DatasetFinderWithCache(new DatasetFinderLogger(finder, cacheDir), new CacheFactory() {
            @Override
            public Cache cacheFor(Dataset dataset) {
                Cache pullThroughCache = new CachePullThrough(namespace, cacheDir);
                CacheLocalReadonly readOnlyCache = new CacheLocalReadonly(namespace, cacheDir);
                return new CacheProxy(Arrays.asList(pullThroughCache, readOnlyCache));
            }
        });
    }

    static CacheFactory getCacheFactoryLocal(String cacheDir) {
        return dataset -> new CacheLocalReadonly(dataset.getNamespace(), cacheDir);
    }

    static DatasetFinderLocal getDatasetFinderLocal(String cacheDir) {
        return new DatasetFinderLocal(cacheDir, getCacheFactoryLocal(cacheDir));
    }

    public static List<String> datasetInfo(Dataset dataset) {
        String citation = CitationUtil.citationOrDefaultFor(dataset, "");
        return StreamUtil.streamOf(dataset, citation).collect(Collectors.toList());
    }

    public static void handleNamespaces(DatasetFinder finder, NodeFactory nodeFactory, List<String> namespaces, String msgPrefix) {
        try {
            handleNamespaces(finder, namespace -> {
                String msg = msgPrefix + " [" + namespace + "]...";
                LOG.info(msg);
                Dataset dataset = DatasetFactory.datasetFor(namespace, finder);
                nodeFactory.getOrCreateDataset(dataset);
                new GitHubImporterFactory()
                        .createImporter(dataset, nodeFactory)
                        .importStudy();
                LOG.info(msg + "done.");
            }, namespaces);
        } catch (DatasetFinderException e) {
            throw new RuntimeException("failed to complete name scan", e);
        }
    }
}
