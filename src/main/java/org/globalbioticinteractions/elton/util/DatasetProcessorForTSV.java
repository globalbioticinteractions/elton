package org.globalbioticinteractions.elton.util;

import org.codehaus.jackson.JsonNode;
import org.eol.globi.service.Dataset;
import org.globalbioticinteractions.doi.DOI;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class DatasetProcessorForTSV implements DatasetProcessor {
    @Override
    public Dataset process(final Dataset dataset) {
        return new Dataset() {

            @Override
            public InputStream getResource(String resourceName) throws IOException {
                return dataset.getResource(resourceName);
            }

            @Override
            public URI getResourceURI(String resourceName) {
                return dataset.getResourceURI(resourceName);
            }

            @Override
            public URI getArchiveURI() {
                return dataset.getArchiveURI();
            }

            @Override
            public String getNamespace() {
                return dataset.getNamespace();
            }

            @Override
            public JsonNode getConfig() {
                return dataset.getConfig();
            }

            @Override
            public String getCitation() {
                return dataset.getCitation();
            }

            @Override
            public String getFormat() {
                return dataset.getFormat();
            }

            @Override
            public String getOrDefault(String key, String defaultValue) {
                return null;
            }

            @Override
            public DOI getDOI() {
                return dataset.getDOI();
            }

            @Override
            public URI getConfigURI() {
                return null;
            }

            @Override
            public void setConfig(JsonNode config) {

            }

            @Override
            public void setConfigURI(URI configURI) {

            }
        };
    }
}
