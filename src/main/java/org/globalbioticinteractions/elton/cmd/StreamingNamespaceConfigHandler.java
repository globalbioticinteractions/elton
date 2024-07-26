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
import org.globalbioticinteractions.elton.util.DatasetProcessorForTSV;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.NodeFactoryForDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

class StreamingNamespaceConfigHandler implements NamespaceHandler {
    private final static Logger LOG = LoggerFactory.getLogger(StreamingNamespaceConfigHandler.class);
    private final String cacheDir;
    private final PrintStream stderr;
    private final PrintStream stdout;

    private InputStreamFactory factory;
    private final JsonNode config;
    private boolean shouldWriteHeader;

    public StreamingNamespaceConfigHandler(JsonNode jsonNode,
                                           InputStreamFactoryLogging inputStreamFactory,
                                           String cacheDir,
                                           PrintStream stderr,
                                           PrintStream stdout) {
        this.factory = inputStreamFactory;
        this.cacheDir = cacheDir;
        this.stderr = stderr;
        this.stdout = stdout;
        this.config = jsonNode;
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

        CmdInteractions.TsvWriter writer = new CmdInteractions.TsvWriter(stdout);
        if (shouldWriteHeader) {
            writer.writeHeader();
        }

        NodeFactory factory = new NodeFactoryForDataset(writer, new DatasetProcessorForTSV());

        factory.getOrCreateDataset(dataset);
        try {
            DatasetImportUtil.importDataset(
                    null,
                    datasetWithCache,
                    factory,
                    null);
            stderr.println("done.");
        } catch (StudyImporterException ex) {
            LOG.error("tracking of [" + namespace + "] failed.", ex);
            stderr.println("failed with [ " + ex.getMessage() + "].");
            ex.printStackTrace(stderr);
        }

        IOUtils.write("wrote [" + namespace + "]\n", stderr, StandardCharsets.UTF_8);
    }

    public void setShouldWriteHeader(boolean shouldWriteHeader) {
        this.shouldWriteHeader = shouldWriteHeader;
    }
}
