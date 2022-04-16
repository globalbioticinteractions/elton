package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CmdUpdateIT {

    @Rule
    private TemporaryFolder tmpDir = new TemporaryFolder();


    @Test
    public void runUpdate() throws IOException {

        CmdUpdate cmd = new CmdUpdate();
        File file1 = tmpDir.newFolder();
        String absolutePath = file1.getAbsolutePath();
        cmd.setCacheDir(absolutePath);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));

        cmd.run();

        File file = new File(file1,"globalbioticinteractions/template-dataset/access.tsv");
        assertThat(file.exists(), is(true));
        String[] jarUrls = FileUtils.readFileToString(file, StandardCharsets.UTF_8).split("jar:file:");
        assertTrue(jarUrls.length > 1);
        String localJarUrl = jarUrls[1].split("\t")[0];
        assertNotNull(new URL("jar:file:" + localJarUrl).openStream());

        int numberOfLogEntries = getNumberOfLogEntries();
        assertThat(getNumberOfLogEntries() > 3, is(true));
        int numberOfCacheFiles = getNumberOfCacheFiles();

        // rerun
        cmd.run();
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
        return IOUtils.toString(accessLogFile.toURI(), StandardCharsets.UTF_8).split("\n").length;
    }

    private String getDatasetCacheDir() {
        return "target/tmp-dataset/globalbioticinteractions/template-dataset/";
    }

}