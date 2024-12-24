package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CmdDatasetsTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void interactionsNoHeader() throws URISyntaxException, IOException {
        CmdDatasets cmd = new CmdDatasets();
        cmd.setDataDir(CmdTestUtil.cacheDirTest(tmpFolder));
        cmd.setProvDir(CmdTestUtil.cacheDirTest(tmpFolder));
        cmd.setSkipHeader(true);
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        assertThat(out1.toString().split("\n")[0],
                is("globalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
    }

    @Test
    public void interactionsWithHeader() throws URISyntaxException, IOException {
        CmdDatasets cmd = new CmdDatasets();
        cmd.setDataDir(CmdTestUtil.cacheDirTest(tmpFolder));
        cmd.setProvDir(CmdTestUtil.cacheDirTest(tmpFolder));

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        String actual1 = out1.toString();
        String[] lines = StringUtils.splitByWholeSeparator(actual1, "\n");
        assertThat(lines[0], is("namespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion"));
        assertThat(lines[1], is("globalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
        assertThat(lines[0].split("\t").length, is(lines[1].split("\t").length));
    }


}