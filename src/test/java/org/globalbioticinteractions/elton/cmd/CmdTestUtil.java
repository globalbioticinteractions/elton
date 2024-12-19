package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.hamcrest.CoreMatchers;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

final class CmdTestUtil {
    static String cacheDirTest() throws URISyntaxException {
        String name = "/dataset-cache-test/globalbioticinteractions/template-dataset/access.tsv";
        return cacheDirTestFor(name);
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
}
