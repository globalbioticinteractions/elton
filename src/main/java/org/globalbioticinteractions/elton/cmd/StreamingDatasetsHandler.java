package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.cmd.ActivityContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.util.DatasetImportUtil;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.cache.Cache;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.cache.ContentPathFactory;
import org.globalbioticinteractions.cache.ProvenancePathFactory;

import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetWithCache;
import org.globalbioticinteractions.dataset.DatasetWithResourceMapping;
import org.globalbioticinteractions.elton.store.AccessLogger;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

class StreamingDatasetsHandler implements NamespaceHandler {
    private final static Logger LOG = LoggerFactory.getLogger(StreamingDatasetsHandler.class);
    private final String dataDir;
    private final PrintStream stderr;

    private InputStreamFactory factory;
    private final JsonNode config;
    private NodeFactorFactory nodeFactorFactory;
    private ImportLoggerFactory loggerFactory;
    private ContentPathFactory contentPathFactory;
    private ProvenancePathFactory provenancePathFactory;
    private final String provDir;
    private ActivityContext ctx;
    private Supplier<IRI> activityIdFactory;

    public StreamingDatasetsHandler(JsonNode config,
                                    String dataDir,
                                    String provDir,
                                    PrintStream stderr,
                                    InputStreamFactory inputStreamFactory,
                                    NodeFactorFactory nodeFactorFactory,
                                    ImportLoggerFactory loggerFactory,
                                    ContentPathFactory contentPathFactory,
                                    ProvenancePathFactory provenancePathFactory,
                                    ActivityContext ctx,
                                    Supplier<IRI> activityIdFactory) {
        this.factory = inputStreamFactory;
        this.dataDir = dataDir;
        this.provDir = provDir;
        this.stderr = stderr;
        this.config = config;
        this.nodeFactorFactory = nodeFactorFactory;
        this.loggerFactory = loggerFactory;
        this.contentPathFactory = contentPathFactory;
        this.provenancePathFactory = provenancePathFactory;
        this.ctx = ctx;
        this.activityIdFactory = activityIdFactory;
    }

    @Override
    public void onNamespace(String namespace) throws Exception {
        stderr.print("tracking [" + namespace + "]...");
        ActivityListener dereferenceListener = new AccessLogger(namespace, provDir);

        CacheFactory cacheFactory = CmdUtil.createCacheFactory(
                namespace,
                dataDir,
                provDir,
                factory,
                contentPathFactory,
                provenancePathFactory,
                dereferenceListener,
                ctx,
                activityIdFactory
        );

        Dataset dataset = new DatasetWithResourceMapping(
                namespace,
                URI.create(config.get("url").asText()),
                cacheFactory.cacheFor(null)
        );
        dataset.setConfig(config);
        Cache cache = cacheFactory.cacheFor(dataset);
        DatasetWithCache datasetWithCache = new DatasetWithCache(dataset, cache);

        NodeFactory nodeFactory = this.nodeFactorFactory.createNodeFactory();
        nodeFactory.getOrCreateDataset(dataset);
        try {
            DatasetImportUtil.importDataset(
                    null,
                    datasetWithCache,
                    nodeFactory,
                    loggerFactory.createImportLogger(),
                    new File(dataDir)
            );
            stderr.println("done.");
        } catch (StudyImporterException ex) {
            LOG.error("tracking of [" + namespace + "] failed.", ex);
            stderr.println("failed with [ " + ex.getMessage() + "].");
            ex.printStackTrace(stderr);
        }

        IOUtils.write("wrote [" + namespace + "]\n", stderr, StandardCharsets.UTF_8);
    }

}
