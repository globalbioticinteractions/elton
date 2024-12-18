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
import java.util.Collection;
import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CmdUpdateIT {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void runDwCA() throws IOException {
        assertAccessLogForNamespace("seltmann/taxonomy-darwin-core");
    }

    @Test
    public void updateKnowledgePixels() throws IOException {
        assertAccessLogForNamespace("globalbioticinteractions/knowledgepixels");
    }

    @Test
    public void runUpdate() throws IOException {
        assertAccessLogForNamespace("globalbioticinteractions/template-dataset");
    }

    @Test
    public void runUpdateNonExistingDataProvFolders() throws IOException {
        CmdUpdate cmd = new CmdUpdate();
        File dataProvFolder = tmpDir.newFolder();
        dataProvFolder.delete();
        doUpdate("globalbioticinteractions/template-dataset", cmd, dataProvFolder, tmpDir.newFolder("workdir"));
    }

    private void assertAccessLogForNamespace(String namespace) throws IOException {
        CmdUpdate cmd = new CmdUpdate();
        File dataAndProvFolder = tmpDir.newFolder();
        doUpdate(namespace, cmd, dataAndProvFolder, tmpDir.newFolder("workdir"));
    }

    private void doUpdate(String namespace, CmdUpdate cmd, File dataAndProvFolder, File workdir) throws IOException {
        String absolutePath = dataAndProvFolder.getAbsolutePath();
        cmd.setDataDir(absolutePath);
        cmd.setProvDir(absolutePath);
        cmd.setWorkDir(workdir.getAbsolutePath());
        cmd.setNamespaces(Collections.singletonList(namespace));

        File workDir = new File(cmd.getWorkDir());
        CmdTestUtil.assertEmpty(workDir);

        cmd.run();

        File datasetDir = assertAccessLog(namespace, dataAndProvFolder);

        int numberOfLogEntries = getNumberOfLogEntries(datasetDir);
        assertThat(getNumberOfLogEntries(datasetDir) > 3, is(true));
        int numberOfCacheFiles = getNumberOfCacheFiles(datasetDir);
        assertTmpFilesRemoved(dataAndProvFolder);

        CmdTestUtil.assertEmpty(workDir);


        // rerun
        cmd.run();
        assertThat("should update regardless or preexisting entries in cache", getNumberOfLogEntries(datasetDir) + 3 > numberOfLogEntries, is(true));
        assertThat("number of cached files should not have changed after update", numberOfCacheFiles, is(getNumberOfCacheFiles(datasetDir)));
    }

    private void assertTmpFilesRemoved(File dataAndProvFolder) {
        Collection<File> files = FileUtils.listFiles(dataAndProvFolder, null, false);
        assertThat(files.size(), is(0));
    }

    private File assertAccessLog(String namespace, File file1) throws IOException {
        File datasetDir = new File(file1, namespace);
        File file = new File(datasetDir,"access.tsv");
        assertThat(file.exists(), is(true));
        String[] jarUrls = FileUtils.readFileToString(file, StandardCharsets.UTF_8).split("jar:file:");
        assertTrue(jarUrls.length > 1);
        String localJarUrl = jarUrls[1].split("\t")[0];
        assertNotNull(new URL("jar:file:" + localJarUrl).openStream());
        return datasetDir;
    }


    private int getNumberOfCacheFiles(File datasetCacheDir) {
        return FileUtils.listFiles(datasetCacheDir, null, false).size();
    }

    private int getNumberOfLogEntries(File dir) throws IOException {
        File accessLogFile = new File(dir,"access.tsv");
        assertThat(accessLogFile.exists(), is(true));
        return IOUtils.toString(accessLogFile.toURI(), StandardCharsets.UTF_8).split("\n").length;
    }

    private String getDatasetCacheDir() {
        return tmpDir + "/globalbioticinteractions/template-dataset/";
    }



}