package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CypherLoaderForNamespaceTest {

    @Test
    public void cypherScripts() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new CypherLoaderForNamespace(new PrintStream(outputStream))
                .onNamespace("globalbioticinteractions/template-dataset");

        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8),
                is(IOUtils.toString(getClass().getResourceAsStream("load-templatedataset.cypher"), StandardCharsets.UTF_8)));
    }

}