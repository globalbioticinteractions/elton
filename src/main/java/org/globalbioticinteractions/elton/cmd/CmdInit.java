package org.globalbioticinteractions.elton.cmd;

import com.Ostermiller.util.LabeledCSVParser;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.eol.globi.util.CSVTSVUtil;
import org.eol.globi.util.HttpUtil;
import org.eol.globi.util.ResourceUtil;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(separators = "= ", commandDescription = "Initialize a GloBI indexed dataset configuration")
public class CmdInit extends CmdDefaultParams {

    @Parameter(names = {"--data-url"}, description = "data url", required = true)
    private String dataUrl;

    @Parameter(names = {"--data-citation"}, description = "data citation", required = true)
    private String dataCitation;

    @Override
    public void run() {
        for (String namespace : getNamespaces()) {
            try {
                System.err.print("generating [README.md]...");
                write(generateReadme(getDataCitation(), namespace), "README.md");
                System.err.println(" done.");
                System.err.print("generating [globi.json]...");
                write(generateConfig(getDataUrl(), getDataCitation()), "globi.json");
                System.err.println(" done.");
                System.err.print("generating [.travis.yml]...");
                InputStream travis = getClass().getResourceAsStream("/org/globalbioticinteractions/elton/template/.travis.yml");
                IOUtils.copy(travis, getFileOutputStream(".travis.yml"));
                System.err.println(" done.");
                System.err.print("generating [.gitignore]...");
                InputStream gitIgnore = getClass().getResourceAsStream("/org/globalbioticinteractions/elton/template/default.gitignore");
                IOUtils.copy(gitIgnore, getFileOutputStream(".gitignore"));
                System.err.println(" done.");
            } catch (IOException e) {
                throw new RuntimeException("failed to initialize [" + namespace + "]", e);
            }
        }

    }



    static String generateReadme(String citation, String namespace) {
        return "[![Build Status](https://travis-ci.com/" + namespace + ".svg)](https://travis-ci.com/" + namespace + ") [![GloBI](http://api.globalbioticinteractions.org/interaction.svg?accordingTo=globi:" + namespace + ")](http://globalbioticinteractions.org/?accordingTo=globi:" + namespace + ")\n" +
                "\n" +
                "Configuration to help make: \n\n" +
                citation +
                "\navailable through Global Biotic Interactions (GloBI, https://globalbioticinteractions.org).";
    }

    static List<String> firstTwoLines(String urlString) throws IOException {
        try (InputStream inputStream = asInputStream(urlString)) {
            BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(inputStream));

            String candidateHeader = inputStreamReader.readLine();
            String candidateValues = inputStreamReader.readLine();

            return Arrays.asList(candidateHeader, candidateValues);

        }

    }

    private static InputStream asInputStream(String urlString) throws IOException {
        InputStream is;
        if (StringUtils.startsWith(urlString, "http")) {
            is = URI.create(urlString).toURL().openStream();
        } else {
            is = ResourceUtil.asInputStream(urlString);
        }
        return is;
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

    static String generateConfig(String urlString, String citation) throws IOException {
        List<String> firstTwoLines = firstTwoLines(urlString);

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
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("@context", mapper.readTree("[\"http://www.w3.org/ns/csvw\", {\"@language\": \"en\"}]"));
        objectNode.put("rdfs:comment", mapper.readTree("[\"inspired by https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/\"]"));
        objectNode.put("url", urlString);
        objectNode.put("dcterms:bibliographicCitation", citation);
        objectNode.put("delimiter", delimiter);
        objectNode.put("headerRowCount", 1);
        objectNode.put("null", mapper.readTree("[\"\"]"));
        objectNode.put("tableSchema", generateColumns(columnNames));
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
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
        objectNode.put("columns", arrayNode);
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

    private FileOutputStream getFileOutputStream(String filename) throws FileNotFoundException {
        return new FileOutputStream(new File(new File(getWorkDir()), filename));
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
