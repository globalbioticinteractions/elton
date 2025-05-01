package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.globalbioticinteractions.elton.util.NamespaceHandler;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class CypherLoaderForNamespace implements NamespaceHandler {
    private static final String ENDPOINT_PREFIX = "https://depot.globalbioticinteractions.org/reviews/";

    private final PrintStream out;

    public CypherLoaderForNamespace(PrintStream out) {
        this.out = out;
    }

    @Override
    public void onNamespace(String namespace) throws Exception {
        String loadScript = IOUtils.toString(getClass().getResourceAsStream("/org/globalbioticinteractions/elton/template/load.cypher"), StandardCharsets.UTF_8);
        String renderedTemplate = StringUtils.replace(loadScript, "{{ ENDPOINT }}", getEndpoint(namespace));
        IOUtils.copy(IOUtils.toInputStream(renderedTemplate, StandardCharsets.UTF_8), this.out);
    }

    private static String getEndpoint(String namespace) {
        return ENDPOINT_PREFIX + namespace;
    }

}
