package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.cmd.ActivityContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.LogUtil;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.domain.LogContext;
import org.eol.globi.util.DatasetImportUtil;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.cache.Cache;

import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetFactory;
import org.globalbioticinteractions.dataset.DatasetFactoryImpl;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryWithCache;
import org.globalbioticinteractions.dataset.DatasetWithCache;
import org.globalbioticinteractions.dataset.DatasetWithResourceMapping;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

class StreamingDatasetsHandler implements NamespaceHandler {
    private final static Logger LOG = LoggerFactory.getLogger(StreamingDatasetsHandler.class);
    private final String dataDir;
    private final PrintStream stderr;
    private final Dataset dataset;

    private NodeFactorFactory nodeFactorFactory;
    private ImportLoggerFactory loggerFactory;
    private Cache cache;

    public StreamingDatasetsHandler(Dataset dataset,
                                    String dataDir,
                                    PrintStream stderr,
                                    InputStreamFactory inputStreamFactory,
                                    NodeFactorFactory nodeFactorFactory,
                                    ImportLoggerFactory loggerFactory,
                                    ActivityContext ctx,
                                    Cache cache) {
        this.dataDir = dataDir;
        this.stderr = stderr;
        this.dataset = dataset;
        this.nodeFactorFactory = nodeFactorFactory;
        this.loggerFactory = loggerFactory;
        this.cache = cache;
    }

    @Override
    public void onNamespace(String namespace) throws Exception {
        URI archiveURI = this.dataset.getArchiveURI();
        if (archiveURI == null && this.dataset.getConfig() != null) {
            String url = this.dataset.getOrDefault("url", null);
            if (StringUtils.isBlank(url)) {
                throw new StudyImporterException("no or empty archiveURI for namespace [" + namespace + "]");
            }
            archiveURI = URI.create(url);
        }

        Dataset dataset = new DatasetWithResourceMapping(
                namespace,
                archiveURI,
                cache
        );

        dataset.setConfig(this.dataset.getConfig());

        DatasetWithCache datasetWithCache = new DatasetWithCache(dataset, cache);

        Dataset datasetWithCacheAndConfig = new DatasetFactoryImpl(new DatasetRegistry() {
            @Override
            public Iterable<String> findNamespaces() throws DatasetRegistryException {
                return null;
            }

            @Override
            public void findNamespaces(Consumer<String> namespaceConsumer) throws DatasetRegistryException {

            }

            @Override
            public Dataset datasetFor(String namespace) throws DatasetRegistryException {
                return datasetWithCache;
            }
        }, in -> in).datasetFor(namespace);

        NodeFactory nodeFactory = this.nodeFactorFactory.createNodeFactory();
        nodeFactory.getOrCreateDataset(datasetWithCacheAndConfig);
        final ImportLogger importLogger = loggerFactory.createImportLogger();
        try {
            DatasetImportUtil.importDataset(
                    null,
                    datasetWithCacheAndConfig,
                    nodeFactory,
                    importLogger,
                    new File(dataDir)
            );
        } catch (Throwable ex) {
            LogUtil.logError(importLogger, ex);
            throw ex;
        }
    }

}
