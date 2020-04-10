package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CmdUpdateTest {

    @Test
    public void update() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("update", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/template-dataset");

        Assert.assertEquals(jc.getParsedCommand(), "sync");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdUpdate.class);
        CmdUpdate cmdUpdate = (CmdUpdate) actual.getObjects().get(0);

        assertThat(cmdUpdate.getNamespaces().size(), is(1));
        assertThat(cmdUpdate.getNamespaces(), hasItem("globalbioticinteractions/template-dataset"));
    }

    @Test
    public void supportedRegistries() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("update", "--registries", "zenodo,github", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/template-dataset");
        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdUpdate.class);
        CmdUpdate cmdUpdate = (CmdUpdate) actual.getObjects().get(0);
        assertThat(cmdUpdate.getRegistryNames(), hasItem("zenodo"));
        assertThat(cmdUpdate.getRegistryNames(), hasItem("github"));
    }

    @Test
    public void updateLocalDataset() throws IOException, URISyntaxException {
        Path tempDirectory = Files.createTempDirectory(
                Paths.get("target"), "test-dataset");

        JCommander jc = new CmdLine().buildCommander();
        URL localDataset = getClass().getResource("/dataset-local-test/globi.json");

        File localWorkDir = new File(localDataset.toURI()).getParentFile();

        jc.parse("update",
                "--registries", "local",
                "--cache-dir=" + tempDirectory.toFile().getAbsolutePath(),
                "--work-dir=" + localWorkDir.getAbsolutePath(),
                "local"
        );
        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdUpdate.class);
        CmdUpdate cmdUpdate = (CmdUpdate) actual.getObjects().get(0);
        List<String> registryNames = cmdUpdate.getRegistryNames();
        assertThat(registryNames.size(), is(1));
        assertThat(registryNames, hasItem("local"));

        cmdUpdate.run();

        File local = new File(tempDirectory.toFile(), "local");
        assertThat(local.exists(), is(true));

        File provenanceLog = new File(local, "access.tsv");
        assertThat(provenanceLog.exists(), is(true));

        String provenanceLogString = IOUtils.toString(provenanceLog.toURI(), StandardCharsets.UTF_8);

        String[] lines = StringUtils.split(provenanceLogString, "\n");

        assertThat(lines[0], startsWith("local\tfile://" + localWorkDir.getAbsolutePath() + "/\t\t"));
        assertThat(lines[0], endsWith("application/globi"));
        assertThat(lines[1], startsWith("local\tfile://" + new File(localWorkDir, "globi.json").getAbsolutePath() + "\t121a46e829f692336bb2038348eb2c174595023a8bc4ed638319d7329b7ff82a\t"));
        assertThat(lines[4], startsWith("local\tfile://" + new File(localWorkDir, "interactions.tsv").getAbsolutePath() + "\td639552071756cb98372f92c6f1eef2c99f4e25b469be56c24fab172fb454bd9\t"));
    }


}