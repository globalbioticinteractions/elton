package org.globalbioticinteractions.elton.cmd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetWithResourceMapping;
import org.globalbioticinteractions.elton.store.ProvUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DatasetConfigReaderPrestonProv implements DatasetConfigReader {

    public static final String DWCA_META_FILENAME = "meta.xml";
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
                    String metaPath = getDwCArchiveMetaPathIfAvailable(version, resourceService);
                    String metaPathContentId = "zip:" + version + "!/" + metaPath;

                    if (StringUtils.isNotBlank(metaPath)) {
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        DocumentBuilder builder;
                        try {
                            builder = factory.newDocumentBuilder();
                            Document doc = builder.parse(resourceService.retrieve(URI.create(metaPathContentId)));
                            if (doc != null) {
                                String emlPath = doc.getDocumentElement().getAttribute("metadata");
                                String emlContentPath = StringUtils.replace(metaPath, "meta.xml", StringUtils.isBlank(emlPath) ? "eml.xml" : emlPath);
                                dataset = createConfigForDwCA(location, version, emlContentPath);
                            }

                        } catch (ParserConfigurationException | SAXException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            }
        }
        return dataset;
    }

    private Dataset createConfigForDwCA(String location, String version, String metaPath) {
        Dataset dataset;
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
                .put("/eml.xml", "zip:" + version + "!/" + metaPath));
        dataset.setConfig(config);
        return dataset;
    }

    public static String getDwCArchiveMetaPathIfAvailable(String contentId, ResourceService resourceService) throws IOException {
        String metaPath = null;
        try (InputStream retrieve = resourceService.retrieve(URI.create(contentId))) {
            if (retrieve != null) {
                ZipInputStream zipInputStream = new ZipInputStream(retrieve);
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (StringUtils.equals(entry.getName(), DWCA_META_FILENAME)
                            || StringUtils.endsWith(entry.getName(), "/" + DWCA_META_FILENAME)) {
                        metaPath = StringUtils.isBlank(metaPath)
                                || (StringUtils.length(entry.getName()) < StringUtils.length(metaPath))
                                ? entry.getName()
                                : metaPath;
                    }
                    IOUtils.copy(zipInputStream, NullOutputStream.INSTANCE);
                }
            }
        }
        return metaPath;
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
