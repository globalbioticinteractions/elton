package org.globalbioticinteractions.elton.cmd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetWithResourceMapping;
import org.globalbioticinteractions.elton.store.ProvUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.regex.Matcher;

public class DatasetConfigReaderPrestonProv implements DatasetConfigReader {

    private String resourceLocation;
    private String resourceFormat;
    private final ResourceService resourceService;

    public DatasetConfigReaderPrestonProv() {
        this(new ResourceService() {
            @Override
            public InputStream retrieve(URI uri) throws IOException {
                throw new IOException("no resource service available to retrieve [" + uri + "]");
            }
        });
    }


    public DatasetConfigReaderPrestonProv(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public Dataset readConfig(String line) throws IOException {
        Dataset dataset = null;
        if (StringUtils.contains(line, ProvUtil.FORMAT)) {
            Matcher matcher = ProvUtil.FORMAT_PATTERN.matcher(line);
            if (matcher.matches()) {
                setResourceLocation(matcher.group("location"));
                setResourceFormat(matcher.group("format"));
            }
        } else if (StringUtils.contains(line, ProvUtil.HAS_VERSION)) {
            Matcher matcher = ProvUtil.VERSION_PATTERN.matcher(line);
            if (matcher.matches()) {
                String location = matcher.group("location");
                if (StringUtils.equals(getResourceLocation(), location)
                        && StringUtils.equals(getResourceFormat(), "application/dwca")) {
                    String version = matcher.group("version");

                    dataset = new DatasetWithResourceMapping(
                            location,
                            URI.create(location),
                            resourceService
                    );
                    ObjectNode config = new ObjectMapper().createObjectNode();
                    config.put("format", "dwca");
                    config.put("url", getResourceLocation());
                    config.put("resources", new ObjectMapper().createObjectNode()
                            .put(location, version)
                            .put("/eml.xml", "zip:" + version + "!/eml.xml"));
                    dataset.setConfig(config);
                }

            }
        }
        return dataset;
    }

    @Override
    public Dataset datasetForContextOrReset() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceFormat(String resourceFormat) {
        this.resourceFormat = resourceFormat;
    }

    public String getResourceFormat() {
        return resourceFormat;
    }
}
