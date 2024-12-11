package org.globalbioticinteractions.elton.cmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.domain.LogContext;
import org.eol.globi.tool.NullImportLogger;

import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.ProgressCursor;
import org.globalbioticinteractions.elton.util.ProgressCursorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@CommandLine.Command(
        name = "stream",
        description = CmdStream.DESCRIPTION
)

public class CmdStream extends CmdDefaultParams {

    private final static Logger LOG = LoggerFactory.getLogger(CmdStream.class);
    public static final String DESCRIPTION = "stream interactions associated with dataset configuration provided by globi.json line-json as input.\n" +
            "example input:" +
            "{ \"namespace\": \"hash://sha256/9cd053d40ef148e16389982ea16d724063b82567f7ba1799962670fc97876fbf\", \"citation\": \"hash://sha256/9cd053d40ef148e16389982ea16d724063b82567f7ba1799962670fc97876fbf\", \"format\": \"dwca\", \"url\": \"https://linker.bio/hash://sha256/9cd053d40ef148e16389982ea16d724063b82567f7ba1799962670fc97876fbf\" }\n";

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    @CommandLine.Option(names = {"--record-type"},
            description = "record types (e.g., interaction, name, review)"
    )
    private String recordType = "interaction";

    @Override
    public void doRun() {

        BufferedReader reader = IOUtils.buffer(new InputStreamReader(getStdin(), StandardCharsets.UTF_8));
        AtomicBoolean isFirst = new AtomicBoolean(true);
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonNode jsonNode = new ObjectMapper().readTree(line);
                    String namespace = jsonNode.at("/namespace").asText(DatasetRegistryUtil.NAMESPACE_LOCAL);
                    if (StringUtils.isNotBlank(namespace)) {
                        ImportLoggerFactory loggerFactory = new ImportLoggerFactoryImpl(recordType, namespace, Arrays.asList(ReviewCommentType.values()), getStdout());
                        try {
                            boolean shouldWriteHeader = isFirst.get();
                            StreamingDatasetsHandler namespaceHandler = new StreamingDatasetsHandler(
                                    jsonNode,
                                    getDataDir(),
                                    getProvDir(),
                                    getStderr(),
                                    createInputStreamFactory(),
                                    new NodeFactoryFactoryImpl(shouldWriteHeader, recordType, loggerFactory.createImportLogger()),
                                    loggerFactory,
                                    getContentPathFactory(),
                                    getProvenancePathFactory()
                            );
                            namespaceHandler.onNamespace(namespace);
                            isFirst.set(false);
                        } catch (Exception e) {
                            String msg = "failed to add dataset associated with namespace [" + namespace + "]";
                            loggerFactory.createImportLogger().warn(new LogContext() {
                                @Override
                                public String toString() {
                                    return "{ \"namespace\": \"" + namespace + "\" }";
                                }
                            }, msg);
                            LOG.error(msg, e);
                        } finally {
                            FileUtils.forceDelete(new File(this.getDataDir()));
                        }
                    }
                } catch (JsonProcessingException e) {
                    // ignore non-json lines
                }
            }
        } catch (IOException ex) {
            LOG.error("failed to read from stdin", ex);
        }

    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public static class ImportLoggerFactoryImpl implements ImportLoggerFactory {
        private final String recordType;
        private final String namespace;
        private final List<ReviewCommentType> desiredReviewCommentTypes;
        private final PrintStream stdout;

        public ImportLoggerFactoryImpl(String recordType,
                                       String namespace,
                                       List<ReviewCommentType> desiredReviewCommentTypes,
                                       PrintStream stdout) {
            this.recordType = recordType;
            this.namespace = namespace;
            this.desiredReviewCommentTypes = desiredReviewCommentTypes;
            this.stdout = stdout;
        }

        @Override
        public ImportLogger createImportLogger() {
            ImportLogger logger;
            if (Arrays.asList("name", "interaction").contains(recordType)) {
                logger = new NullImportLogger();
            } else if (StringUtils.equals("review", recordType)) {
                logger = new ReviewReportLogger(
                        new ReviewReport(namespace, desiredReviewCommentTypes),
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
            NodeFactory factory;
            if (StringUtils.equals("interaction", recordType)) {
                factory = WriterUtil.nodeFactoryForInteractionWriting(shouldWriteHeader, getStdout());
            } else if (StringUtils.equals("name", recordType)) {
                factory = WriterUtil.nodeFactoryForTaxonWriting(shouldWriteHeader, getStdout());
            } else if (StringUtils.equals("review", recordType)) {
                factory = WriterUtil.nodeFactoryForReviewWriting(shouldWriteHeader, getStdout(), logger);
            } else {
                throw new NotImplementedException("no node factory for [" + recordType + "] available yet.");
            }
            return factory;
        }
    }

    public static class WriterFactory {

    }
}
