package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.DateUtil;
import bio.guoda.preston.DerefProgressListener;
import bio.guoda.preston.DerefState;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.BlobStoreReadOnly;
import bio.guoda.preston.store.KeyValueStoreConfig;
import bio.guoda.preston.store.KeyValueStoreFactoryImpl;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import bio.guoda.preston.stream.ContentHashDereferencer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.domain.LogContext;
import org.eol.globi.tool.NullImportLogger;
import org.globalbioticinteractions.cache.Cache;
import org.globalbioticinteractions.cache.ContentProvenance;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetProxy;
import org.globalbioticinteractions.elton.util.ProgressCursor;
import org.globalbioticinteractions.elton.util.ProgressCursorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;

import static bio.guoda.preston.RefNodeConstants.BIODIVERSITY_DATASET_GRAPH;
import static bio.guoda.preston.RefNodeConstants.BIODIVERSITY_DATASET_GRAPH_UUID_STRING;
import static org.apache.commons.lang3.StringUtils.startsWith;

@CommandLine.Command(
        name = "stream",
        description = CmdStream.DESCRIPTION
)

public class CmdStream extends CmdDefaultParams {

    private final static Logger LOG = LoggerFactory.getLogger(CmdStream.class);
    public static final String DESCRIPTION = "stream interactions associated with dataset configuration provided by globi.json line-json as input.\n" +
            "example input:" +
            "{ \"namespace\": \"hash://sha256/9cd053d40ef148e16389982ea16d724063b82567f7ba1799962670fc97876fbf\", \"citation\": \"hash://sha256/9cd053d40ef148e16389982ea16d724063b82567f7ba1799962670fc97876fbf\", \"format\": \"dwca\", \"url\": \"hash://sha256/9cd053d40ef148e16389982ea16d724063b82567f7ba1799962670fc97876fbf\" }\n";

    @CommandLine.Option(
            names = {"--remote", "--remotes", "--include", "--repos", "--repositories"},
            split = ",",
            description = "Included repository dependencies (e.g., https://linker.bio/,https://softwareheritage.org,https://wikimedia.org,https://dataone.org,https://zenodo.org)"
    )

    private List<URI> remotes = new ArrayList<>();

    @CommandLine.Option(
            names = {"--no-cache", "--disable-cache"},
            defaultValue = "false",
            description = "Disable local content cache"
    )
    private Boolean disableCache = false;

    @CommandLine.Option(
            names = {"-d", "--depth"},
            defaultValue = "2",
            description = "folder depth of data dir"
    )
    private int depth = 2;

    @CommandLine.Option(
            names = {"--anchor"},
            defaultValue = "urn:uuid:" + BIODIVERSITY_DATASET_GRAPH_UUID_STRING,
            description = "provenance anchor (aka bill of material identifier)"
    )
    private IRI provenanceAnchor = BIODIVERSITY_DATASET_GRAPH;

    private boolean supportTarGzDiscovery = true;


    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    @CommandLine.Option(names = {"--record-type"},
            description = "record types (e.g., interaction, name, review)"
    )
    private String recordType = "interaction";


    @CommandLine.Option(names = {"--config"},
                description = "point to content id (hash) of globi.json config to apply global settings (e.g., custom interaction type mappings). Example: hash://sha256/02682fdd62a3e985dc06236662299f00ec5453c4e6f707d02efa93628f927649 for:\n" +
                        "{\n" +
                        "  \"resources\": {\n" +
                        "    \"interaction_types_mapping.csv\": \"hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7\"\n" +
                        "  }\n" +
                        "}\n"
    )
    private URI configOverrideResource = null;

