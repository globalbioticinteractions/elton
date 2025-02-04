package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetWithResourceMapping;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatasetConfigReaderProv implements DatasetConfigReader {
    private static final String ASSOCIATED_WITH = " <http://www.w3.org/ns/prov#wasAssociatedWith> ";
    private static final String FORMAT = " <http://purl.org/dc/elements/1.1/format> ";
    private static final String HAS_VERSION = " <http://purl.org/pav/hasVersion> ";
    private static final String URN_LSID_GLOBALBIOTICINTERACTIONS_ORG = "urn:lsid:globalbioticinteractions.org:";
    public static final String ENDED_AT_TIME = " <http://www.w3.org/ns/prov#endedAtTime> ";
    private final ResourceService resourceService;

    private IRI resourceActivityContext = null;
    private IRI resourceLocation = null;
    private IRI resourceNamespace = null;
    private String resourceFormat = null;
    private IRI resourceVersion = null;
    private boolean contextComplete = false;
    private TreeMap<URI, IRI> contextDeps = new TreeMap<>();

    public DatasetConfigReaderProv() {
        this(new ResourceService() {
            @Override
            public InputStream retrieve(URI uri) throws IOException {
                throw new IOException("no resource service available to retrieve [" + uri + "]");
            }
        });
    }

    public DatasetConfigReaderProv(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public Dataset readConfig(String line) throws IOException {
        Dataset dataset = null;
        if (StringUtils.contains(line, ASSOCIATED_WITH)) {
            dataset = handleAssociation(line, dataset);
        } else if (StringUtils.contains(line, FORMAT)) {
            handleFormat(line);
        } else if (StringUtils.contains(line, HAS_VERSION)) {
            handleVersion(line);
        } else if (StringUtils.contains(line, ENDED_AT_TIME)) {
            handleEnded(line);
        }

        return dataset == null
                ? createDatasetIfComplete(dataset)
                : dataset;
    }

    private void handleEnded(String line) {
        Pattern namespacePattern = Pattern.compile("<(?<activity>[^>]+)>" + ENDED_AT_TIME + ".*");
        Matcher matcher = namespacePattern.matcher(line);
        if (matcher.matches()) {
            contextComplete = inSameActivity(matcher);
        }
    }

    private void handleVersion(String line) {
        // possible version statement
        Pattern namespacePattern = Pattern.compile("<(?<location>[^>]+)>" + HAS_VERSION + "<(?<version>[^>]+)> <(?<activity>[^>]+)> [.]");
        Matcher matcher = namespacePattern.matcher(line);
        if (matcher.matches()) {
            String location = matcher.group("location");
            if (contextSupported()) {
                if (inSameActivity(matcher)) {
                    IRI version = setVersionOnMatchingLocation(matcher, location);
                    if (contextSupported()) {
                        contextDeps.put(URI.create(location), version);
                    }
                }
            } else {
                setVersionOnMatchingLocation(matcher, location);
            }

        }
    }

    private void handleFormat(String line) {
        Pattern namespacePattern = Pattern.compile("<(?<location>[^>]+)>" + FORMAT + "\"(?<format>[^\"]+)\".*");
        Matcher matcher = namespacePattern.matcher(line);
        if (matcher.matches()) {
            String location = matcher.group("location");
            if (resourceLocation == null
                    || !StringUtils.equals(location, resourceLocation.getIRIString())) {
                if (resourceNamespace == null) {
                    resetContext();
                    resourceLocation = RefNodeFactory.toIRI(location);
                    resourceNamespace = RefNodeFactory.toIRI(URN_LSID_GLOBALBIOTICINTERACTIONS_ORG + "local");
                }
            }
            resourceFormat = matcher.group("format");
        }
        // possible format statement
    }

    private Dataset handleAssociation(String line, Dataset dataset) {
        // possible namespace statement
        Pattern namespacePattern = Pattern.compile("<(?<namespace>" + URN_LSID_GLOBALBIOTICINTERACTIONS_ORG + "[^>]+)>" + ASSOCIATED_WITH + "<(?<location>[^>]+)> <(?<activity>[^>]+)> [.]");
        Matcher matcher = namespacePattern.matcher(line);
        if (matcher.matches()) {
            dataset = attemptToCreateDataset(dataset);
            String location = matcher.group("location");
            resourceLocation = RefNodeFactory.toIRI(location);
            resourceNamespace = RefNodeFactory.toIRI(matcher.group("namespace"));
            resourceFormat = "application/globi";
            resourceActivityContext = RefNodeFactory.toIRI(matcher.group("activity"));
            contextComplete = false;
        }
        return dataset;
    }

    private IRI setVersionOnMatchingLocation(Matcher matcher, String location) {
        IRI version = RefNodeFactory.toIRI(matcher.group("version"));
        if (resourceLocation != null && StringUtils.equals(location, resourceLocation.getIRIString())) {
            resourceVersion = version;
        }
        return version;
    }

    private boolean inSameActivity(Matcher matcher) {
        String activity = matcher.group("activity");
        return inSameActivity(activity);
    }

    private boolean inSameActivity(String activity) {
        boolean sameActivity = false;
        if (resourceActivityContext != null && StringUtils.equals(activity, resourceActivityContext.getIRIString())) {
            sameActivity = true;
        }
        return sameActivity;
    }

    private Dataset attemptToCreateDataset(Dataset dataset) {
        if (contextSupported()) {
            dataset = createDataset();
        } else {
            resetContext();
        }
        return dataset;
    }

    private Dataset createDatasetIfComplete(Dataset dataset) {
        if (resourceLocation != null
                && resourceNamespace != null
                && resourceFormat != null
                && resourceVersion != null) {
            if (contextSupported()) {
                if (contextComplete) {
                    dataset = createDataset();
                }
            } else {
                dataset = createDataset();
            }
        }
        return dataset;
    }

    private boolean contextSupported() {
        return Arrays.asList("application/globi", "application/dwca").contains(resourceFormat);
    }

    private Dataset createDataset() {
        String resourceNamespaceString = resourceNamespace.getIRIString();
        String namespace = StringUtils.removeStart(resourceNamespaceString, URN_LSID_GLOBALBIOTICINTERACTIONS_ORG);

        Dataset dataset = new DatasetWithResourceMapping(namespace, URI.create(resourceVersion.getIRIString()), resourceService);
        ObjectNode config = new ObjectMapper().createObjectNode();
        ObjectNode resourceVersions = new ObjectMapper().createObjectNode();
        contextDeps.forEach((location, version) -> {
            resourceVersions.put(location.toString(), version.getIRIString());
        });
        config.set("resources", resourceVersions);
        dataset.setConfig(config);

        resetContext();
        return dataset;
    }

    private void resetContext() {
        resourceLocation = null;
        resourceFormat = null;
        resourceVersion = null;
        resourceNamespace = null;
        resourceActivityContext = null;
        contextComplete = false;
        contextDeps.clear();
    }
}
