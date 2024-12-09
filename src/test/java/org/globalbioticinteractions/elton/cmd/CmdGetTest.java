package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullPrintStream;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;


public class CmdGetTest {

    @Test
    public void retrieveByContentId() throws URISyntaxException, IOException {
        URL resource = getClass().getResource("/dataset-cache-test/globalbioticinteractions/template-dataset/access.tsv");
        File file = new File(resource.toURI());
        File cacheDir = file.getParentFile().getParentFile().getParentFile();
        assertThat(cacheDir.getName(), Is.is("dataset-cache-test"));

        CmdGet get = new CmdGet();

        get.setCacheDir(cacheDir.getAbsolutePath());
        get.setStdin(IOUtils.toInputStream("<bla> <http://purl.org/pav/hasVersion> <hash://sha256/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80> .", StandardCharsets.UTF_8));
        get.setStdout(NullPrintStream.INSTANCE);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        get.run(new PrintStream(outputStream));

        InputStream expected = getClass().getResourceAsStream("/dataset-cache-test/globalbioticinteractions/template-dataset/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80");
        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), Is.is(
                IOUtils.toString(expected, StandardCharsets.UTF_8)));

    }

}