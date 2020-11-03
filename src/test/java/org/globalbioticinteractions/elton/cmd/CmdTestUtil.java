package org.globalbioticinteractions.elton.cmd;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

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
}
