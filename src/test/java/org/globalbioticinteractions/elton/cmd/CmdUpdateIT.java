package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public void runUpdateUCSB() throws IOException {
        assertAccessLogForNamespace("globalbioticinteractions/ucsb-izc");
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
        assertThat(numberOfLogEntries, is(2));
        int numberOfCacheFiles = getNumberOfCacheFiles(datasetDir);
        assertTmpFilesRemoved(dataAndProvFolder);

        CmdTestUtil.assertEmpty(workDir);


        // rerun
        cmd.run();
        assertThat("should update regardless or preexisting entries in cache", getNumberOfLogEntries(datasetDir), is(numberOfLogEntries * 2));
        assertThat("number of cached files should not have changed after update", numberOfCacheFiles, is(getNumberOfCacheFiles(datasetDir)));
    }

    private void assertTmpFilesRemoved(File dataAndProvFolder) {
        Collection<File> files = FileUtils.listFiles(dataAndProvFolder, null, false);
        assertThat(files.size(), is(0));
    }

    private File assertAccessLog(String namespace, File dataAndProvFolder) throws IOException {
        File datasetDir = new File(dataAndProvFolder, namespace);
        File file = new File(datasetDir,"access.tsv");
        assertThat(file.exists(), is(true));
        String[] lines = FileUtils.readFileToString(file, StandardCharsets.UTF_8).split("\n");
        assertTrue(lines.length > 1);
        List<String> availableHashes = new ArrayList<>();
        Set<String> availableUniqueHashes = new HashSet<>();
        for (String line : lines) {
            String hash = line.split("\t")[2];
            if (StringUtils.isNotBlank(hash)) {
                File resourceFile = new File(datasetDir, hash);
                if (resourceFile.exists()) {
                    availableHashes.add(hash);
                    availableUniqueHashes.add(hash);
                }
            }
        }
        assertThat(availableUniqueHashes.size(), is(1));
        assertThat(availableHashes.size(), is(1));
        return datasetDir;
    }


    private int getNumberOfCacheFiles(File datasetCacheDir) {
        return FileUtils.listFiles(datasetCacheDir, null, false).size();
    }

    private int getNumberOfLogEntries(File dir) throws IOException {
        File accessLogFile = new File(dir,"access.tsv");
        assertThat(accessLogFile.exists(), is(true));
        return IOUtils.toString(accessLogFile.toURI(), StandardCharsets.UTF_8)
                .split("\n").length;
    }

}