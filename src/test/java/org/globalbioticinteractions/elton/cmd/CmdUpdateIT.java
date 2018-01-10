package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class CmdUpdateIT {

    @Before
    public void emptyCache() {
        FileUtils.deleteQuietly(new File("target/tmp-dataset"));
    }

    @Test
    public void update() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("update", "--cache-dir=./bla");
        assertUpdateCmd(jc);

        jc = new CmdLine().buildCommander();
        jc.parse("update", "-c", "./bla");
        assertUpdateCmd(jc);
    }

    @Test
    public void runUpdate() throws IOException {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("update", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/template-dataset");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Assert.assertEquals(actual.getObjects().get(0).getClass(), CmdUpdate.class);

        CmdLine.run(actual);
        
        File file = new File("./target/tmp-dataset/globalbioticinteractions/template-dataset/access.tsv");
        assertThat(file.exists(), is(true));
        String[] jarUrls = FileUtils.readFileToString(file).split("jar:file:");
        assertTrue(jarUrls.length > 1);
        String localJarUrl = jarUrls[1].split("\t")[0];
        assertThat(localJarUrl, not(startsWith("/")));
        assertNotNull(new URL("jar:file:" + localJarUrl).openStream());

        int numberOfLogEntries = getNumberOfLogEntries();
        assertThat(getNumberOfLogEntries() > 3, is(true));
        int numberOfCacheFiles = getNumberOfCacheFiles();

        // rerun
        CmdLine.run(actual);
        assertThat("should update regardless or preexisting entries in cache", getNumberOfLogEntries() + 3 > numberOfLogEntries, is(true));
        assertThat("number of cached files should not have changed after update", numberOfCacheFiles, is(getNumberOfCacheFiles()));
    }

    private int getNumberOfCacheFiles() {
        return FileUtils.listFiles(new File(getDatasetCacheDir()), null, false).size();
    }

    private int getNumberOfLogEntries() throws IOException {
        String accessLog = getDatasetCacheDir() + "access.tsv";
        File accessLogFile = new File(accessLog);
        assertThat(accessLogFile.exists(), is(true));
        return IOUtils.toString(accessLogFile.toURI()).split("\n").length;
    }

    private String getDatasetCacheDir() {
        return "target/tmp-dataset/globalbioticinteractions/template-dataset/";
    }

    @Test
    public void runUpdateHafner() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("update", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/hafner");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Assert.assertEquals(actual.getObjects().get(0).getClass(), CmdUpdate.class);

        CmdLine.run(actual);
    }

    private void assertUpdateCmd(JCommander jc) {
        Assert.assertEquals(jc.getParsedCommand(), "update");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdUpdate.class);
        assertThat(((CmdUpdate) cmd).getCacheDir(), is("./bla"));
    }
}