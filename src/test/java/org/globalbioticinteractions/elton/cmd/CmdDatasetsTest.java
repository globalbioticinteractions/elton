package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class CmdDatasetsTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void interactionsNoHeader() throws URISyntaxException, IOException {
        CmdDatasets cmd = getCmdDatasets();
        cmd.setSkipHeader(true);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));
        cmd.run(out);
        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));
        assertThat(out1.toString().split("\n")[0],
                is("globalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
    }

    private CmdDatasets getCmdDatasets() throws URISyntaxException, IOException {
        CmdDatasets cmd = new CmdDatasets();
        String dataDir = CmdTestUtil.cacheDirTest(tmpFolder);
        cmd.setDataDir(dataDir);
        cmd.setProvDir(CmdTestUtil.cacheDirTest(tmpFolder));
        return cmd;
    }

    @Test
    public void interactionsWithHeader() throws URISyntaxException, IOException {
        CmdDatasets cmd = getCmdDatasets();

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));
        cmd.run(out);
        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));

        String actual1 = out1.toString();
        String[] lines = StringUtils.splitByWholeSeparator(actual1, "\n");
        assertThat(lines[0], is("namespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion"));
        assertThat(lines[1], is("globalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
        assertThat(lines[0].split("\t").length, is(lines[1].split("\t").length));
    }

    @Test
    public void interactionsWithHeaderWithProv() throws URISyntaxException, IOException {
        CmdDatasets cmd = getCmdDatasets();
        cmd.setEnableProvMode(true);

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);
        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));
        cmd.run();
        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(5));

        String actual1 = out1.toString();
        String[] lines = StringUtils.splitByWholeSeparator(actual1, "\n");
        assertThat(lines[0], is(not("namespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion")));
        assertThat(lines[0], startsWith("<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent>"));
        assertThat(lines[lines.length - 2], containsString("<http://www.w3.org/ns/prov#endedAtTime>"));
    }


}