    @Override
    public void doRun() {

        KeyValueStoreConfig config
                = new KeyValueStoreConfig(
                new File(getDataDir()),
                new File(getWorkDir()),
                2,
                isCacheEnabled(),
                getRemotes(),
                getHashType(),
                getProgressListener(),
                isSupportTarGzDiscovery(),
                getProvenanceAnchor()
        );

        BlobStoreReadOnly blobStore = new BlobStoreAppendOnly(
                new KeyValueStoreFactoryImpl(config)
                        .getKeyValueStore(new ValidatingKeyValueStreamContentAddressedFactory()),
                true,
                getHashType()
        );

        AtomicBoolean shouldWriteHeader = new AtomicBoolean(true);
        try {

            LineIterator lineIterator = IOUtils.lineIterator(getStdin(), StandardCharsets.UTF_8);

            DatasetConfigReader jsonDatasetConfigReader = new DatasetConfigReaderJson();

            DatasetConfigReaderProv provDatasetConfigReader = new DatasetConfigReaderProv();

            while (lineIterator.hasNext()) {
                String line = lineIterator.next();
                Dataset dataset = jsonDatasetConfigReader.readConfig(line);
                if (dataset == null) {
                    dataset = provDatasetConfigReader.readConfig(line);
                }
                handleDataset(blobStore, shouldWriteHeader, dataset);
            }

            provDatasetConfigReader.close();
            handleDataset(blobStore, shouldWriteHeader, provDatasetConfigReader.datasetForContextOrReset());

        } catch (IOException ex) {
            LOG.error("failed to read from stdin", ex);
        }
    }

    private void handleDataset(BlobStoreReadOnly blobStore, AtomicBoolean shouldWriteHeader, Dataset dataset) throws IOException {
        if (dataset != null) {
            Cache cache = getCache(blobStore, dataset.getNamespace(), getProvenanceAnchor() == null ? "" : getProvenanceAnchor().getIRIString());
            if (handleDataset(dataset, shouldWriteHeader.get(), cache)) {
                shouldWriteHeader.set(false);
            }
        }
    }

    private DerefProgressListener getProgressListener() {
        return hideProgressIndicator()
        ? (iri, derefState, l, l1) -> {}
        : new DerefProgressListener() {
    private ProgressCursor progressCursor = getProgressCursorFactory().createProgressCursor();

    @Override
    public void onProgress(IRI dataURI, DerefState derefState, long read, long total) {
        progressCursor.increment();
    }
};
    }

    private boolean isCacheEnabled() {
        return !getDisableCache();
    }

    private boolean handleDataset(final Dataset datasetProvided, boolean shouldWriteHeader, Cache cache) throws IOException {
        boolean handled = false;
        ImportLoggerFactory loggerFactory = new ImportLoggerFactoryImpl(
                recordType,
                datasetProvided.getNamespace(),
                Arrays.asList(ReviewCommentType.values()),
                getStdout(),
                getProvenanceAnchor() == null ? null : getProvenanceAnchor().getIRIString()
        );
        try {
            Dataset datasetApplied = hasConfigOverride()
                    ? applyConfigOverride(datasetProvided, cache)
                    : datasetProvided;

            StreamingDatasetsHandler namespaceHandler = new StreamingDatasetsHandler(
                    datasetApplied,
                    getDataDir(),
                    getStderr(),
                    createInputStreamFactory(),
                    new NodeFactoryFactoryImpl(shouldWriteHeader, recordType, loggerFactory.createImportLogger()),
                    loggerFactory,
                    getActivityContext(),
                    cache
            );
            namespaceHandler.onNamespace(datasetProvided.getNamespace());
            handled = true;
        } catch (Exception e) {
            String msg = "failed to add dataset associated with namespace [" + datasetProvided.getNamespace() + "]";
            loggerFactory.createImportLogger().warn(new LogContext() {
                @Override
                public String toString() {
                    return "{ \"namespace\": \"" + datasetProvided.getNamespace() + "\" }";
                }
            }, msg);
            LOG.error(msg, e);
        } finally {
            // FileUtils.forceDelete(new File(this.getDataDir()));
        }
        return handled;

    }

    private boolean hasConfigOverride() {
        return configOverrideResource != null;
    }

