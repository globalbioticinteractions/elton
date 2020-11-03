package org.globalbioticinteractions.elton.cmd;

import com.Ostermiller.util.LabeledCSVParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.util.CSVTSVUtil;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

public class CmdInitTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(expected = RuntimeException.class)
    public void throwOnNoNamespace() throws IOException {
        CmdInit cmdInit = new CmdInit();
        cmdInit.setDataUrl(getClass().getResource("/org/globalbioticinteractions/elton/cmd/data.csv").toExternalForm());
        cmdInit.setDataCitation("some citation");

        try {
            cmdInit.run();
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage(), Is.is("no dataset namespace found: please provide one and only one dataset namespace"));
            throw ex;
        }
    }

    @Test(expected = RuntimeException.class)
    public void throwOnMultipleNamespaces() throws IOException {
        CmdInit cmdInit = new CmdInit();
        cmdInit.setDataUrl(getClass().getResource("/org/globalbioticinteractions/elton/cmd/data.csv").toExternalForm());
        cmdInit.setDataCitation("some citation");
        cmdInit.getNamespaces().add("some/namespace1");
        cmdInit.getNamespaces().add("some/namespace2");

        try {
            cmdInit.run();
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage(), Is.is("found multiple namespaces: please provide one and only one dataset namespace"));
            throw ex;
        }
    }

    @Test
    public void gatherInput() throws IOException {
        File tempFile = folder.newFolder();

        CmdInit cmdInit = new CmdInit();
        cmdInit.setDataUrl(getClass().getResource("/org/globalbioticinteractions/elton/cmd/data.csv").toExternalForm());
        cmdInit.setDataCitation("some citation");
        cmdInit.setWorkDir(tempFile.getAbsolutePath());
        cmdInit.getNamespaces().add("some/namespace");

        assertFalse(new File(tempFile, "README.md").exists());
        assertFalse(new File(tempFile, "globi.json").exists());
        assertFalse(new File(tempFile, ".travis.yaml").exists());
        assertFalse(new File(tempFile, ".gitignore").exists());
        cmdInit.run();

        assertTrue(new File(tempFile, "README.md").exists());
        assertTrue(new File(tempFile, "globi.json").exists());
        assertTrue(new File(tempFile, ".travis.yml").exists());
        assertTrue(new File(tempFile, ".gitignore").exists());
    }

    @Test
    public void generateREADME() {
        String citation = "the parasite-host records in collection of the University of Michigan Museum of Zoology Division of Insects";
        String namespace = "globalbioticinteractions/ummzi";

        String expectedReadme = "[![Build Status](https://travis-ci.com/globalbioticinteractions/ummzi.svg)](https://travis-ci.com/globalbioticinteractions/ummzi) [![GloBI](http://api.globalbioticinteractions.org/interaction.svg?accordingTo=globi:globalbioticinteractions/ummzi)](http://globalbioticinteractions.org/?accordingTo=globi:globalbioticinteractions/ummzi)\n" +
                "\n" +
                "Configuration to help Global Biotic Interactions (GloBI, https://globalbioticinteractions.org) index: \n" +
                "\n" +
                "the parasite-host records in collection of the University of Michigan Museum of Zoology Division of Insects";

        String actualReadme = CmdInit.generateReadme(citation, namespace);

        assertThat(actualReadme, Is.is(expectedReadme));
    }

    private List<String> inferColumnNamesCSV(InputStream is) throws IOException {
        List<String> columnNames;
        try {
            LabeledCSVParser csvParser = new LabeledCSVParser(CSVTSVUtil.createCSVParser(is));
            columnNames = csvParser.getLabels().length > 1
                    ? new ArrayList<>(Arrays.asList(csvParser.getLabels()))
                    : Collections.emptyList();
        } catch (IOException ex) {
            columnNames = Collections.emptyList();
        }
        return columnNames;
    }

    @Test
    public void inferColumnNamesCSV() throws IOException {
        String urlString = "classpath:/org/globalbioticinteractions/elton/cmd/data.csv";
        List<String> firstTwoLines = CmdInit.firstTwoLines(urlString);
        List<String> columnNames = inferColumnNamesCSV(IOUtils.toInputStream(StringUtils.join(firstTwoLines, "\n"), StandardCharsets.UTF_8));

        assertEquals(columnNames, Arrays.asList("header1", "header2", "header3"));
    }

    @Test
    public void inferColumnNamesTSV() throws IOException {
        String urlString = "classpath:/org/globalbioticinteractions/elton/cmd/data.tsv";
        List<String> columnNames = CmdInit.inferColumnNamesTSV(CmdInit.firstTwoLines(urlString));

        assertEquals(columnNames, Arrays.asList("header1", "header2", "header3"));


    }

    @Test
    public void inferDefaultSchemaFromCSV() throws IOException {

        String expectedConfig = "{\n" +
                "  \"@context\" : [ \"http://www.w3.org/ns/csvw\", {\n" +
                "    \"@language\" : \"en\"\n" +
                "  } ],\n" +
                "  \"rdfs:comment\" : [ \"inspired by https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/\" ],\n" +
                "  \"tables\" : [ {\n" +
                "    \"@context\" : [ \"http://www.w3.org/ns/csvw\", {\n" +
                "      \"@language\" : \"en\"\n" +
                "    } ],\n" +
                "    \"rdfs:comment\" : [ \"inspired by https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/\" ],\n" +
                "    \"url\" : \"classpath:/org/globalbioticinteractions/elton/cmd/data.csv\",\n" +
                "    \"dcterms:bibliographicCitation\" : \"some citation\",\n" +
                "    \"delimiter\" : \",\",\n" +
                "    \"headerRowCount\" : 1,\n" +
                "    \"null\" : [ \"\" ],\n" +
                "    \"tableSchema\" : {\n" +
                "      \"columns\" : [ {\n" +
                "        \"name\" : \"header1\",\n" +
                "        \"titles\" : \"header1\",\n" +
                "        \"datatype\" : \"string\"\n" +
                "      }, {\n" +
                "        \"name\" : \"header2\",\n" +
                "        \"titles\" : \"header2\",\n" +
                "        \"datatype\" : \"string\"\n" +
                "      }, {\n" +
                "        \"name\" : \"header3\",\n" +
                "        \"titles\" : \"header3\",\n" +
                "        \"datatype\" : \"string\"\n" +
                "      } ]\n" +
                "    }\n" +
                "  } ]\n" +
                "}";

        String urlString = "classpath:/org/globalbioticinteractions/elton/cmd/data.csv";
        String citation = "some citation";

        String actualConfig = CmdInit.generateConfig(urlString, citation);
        assertThat(actualConfig, Is.is(expectedConfig));
    }

    @Test
    public void inferDefaultSchemaFromTSV() throws IOException {

        String expectedConfig = "{\n" +
                "  \"@context\" : [ \"http://www.w3.org/ns/csvw\", {\n" +
                "    \"@language\" : \"en\"\n" +
                "  } ],\n" +
                "  \"rdfs:comment\" : [ \"inspired by https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/\" ],\n" +
                "  \"tables\" : [ {\n" +
                "    \"@context\" : [ \"http://www.w3.org/ns/csvw\", {\n" +
                "      \"@language\" : \"en\"\n" +
                "    } ],\n" +
                "    \"rdfs:comment\" : [ \"inspired by https://www.w3.org/TR/2015/REC-tabular-data-model-20151217/\" ],\n" +
                "    \"url\" : \"classpath:/org/globalbioticinteractions/elton/cmd/data.tsv\",\n" +
                "    \"dcterms:bibliographicCitation\" : \"some citation\",\n" +
                "    \"delimiter\" : \"\\t\",\n" +
                "    \"headerRowCount\" : 1,\n" +
                "    \"null\" : [ \"\" ],\n" +
                "    \"tableSchema\" : {\n" +
                "      \"columns\" : [ {\n" +
                "        \"name\" : \"header1\",\n" +
                "        \"titles\" : \"header1\",\n" +
                "        \"datatype\" : \"string\"\n" +
                "      }, {\n" +
                "        \"name\" : \"header2\",\n" +
                "        \"titles\" : \"header2\",\n" +
                "        \"datatype\" : \"string\"\n" +
                "      }, {\n" +
                "        \"name\" : \"header3\",\n" +
                "        \"titles\" : \"header3\",\n" +
                "        \"datatype\" : \"string\"\n" +
                "      } ]\n" +
                "    }\n" +
                "  } ]\n" +
                "}";

        String urlString = "classpath:/org/globalbioticinteractions/elton/cmd/data.tsv";
        String citation = "some citation";

        String actualConfig = CmdInit.generateConfig(urlString, citation);
        assertThat(actualConfig, Is.is(expectedConfig));
    }

    @Test
    public void inferDefaultSchemaFromNonTSVCSV() throws IOException {

        String urlString = "classpath:/org/globalbioticinteractions/elton/cmd/data.txt";
        String citation = "some citation";

        String actualConfig = CmdInit.generateConfig(urlString, citation);
        assertThat(actualConfig, Is.is("{\"format\":\"unknown\",\"citation\":\"some citation\",\"url\":\"classpath:/org/globalbioticinteractions/elton/cmd/data.txt\"}"));
    }

}