package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CmdNamesTest {

    @Test
    public void namesNoHeader() throws URISyntaxException {
        CmdNames cmd = new CmdNames();
        cmd.setDataDir(CmdTestUtil.cacheDirTest());
        cmd.setProvDir(CmdTestUtil.cacheDirTest());
        cmd.setSkipHeader(true);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        assertThat(out1.toString(), startsWith("\tLeptoconchus incycloseris"));
        assertThat(out1.toString().split("\n").length, is(22));
    }

    @Test
    public void namesWithHeader() throws URISyntaxException {
        CmdNames cmdNames = new CmdNames();
        cmdNames.setDataDir(CmdTestUtil.cacheDirTest());
        cmdNames.setProvDir(CmdTestUtil.cacheDirTest());
        cmdNames.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmdNames.run(out);
        String actualOutput = out1.toString();
        String[] actualLines = StringUtils.splitByWholeSeparator(actualOutput, "\n");
        assertThat(actualLines[0], is("taxonId\ttaxonName\ttaxonRank\ttaxonPathIds\ttaxonPath\ttaxonPathNames\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion"));
        assertThat(actualLines[1], is("\tLeptoconchus incycloseris\t\t\t\t\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
        assertThat(actualLines[1].split("\t").length, is(actualLines[0].split("\t").length));
    }

    @Test
    public void namesWithHeaderEmpty() throws URISyntaxException {
        CmdNames cmd = new CmdNames();
        String cacheDir = CmdTestUtil.cacheDirTestFor("/dataset-cache-empty/globalbioticinteractions/template-dataset/access.tsv");
        cmd.setDataDir(cacheDir);
        cmd.setProvDir(cacheDir);

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        String actualOutput = out1.toString();
        String[] actualLines = StringUtils.splitByWholeSeparator(actualOutput, "\n");
        assertThat(actualLines[0], is("taxonId\ttaxonName\ttaxonRank\ttaxonPathIds\ttaxonPath\ttaxonPathNames\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion"));
        assertThat(actualLines[1], is("\tLeptoconchus incycloseris\t\t\t\t\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
        assertThat(actualLines[1].split("\t").length, is(actualLines[0].split("\t").length));
    }

}