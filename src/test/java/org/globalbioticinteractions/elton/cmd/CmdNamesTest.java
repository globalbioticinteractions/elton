package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CmdNamesTest {

    @Test
    public void namesNoHeader() throws URISyntaxException {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("names",
                "--cache-dir=" + CmdTestUtil.cacheDirTest(),
                "--skip-header",
                "globalbioticinteractions/template-dataset");

        Assert.assertEquals(jc.getParsedCommand(), "names");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdNames.class);
        CmdNames cmdNames = (CmdNames) actual.getObjects().get(0);
        assertThat(cmdNames.getNamespaces().size(), is(1));
        assertThat(cmdNames.getNamespaces(), hasItem("globalbioticinteractions/template-dataset"));

        if (actual.getObjects().get(0) instanceof Runnable) {
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(out1);
            ((CmdNames) actual.getObjects().get(0)).run(out);
            assertThat(out1.toString(), startsWith("\tLeptoconchus incycloseris"));
            assertThat(out1.toString().split("\n").length, is(22));
        }
    }

    @Test
    public void namesWithHeader() throws URISyntaxException {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("names",
                "--cache-dir=" + CmdTestUtil.cacheDirTest(),
                "globalbioticinteractions/template-dataset");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());

        if (actual.getObjects().get(0) instanceof Runnable) {
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(out1);
            ((CmdNames) actual.getObjects().get(0)).run(out);
            String actualOutput = out1.toString();
            String[] actualLines = StringUtils.splitByWholeSeparator(actualOutput, "\n");
            assertThat(actualLines[0], is("taxonId\ttaxonName\ttaxonRank\ttaxonPath\ttaxonPathIds\ttaxonPathNames\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion"));
            assertThat(actualLines[1], is("\tLeptoconchus incycloseris\t\t\t\t\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
            assertThat(actualLines[1].split("\t").length, is(actualLines[0].split("\t").length));
        }
    }

    @Test
    public void namesWithHeaderEmpty() throws URISyntaxException {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("names",
                "--cache-dir=" + CmdTestUtil.cacheDirTestFor("/dataset-cache-empty/globalbioticinteractions/template-dataset/access.tsv"));

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());

        if (actual.getObjects().get(0) instanceof Runnable) {
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(out1);
            ((CmdNames) actual.getObjects().get(0)).run(out);
            String actualOutput = out1.toString();
            String[] actualLines = StringUtils.splitByWholeSeparator(actualOutput, "\n");
            assertThat(actualLines[0], is("taxonId\ttaxonName\ttaxonRank\ttaxonPath\ttaxonPathIds\ttaxonPathNames\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion"));
            assertThat(actualLines[1], is("\tLeptoconchus incycloseris\t\t\t\t\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
            assertThat(actualLines[1].split("\t").length, is(actualLines[0].split("\t").length));
        }
    }


}