package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
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
        File tmpWorkDir = createTmpWorkDir(localWorkDir);

        CmdUpdate cmd = new CmdUpdate();
        File file = tmpDir.newFolder();
        assertUpdate(tmpWorkDir, cmd, file.getAbsolutePath(), file.getAbsolutePath());
    }

    @Test
    public void updateLocalDatasetSeparateDataProvDirs() throws IOException, URISyntaxException {
        URL localDataset = getClass().getResource("/dataset-local-test/globi.json");
        File localWorkDir = new File(localDataset.toURI()).getParentFile();
        File tmpWorkDir = createTmpWorkDir(localWorkDir);

        CmdUpdate cmd = new CmdUpdate();
        String dataDir = tmpDir.newFolder("data").getAbsolutePath();
        String provDir = tmpDir.newFolder("prov").getAbsolutePath();
        assertUpdate(tmpWorkDir, cmd, dataDir, provDir);
    }

    private File createTmpWorkDir(File localWorkDir) throws IOException {
        File tmpWorkDir = tmpDir.newFolder("workdir");
        FileUtils.copyFileToDirectory(new File(localWorkDir, "globi.json"), tmpWorkDir);
        FileUtils.copyFileToDirectory(new File(localWorkDir, "interactions.tsv"), tmpWorkDir);
        return tmpWorkDir;
    }

    @Test
    public void updateNonExistentDirectoriesLocalDataset() throws IOException, URISyntaxException {

        URL localDataset = getClass().getResource("/dataset-local-test/globi.json");

        File localWorkDir = new File(localDataset.toURI()).getParentFile();
        File tmpWorkDir = createTmpWorkDir(localWorkDir);

        File file = tmpDir.newFolder();
        file.delete();

        CmdUpdate cmd = new CmdUpdate();
        assertUpdate(tmpWorkDir, cmd, file.getAbsolutePath(), file.getAbsolutePath());
    }

    private void assertUpdate(File localWorkDir, CmdUpdate cmd, String dataDir, String provDir) throws IOException {
        cmd.setDataDir(dataDir);
        cmd.setProvDir(provDir);
        cmd.setWorkDir(localWorkDir.getAbsolutePath());
        cmd.setRegistryNames(Arrays.asList(DatasetRegistryUtil.NAMESPACE_LOCAL));
        cmd.setNamespaces(Collections.singletonList(DatasetRegistryUtil.NAMESPACE_LOCAL));

        Collection<File> files = FileUtils.listFilesAndDirs(
                localWorkDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        assertThat(files.size(), CoreMatchers.is(3));

        cmd.run();

        files = FileUtils.listFilesAndDirs(
                localWorkDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        assertThat(files.size(), CoreMatchers.is(3));

        File localDataDir = new File(dataDir, DatasetRegistryUtil.NAMESPACE_LOCAL);
        assertThat(localDataDir.exists(), is(true));

        File localProvDir = new File(provDir, DatasetRegistryUtil.NAMESPACE_LOCAL);
        assertThat(localProvDir.exists(), is(true));

        if (!localDataDir.equals(localProvDir)) {
            assertThat(new File(localDataDir, "access.tsv").exists(), is(false));
        }

        File provenanceLog = new File(localProvDir, "access.tsv");
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