package org.globalbioticinteractions.elton.cmd;

import com.Ostermiller.util.LabeledCSVParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CommandLine.Command(
        name = "init",
        description = "Initialize a GloBI indexed dataset configuration"
)
public class CmdInit extends CmdDefaultParams {

    @CommandLine.Option(names = {"--data-url"}, description = "data url", required = true)
    private String dataUrl;

    @CommandLine.Option(names = {"--data-citation"}, description = "data citation", required = true)
    private String dataCitation;

    @Override
    public void run() {
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
                write(generateConfig(getDataUrl(), getDataCitation(), is -> is), "globi.json");
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


    static String generateReadme(String citation, String namespace) {
        return "[![GloBI Review by Elton](../../actions/workflows/review.yml/badge.svg)](../../actions/workflows/review.yml)" +
                " [![GloBI](https://api.globalbioticinteractions.org/interaction.svg?accordingTo=globi:" + namespace + "&refutes=true&refutes=false)](https://globalbioticinteractions.org/?accordingTo=globi:" + namespace + ")\n" +
                "\n" +
                "Configuration to help Global Biotic Interactions (GloBI, https://globalbioticinteractions.org) index: \n\n" +
                citation;
    }

    static List<String> firstTwoLines(String urlString, InputStreamFactory inputStreamFactory) throws IOException {
        try (InputStream inputStream = new ResourceServiceLocalAndRemote(inputStreamFactory).retrieve(URI.create(urlString))) {
            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(inputStream));

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

    static String generateConfig(String urlString, String citation, InputStreamFactory inputStreamFactory) throws IOException {
        List<String> firstTwoLines = firstTwoLines(urlString, inputStreamFactory);

        String actualConfig;
        List<String> columnNames = extractColumnNamesCSV(firstTwoLines);
        if (columnNames.size() > 0) {
            actualConfig = generateConfig(urlString, citation, columnNames, ",");
        } else {
            columnNames = inferColumnNamesTSV(firstTwoLines);
            if (columnNames.size() > 0) {
                actualConfig = generateConfig(urlString, citation, columnNames, "\t");
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode node = objectMapper.createObjectNode();
                node.put("format", "unknown");
                node.put("citation", citation);
                node.put("url", urlString);
                actualConfig = node.toString();
            }
        }
        return actualConfig;
    }

    private static String generateConfig(String urlString, String citation, List<String> columnNames, String delimiter) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode tableNode = mapper.createObjectNode();
        tableNode.set("@context", mapper.readTree("[\"http://www.w3.org/ns/csvw\", {\"@language\": \"en\"}]"));
        tableNode.set("rdfs:comment", mapper.readTree("[\"inspired by https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/\"]"));
        tableNode.put("url", urlString);
        tableNode.put("dcterms:bibliographicCitation", citation);
        tableNode.put("delimiter", delimiter);
        tableNode.put("headerRowCount", 1);
        tableNode.set("null", mapper.readTree("[\"\"]"));
        tableNode.set("tableSchema", generateColumns(columnNames));

        final ArrayNode tablesNode = mapper.createArrayNode();
        tablesNode.add(tableNode);

        ObjectNode rootNode = mapper.createObjectNode();
        rootNode.set("@context", mapper.readTree("[\"http://www.w3.org/ns/csvw\", {\"@language\": \"en\"}]"));
        rootNode.set("rdfs:comment", mapper.readTree("[\"inspired by https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/\"]"));
        rootNode.set("tables", tablesNode);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
    }

    private static List<String> extractColumnNamesCSV(List<String> firstTwoLines) throws IOException {
        return extractColumnNamesCSV(IOUtils.toInputStream(StringUtils.join(firstTwoLines, "\n"), StandardCharsets.UTF_8));
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

    private static List<String> extractColumnNamesCSV(InputStream inputStream) throws IOException {
        List<String> columnNames;
        try {
            LabeledCSVParser csvParser = new LabeledCSVParser(CSVTSVUtil.createCSVParser(inputStream));
            columnNames = csvParser.getLabels().length > 1
                    ? new ArrayList<>(Arrays.asList(csvParser.getLabels()))
                    : Collections.emptyList();
        } catch (IOException ex) {
            columnNames = Collections.emptyList();
        }
        return columnNames;
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

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataCitation(String dataCitation) {
        this.dataCitation = dataCitation;
    }

    public String getDataCitation() {
        return dataCitation;
    }
}
