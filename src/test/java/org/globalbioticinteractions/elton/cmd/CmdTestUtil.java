package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

final class CmdTestUtil {


    static String cacheDirTest(TemporaryFolder tmpFolder) throws URISyntaxException, IOException {
        String name = "/dataset-cache-test/globalbioticinteractions/template-dataset/access.tsv";
        String dataStatic = cacheDirTestFor(name);
        File tmpDir = tmpFolder.newFolder();
        FileUtils.copyDirectory(new File(dataStatic), tmpDir);
        return tmpDir.getAbsolutePath();
    }

    static String cacheDirTestMD5(TemporaryFolder tmpFolder) throws URISyntaxException, IOException {
        String name = "/dataset-cache-test-md5/globalbioticinteractions/template-dataset/access.tsv";
        String dataStatic = cacheDirTestFor(name);
        File tmpDir = tmpFolder.newFolder();
        FileUtils.copyDirectory(new File(dataStatic), tmpDir);
        return tmpDir.getAbsolutePath();
    }

    static String cacheDirTestFor(String name) throws URISyntaxException {
        URL accessURL = CmdNamesTest.class.getResource(name);
        assertThat(accessURL, is(notNullValue()));
        return new File(accessURL.toURI()).getParentFile().getParentFile().getParentFile().getAbsolutePath();
    }

    public static void assertEmpty(File dir) {
        Collection<File> files = FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        assertThat(files.size(), CoreMatchers.is(1));
        assertThat(files.iterator().next().getAbsolutePath(), CoreMatchers.is(dir.getAbsolutePath()));
    }

    public static int numberOfDataFiles(String dataDir) {
        return numberOfDataFiles(new File(dataDir));
    }

    public static int numberOfDataFiles(File dataDir) {
        return FileUtils.listFiles(dataDir, null, true).size();
    }
}
