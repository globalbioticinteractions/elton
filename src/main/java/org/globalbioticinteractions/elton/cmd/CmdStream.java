package org.globalbioticinteractions.elton.cmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eol.globi.data.NodeFactory;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

@CommandLine.Command(
        name = "stream",
        description = "stream interactions associated with dataset configuration provided by globi.json line-json as input.\n" +
                "example input:" +
                "{ \"namespace\": \"hash://sha256/9cd053d40ef148e16389982ea16d724063b82567f7ba1799962670fc97876fbf\", \"citation\": \"hash://sha256/9cd053d40ef148e16389982ea16d724063b82567f7ba1799962670fc97876fbf\", \"format\": \"dwca\", \"url\": \"https://linker.bio/hash://sha256/9cd053d40ef148e16389982ea16d724063b82567f7ba1799962670fc97876fbf\" }\n"
)

public class CmdStream extends CmdDefaultParams {

    private final static Logger LOG = LoggerFactory.getLogger(CmdStream.class);

    @CommandLine.Option(names = {"--record-type"},
            description = "record types (e.g., interaction, name, review)"
    )
    private String recordType = "interaction";

    @Override
    public void run() {

        BufferedReader reader = IOUtils.buffer(new InputStreamReader(getStdin(), StandardCharsets.UTF_8));
        AtomicBoolean isFirst = new AtomicBoolean(true);
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonNode jsonNode = new ObjectMapper().readTree(line);
                    String namespace = jsonNode.at("/namespace").asText(DatasetRegistryUtil.NAMESPACE_LOCAL);
                    if (StringUtils.isNotBlank(namespace)) {
                        try {
                            boolean shouldWriteHeader = isFirst.get();
                            StreamingDatasetsHandler namespaceHandler = new StreamingDatasetsHandler(
                                    jsonNode,
                                    this.createInputStreamFactory(),
                                    this.getCacheDir(),
                                    this.getStderr(),
                                    new NodeFactoryFactoryImpl(shouldWriteHeader)
                            );
                            namespaceHandler.onNamespace(namespace);
                            isFirst.set(false);
                        } catch (Exception e) {
                            LOG.error("failed to add dataset associated with namespace [" + namespace + "]", e);
                        } finally {
                            FileUtils.forceDelete(new File(this.getCacheDir()));
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

    public class NodeFactoryFactoryImpl implements NodeFactorFactory {

        private final boolean shouldWriteHeader;

        public NodeFactoryFactoryImpl(boolean shouldWriteHeader) {
            this.shouldWriteHeader = shouldWriteHeader;
        }

        @Override
        public NodeFactory createNodeFactory() {

            return StringUtils.equals("name", recordType)
                    ? WriterUtil.nodeFactoryForInteractionWriting(shouldWriteHeader, getStdout())
                    : WriterUtil.nodeFactoryForTaxonWriting(shouldWriteHeader, getStdout());
        }
    }
}
