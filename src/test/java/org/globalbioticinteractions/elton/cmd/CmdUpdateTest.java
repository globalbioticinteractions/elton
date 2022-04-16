package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CmdUpdateTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void updateLocalDataset() throws IOException, URISyntaxException {

        URL localDataset = getClass().getResource("/dataset-local-test/globi.json");

        File localWorkDir = new File(localDataset.toURI()).getParentFile();


        CmdUpdate cmd = new CmdUpdate();
        File file = tmpDir.newFolder();
        cmd.setCacheDir(file.getAbsolutePath());
        cmd.setWorkDir(localWorkDir.getAbsolutePath());
        cmd.setRegistryNames(Arrays.asList("local"));
        cmd.setNamespaces(Collections.singletonList("local"));

        cmd.run();

        File local = new File(file, "local");
        assertThat(local.exists(), is(true));

        File provenanceLog = new File(local, "access.tsv");
        assertThat(provenanceLog.exists(), is(true));

        String provenanceLogString = IOUtils.toString(provenanceLog.toURI(), StandardCharsets.UTF_8);

        String[] lines = StringUtils.split(provenanceLogString, "\n");

        assertThat(lines.length, is(3));

        assertThat(lines[0], startsWith("local\tfile://" + localWorkDir.getAbsolutePath() + "/\t\t"));
        assertThat(lines[0], endsWith("application/globi"));
        assertThat(lines[1], startsWith("local\tfile://" + new File(localWorkDir, "globi.json").getAbsolutePath() + "\t121a46e829f692336bb2038348eb2c174595023a8bc4ed638319d7329b7ff82a\t"));
        assertThat(lines[2], startsWith("local\tfile://" + new File(localWorkDir, "interactions.tsv").getAbsolutePath() + "\td639552071756cb98372f92c6f1eef2c99f4e25b469be56c24fab172fb454bd9\t"));
    }


}