    private Dataset applyConfigOverride(Dataset datasetProvided, Cache cache) throws IOException {
        Dataset datasetApplied;
        datasetApplied = new DatasetProxy(datasetProvided);
        JsonNode config = new ObjectMapper().readTree(cache.retrieve(configOverrideResource));
        datasetApplied.setConfig(config);
        return datasetApplied;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public List<URI> getRemotes() {
        return remotes;
    }

    public Boolean getDisableCache() {
        return disableCache;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isSupportTarGzDiscovery() {
        return supportTarGzDiscovery;
    }

    public void setRemotes(List<URI> remotes) {
        this.remotes = remotes;
    }

    public void setConfigOverrideResource(URI configOverrideResource) {
        this.configOverrideResource = configOverrideResource;
    }

    public IRI getProvenanceAnchor() {
        return provenanceAnchor;
    }

    public void setProvenanceAnchor(IRI provenanceAnchor) {
        this.provenanceAnchor = provenanceAnchor;
    }

    public static class ImportLoggerFactoryImpl implements ImportLoggerFactory {
        private final String recordType;
        private final String namespace;
        private final List<ReviewCommentType> desiredReviewCommentTypes;
        private final PrintStream stdout;
        private String provenanceAnchor;

        public ImportLoggerFactoryImpl(String recordType,
                                       String namespace,
                                       List<ReviewCommentType> desiredReviewCommentTypes,
                                       PrintStream stdout,
                                       String provenanceAnchor) {
            this.recordType = recordType;
            this.namespace = namespace;
            this.desiredReviewCommentTypes = desiredReviewCommentTypes;
            this.stdout = stdout;
            this.provenanceAnchor = provenanceAnchor;
        }

        @Override
        public ImportLogger createImportLogger() {
            ImportLogger logger;
            if (Arrays.asList("name", "interaction").contains(recordType)) {
                logger = new NullImportLogger();
            } else if (StringUtils.equals("review", recordType)) {
                logger = new ReviewReportLogger(
                        new ReviewReport(namespace, desiredReviewCommentTypes, provenanceAnchor),
                        stdout,
                        null,
                        new ProgressCursorFactory() {
                            @Override
                            public ProgressCursor createProgressCursor() {
                                return new ProgressCursor() {
                                    @Override
                                    public void increment() {

                                    }
                                };
                            }
                        });
            } else {
                throw new NotImplementedException("no import logger for [" + recordType + "] available yet.");
            }

            return logger;
        }
    }

    public class NodeFactoryFactoryImpl implements NodeFactorFactory {

        private final boolean shouldWriteHeader;
        private final String recordType;
        private ImportLogger logger;

        public NodeFactoryFactoryImpl(boolean shouldWriteHeader, String recordType, ImportLogger logger) {
            this.shouldWriteHeader = shouldWriteHeader;
            this.recordType = recordType;
            this.logger = logger;
        }

        @Override
        public NodeFactory createNodeFactory() {
            String recordType = this.recordType;
            return WriterUtil.getNodeFactoryForType(recordType, shouldWriteHeader, getStdout(), logger);
        }
    }

    public static Cache getCache(BlobStoreReadOnly blobStore, final String namespace, final String provenanceAnchor) {
        return new Cache() {
            @Override
            public ContentProvenance provenanceOf(URI resourceURI) {
                return new ContentProvenance(
                        namespace,
                        resourceURI,
                        resourceURI,
                        provenanceAnchor,
                        DateUtil.now()
                );
            }

            @Override
            public InputStream retrieve(URI uri) throws IOException {
                IRI iri = RefNodeFactory.toIRI(uri);
                if (startsWith(iri.getIRIString(), "jar:")) {
                    iri = RefNodeFactory.toIRI("zip:" + org.apache.commons.lang3.StringUtils.substring(iri.getIRIString(), "jar:".length()));
                }
                InputStream inputStream = new ContentHashDereferencer(blobStore).get(iri);
                
                return StringUtils.endsWith(iri.getIRIString(), ".gz")
                        ? new GZIPInputStream(inputStream)
                        : inputStream;
            }

        };
    }


}
