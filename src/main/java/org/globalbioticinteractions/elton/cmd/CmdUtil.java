package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.cmd.ActivityContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.geo.LatLng;
import org.eol.globi.service.GeoNamesService;
import org.eol.globi.service.ResourceService;
import org.eol.globi.util.DatasetImportUtil;
import org.eol.globi.util.InputStreamFactory;
import org.eol.globi.util.ResourceServiceLocal;
import org.eol.globi.util.ResourceServiceLocalAndRemote;
import org.globalbioticinteractions.cache.Cache;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.cache.CacheLocalReadonly;
import org.globalbioticinteractions.cache.CacheProxy;
import org.globalbioticinteractions.cache.ContentPathFactory;
import org.globalbioticinteractions.cache.ProvenancePathFactory;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetFactory;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryLogger;
import org.globalbioticinteractions.dataset.DatasetRegistryWithCache;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.store.CachePullThroughPrestonStore;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CmdUtil {
    private static final Logger LOG = LoggerFactory.getLogger(CmdUtil.class);

    static void handleNamespaces(DatasetRegistry registry,
                                 NamespaceHandler handler,
                                 List<String> namespaces) throws DatasetRegistryException {
        Iterable<String> selectedNamespaces = new ArrayList<>(namespaces);
        if (!selectedNamespaces.iterator().hasNext()) {
            selectedNamespaces = registry.findNamespaces();
        }

        for (String namespace : selectedNamespaces) {
            try {
                handler.onNamespace(namespace);
            } catch (Exception e) {
                LOG.error("failed to handle namespace [" + namespace + "]", e);
            }
        }
    }

    static DatasetRegistry createDataFinderLoggingCaching(
            DatasetRegistry registry,
            String namespace,
            String dataDir,
            String provDir,
            InputStreamFactory factory,
            ContentPathFactory contentPathFactory,
            ProvenancePathFactory provenancePathFactory,
            ActivityListener dereferenceListener,
            ActivityContext ctx,
            Supplier<IRI> iriFactory) {

        CacheFactory cacheFactory = createCacheFactory(
                namespace,
                dataDir,
                provDir,
                factory,
                contentPathFactory,
                provenancePathFactory,
                dereferenceListener,
                ctx, iriFactory
        );
        return new DatasetRegistryWithCache(
                new DatasetRegistryLogger(registry, provDir),
                cacheFactory
        );
    }

    public static CacheFactory createCacheFactory(String namespace,
                                                  String dataDir,
                                                  String provDir,
                                                  InputStreamFactory factory,
                                                  ContentPathFactory contentPathFactory,
                                                  ProvenancePathFactory provenancePathFactory,
                                                  ActivityListener dereferenceListener,
                                                  ActivityContext ctx,
                                                  Supplier<IRI> iriFactory) {
        return dataset -> {
            ResourceService remote = new ResourceServiceLocalAndRemote(factory, new File(dataDir));
            ResourceService local = new ResourceServiceLocal(factory);
            Cache pullThroughCache = new CachePullThroughPrestonStore(
                    namespace,
                    remote,
                    contentPathFactory,
                    dataDir,
                    provDir,
                    dereferenceListener,
                    ctx,
                    iriFactory);

            CacheLocalReadonly readOnlyCache = new CacheLocalReadonly(
                    namespace,
                    dataDir,
                    provDir,
                    local,
                    contentPathFactory,
                    provenancePathFactory
            );
            return new CacheProxy(Arrays.asList(pullThroughCache, readOnlyCache));
        };
    }

    public static List<String> datasetInfo(Dataset dataset) {
        return StreamUtil.streamOf(dataset).collect(Collectors.toList());
    }

    public static void handleNamespaces(DatasetRegistry registry,
                                        NodeFactory nodeFactory,
                                        List<String> namespaces,
                                        String msgPrefix,
                                        Appendable out,
                                        ImportLogger logger,
                                        File workDir) {
        try {
            List<String> failedNamespaces = Collections.synchronizedList(new ArrayList<>());
            final AtomicReference<Throwable> firstException = new AtomicReference<>();
            handleNamespaces(registry, namespace -> {
                out.append(msgPrefix)
                        .append(" [")
                        .append(namespace)
                        .append("]... ");
                try {
                    handleSingleNamespace(registry, nodeFactory, namespace, logger, workDir);
                    out.append("done.\n");
                } catch (StudyImporterException | DatasetRegistryException ex) {
                    failedNamespaces.add(namespace);
                    if (firstException.get() == null) {
                        firstException.set(ex);
                    }
                    out.append("failed.\n");
                }
            }, namespaces);

            if (failedNamespaces.size() > 0 && firstException.get() != null) {
                throw new DatasetRegistryException("failed to import datasets [" + StringUtils.join(failedNamespaces, ";") + "], please check the logs.", firstException.get());
            }

        } catch (DatasetRegistryException e) {
            throw new RuntimeException(msgPrefix + " failed.", e);
        }
    }

    private static void handleSingleNamespace(DatasetRegistry registry,
                                              NodeFactory nodeFactory,
                                              String namespace,
                                              ImportLogger logger,
                                              File workDir) throws DatasetRegistryException, StudyImporterException {
        Dataset dataset = new DatasetFactory(registry).datasetFor(namespace);

        DatasetImportUtil.importDataset(
                null,
                dataset,
                nodeFactory,
                logger,
                createDummyGeoNamesService(),
                workDir
        );
    }

    private static GeoNamesService createDummyGeoNamesService() {
        return new GeoNamesService() {
            @Override
            public boolean hasTermForLocale(String locality) {
                return false;
            }

            @Override
            public LatLng findLatLng(String locality) throws IOException {
                return null;
            }
        };
    }
}
