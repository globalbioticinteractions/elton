package org.globalbioticinteractions.elton.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.util.DatasetImportUtil;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.cache.Cache;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetWithCache;
import org.globalbioticinteractions.dataset.DatasetWithResourceMapping;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

class StreamingDatasetsHandler implements NamespaceHandler {
    private final static Logger LOG = LoggerFactory.getLogger(StreamingDatasetsHandler.class);
    private final String cacheDir;
    private final PrintStream stderr;

    private InputStreamFactory factory;
    private final JsonNode config;
    private NodeFactorFactory nodeFactorFactory;

    public StreamingDatasetsHandler(JsonNode config,
                                    String cacheDir,
                                    PrintStream stderr,
                                    InputStreamFactory inputStreamFactory,
                                    NodeFactorFactory nodeFactorFactory) {
        this.factory = inputStreamFactory;
        this.cacheDir = cacheDir;
        this.stderr = stderr;
        this.config = config;
        this.nodeFactorFactory = nodeFactorFactory;

    }

    @Override
    public void onNamespace(String namespace) throws Exception {
        stderr.print("tracking [" + namespace + "]...");
        CacheFactory cacheFactory = CmdUtil.createCacheFactory(
                namespace,
                cacheDir,
                factory
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
                    null);
            stderr.println("done.");
        } catch (StudyImporterException ex) {
            LOG.error("tracking of [" + namespace + "] failed.", ex);
            stderr.println("failed with [ " + ex.getMessage() + "].");
            ex.printStackTrace(stderr);
        }

        IOUtils.write("wrote [" + namespace + "]\n", stderr, StandardCharsets.UTF_8);
    }

}
