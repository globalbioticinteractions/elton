package org.globalbioticinteractions.elton.cmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.util.CSVTSVUtil;
import org.eol.globi.util.InputStreamFactory;
import org.eol.globi.util.ResourceServiceLocalAndRemote;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CommandLine.Command(
        name = "init",
        description = CmdInit.DESCRIPTION
)
public class CmdInit extends CmdDefaultParams {

    public static final String DESCRIPTION = "Initialize a GloBI indexed dataset configuration";
    @CommandLine.Option(
            names = {"--data-url", "--data-urls"},
            description = "data url(s)",
            arity = "1..*",
            required = true)
    private List<String> dataUrls;

    @CommandLine.Option(
            names = {"--data-citation"},
            description = "data citation",
            required = true)
    private String dataCitation;

    @Override
    public void doRun() {
        if (getNamespaces().size() == 0) {
            throw new RuntimeException("no dataset namespace found: please provide one and only one dataset namespace");
        } else if (getNamespaces().size() > 1) {
            throw new RuntimeException("found multiple namespaces: please provide one and only one dataset namespace");
        }

        for (String namespace : getNamespaces()) {
            try {
                getStderr().print("generating [README.md]...");
                write(generateReadme(getDataCitation(), namespace), "README.md");
                getStderr().println(" done.");
                getStderr().print("generating [globi.json]...");
                write(generateConfig(getDataCitation(), is -> is, new File(getDataDir()), getDataUrls().get(0)), "globi.json");
                getStderr().println(" done.");
                getStderr().print("generating [.gitignore]...");
                InputStream gitIgnore = getClass().getResourceAsStream("/org/globalbioticinteractions/elton/template/default.gitignore");
                IOUtils.copy(gitIgnore, getFileOutputStream(".gitignore"));
                getStderr().println(" done.");
                getStderr().print("generating [.github/workflows/review.yml]...");
                InputStream githubAction = getClass().getResourceAsStream("/org/globalbioticinteractions/elton/template/github.review.action.yml");
                IOUtils.copy(githubAction, getFileOutputStream(".github/workflows/review.yml"));
                getStderr().println(" done.");
            } catch (IOException e) {
                throw new RuntimeException("failed to initialize [" + namespace + "]", e);
            }
        }

    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }


    static String generateReadme(String citation, String namespace) {
        return "[![GloBI Review by Elton](../../actions/workflows/review.yml/badge.svg)](../../actions/workflows/review.yml)" +
                " [![GloBI](https://api.globalbioticinteractions.org/interaction.svg?accordingTo=globi:" + namespace + "&refutes=true&refutes=false)](https://globalbioticinteractions.org/?accordingTo=globi:" + namespace + ")\n" +
                "\n" +
                "Configuration to help Global Biotic Interactions (GloBI, https://globalbioticinteractions.org) index: \n\n" +
                citation;
    }

    static List<String> firstTwoLines(String urlString, InputStreamFactory inputStreamFactory, File cacheDir) throws IOException {
        try (InputStream inputStream =
                     new ResourceServiceLocalAndRemote(inputStreamFactory, cacheDir)
                             .retrieve(URI.create(urlString))) {
            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(BOMInputStream.builder().setInputStream(inputStream).get()));


            String candidateHeader = inputStreamReader.readLine();
            String candidateValues = inputStreamReader.readLine();

            return Arrays.asList(candidateHeader, candidateValues);

        }

    }

    static List<String> inferColumnNamesTSV(List<String> firstTwoLines) {
        List<String> columnNames;
        if (firstTwoLines.size() < 2) {
            columnNames = Collections.emptyList();
        } else {
            String[] header = CSVTSVUtil.splitTSV(firstTwoLines.get(0));
            String[] values = CSVTSVUtil.splitTSV(firstTwoLines.get(1));
            columnNames = header != null
                    && values != null
                    && header.length == values.length
                    && header.length > 1
                    ? Arrays.asList(header)
                    : Collections.emptyList();
        }
        return columnNames;
    }

    static String generateConfig(String citation,
                                 InputStreamFactory inputStreamFactory,
                                 File cacheDir,
                                 String urlString) throws IOException {
        List<String> firstTwoLines = firstTwoLines(urlString, inputStreamFactory, cacheDir);

        TableSchema tableSchema = new TableSchemaFactory().create(urlString, citation, firstTwoLines);
        String actualConfig;
        if (tableSchema.getColumnNames() == null || tableSchema.getColumnNames().isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode node = objectMapper.createObjectNode();
            node.put("format", "unknown");
            node.put("citation", citation);
            node.put("url", urlString);
            actualConfig = node.toString();
        } else {
            ObjectNode tableNode = generateConfigForTable(tableSchema);
            actualConfig = generateConfigForTables(tableNode);
        }
        return actualConfig;
    }

    private static ObjectNode generateConfigForTable(TableSchema tableSchema) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode tableNode = mapper.createObjectNode();
        tableNode.set("@context", mapper.readTree("[\"http://www.w3.org/ns/csvw\", {\"@language\": \"en\"}]"));
        tableNode.set("rdfs:comment", mapper.readTree("[\"inspired by https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/\"]"));
        tableNode.put("url", tableSchema.getUrlString());
        tableNode.put("dcterms:bibliographicCitation", tableSchema.getCitation());
        tableNode.put("delimiter", tableSchema.getDelimiter());
        tableNode.put("headerRowCount", 1);
        tableNode.set("null", mapper.readTree("[\"\"]"));
        tableNode.set("tableSchema", generateColumns(tableSchema.getColumnNames()));
        return tableNode;
    }

    private static String generateConfigForTables(ObjectNode... tableNodes) throws JsonProcessingException {
        ObjectMapper mapper2 = new ObjectMapper();
        final ArrayNode tables = mapper2.createArrayNode();
        for (JsonNode tableNode : tableNodes) {
            tables.add(tableNode);
        }

        ObjectNode rootNode = mapper2.createObjectNode();
        rootNode.set("@context", mapper2.readTree("[\"http://www.w3.org/ns/csvw\", {\"@language\": \"en\"}]"));
        rootNode.set("rdfs:comment", mapper2.readTree("[\"inspired by https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/\"]"));
        rootNode.set("tables", tables);

        return mapper2.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
    }

    private static JsonNode generateColumns(List<String> columnNames) {
        ArrayNode arrayNode = new ObjectMapper().createArrayNode();

        columnNames
                .stream()
                .map(StringUtils::trim)
                .map(headerName -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ObjectNode objectNode = objectMapper.createObjectNode();
                    objectNode.put("name", headerName);
                    objectNode.put("titles", headerName);
                    objectNode.put("datatype", "string");
                    return objectNode;
                })
                .forEach(arrayNode::add);

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.set("columns", arrayNode);
        return objectNode;
    }

    private void write(String content, String filename) throws IOException {
        IOUtils.copy(IOUtils.toInputStream(content, StandardCharsets.UTF_8),
                getFileOutputStream(filename));
    }

    private FileOutputStream getFileOutputStream(String filename) throws IOException {
        File file = new File(new File(getWorkDir()), filename);
        FileUtils.forceMkdirParent(file);
        return new FileOutputStream(file);
    }

    public void setDataUrls(String dataUrls) {
        setDataUrls(Arrays.asList(dataUrls));
    }

    public void setDataUrls(List<String> dataUrls) {
        this.dataUrls = dataUrls;
    }

    public List<String> getDataUrls() {
        return dataUrls;
    }

    public void setDataCitation(String dataCitation) {
        this.dataCitation = dataCitation;
    }

    public String getDataCitation() {
        return dataCitation;
    }
}
