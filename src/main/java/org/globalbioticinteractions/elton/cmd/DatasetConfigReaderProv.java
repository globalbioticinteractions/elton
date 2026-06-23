package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.store.HashKeyUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetWithResourceMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bio.guoda.preston.RefNodeConstants.HAS_FORMAT;
import static org.globalbioticinteractions.elton.store.ProvUtil.URN_LSID_GLOBALBIOTICINTERACTIONS_ORG;

public class DatasetConfigReaderProv implements DatasetConfigReader, Closeable {
    private final static Logger LOG = LoggerFactory.getLogger(DatasetConfigReaderProv.class);
    private static final String ASSOCIATED_WITH = " " + RefNodeConstants.WAS_ASSOCIATED_WITH + " ";
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("<(?<namespace>" + "urn:lsid:" + "[^>]+)>" + ASSOCIATED_WITH + "<(?<location>[^>]+)> <(?<activity>[^>]+)> [.]");
    private static final String FORMAT = " " + HAS_FORMAT + " ";
    private static final Pattern FORMAT_PATTERN = Pattern.compile("<(?<location>[^>]+)>" + FORMAT + "\"(?<format>[^\"]+)\".*");
    private static final String HAS_VERSION = " " + RefNodeConstants.HAS_VERSION + " ";
    private static final Pattern VERSION_PATTERN = Pattern.compile("<(?<location>[^>]+)>" + HAS_VERSION + "<(?<version>[^>]+)> <(?<activity>[^>]+)> [.]");
    private static final String ENDED_AT_TIME = " " + RefNodeConstants.ENDED_AT_TIME + " ";
    private static final Pattern ENDED_AT_PATTERN = Pattern.compile("<(?<activity>[^>]+)>" + ENDED_AT_TIME + ".*");

    private static final String WAS_INFORMED_BY = " " + RefNodeConstants.WAS_INFORMED_BY + " ";
    private static final Pattern INFORMED_BY_PATTERN = Pattern.compile("<(?<activity>[^>]+)>" + WAS_INFORMED_BY + "<(?<parentActivity>[^>]+)>" + ".*");

    public static final String APPLICATION_GLOBI = "application/globi";
    public static final List<String> SUPPORTED_FORMATS = Arrays.asList(APPLICATION_GLOBI);

    private final ResourceService resourceService;

    private IRI resourceActivityContext = null;
    private IRI resourceLocation = null;
    private IRI resourceNamespace = null;
    private String resourceFormat = null;
    private IRI resourceVersion = null;
    private boolean contextComplete = false;
    private TreeMap<URI, IRI> contextDeps = new TreeMap<>();
    private Map<String, String> activityRelations = new TreeMap<>();
    private Pair<String, String> lastAddedActivityRelation;
    private final String resourceFormatDefault;
    private final IRI resourceNamespaceDefault;

    public DatasetConfigReaderProv() {
        this(new ResourceService() {
            @Override
            public InputStream retrieve(URI uri) throws IOException {
                throw new IOException("no resource service available to retrieve [" + uri + "]");
            }
        });
    }

    public DatasetConfigReaderProv(ResourceService resourceService) {
        this(resourceService, null, null);
    }

    public DatasetConfigReaderProv(ResourceService resourceService,
                                   String resourceFormatDefault,
                                   IRI resourceNamespaceDefault) {
        this.resourceService = resourceService;
        this.resourceFormatDefault = resourceFormatDefault;
        this.resourceNamespaceDefault = resourceNamespaceDefault;
        resetContext();
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
        } else if (StringUtils.contains(line, WAS_INFORMED_BY)) {
            Matcher matcher = INFORMED_BY_PATTERN.matcher(line);
            if (matcher.matches()) {
                String activity = matcher.group("activity");
                String parentActivity = matcher.group("parentActivity");
                lastAddedActivityRelation = Pair.of(activity, parentActivity);
                activityRelations.put(activity, parentActivity);

            }
        }

        return dataset == null
                ? createDatasetIfComplete(dataset)
                : dataset;
    }

    private void handleEnded(String line) {
        Matcher matcher = ENDED_AT_PATTERN.matcher(line);
        if (matcher.matches()) {
            contextComplete = inSameActivity(matcher);
        }
    }

    private void handleVersion(String line) {
        Matcher matcher = VERSION_PATTERN.matcher(line);
        if (matcher.matches()) {
            String location = matcher.group("location");
            if (explicitContextSupported()) {
                if (inActiveNamespaceContext(matcher)) {
                    IRI version = setVersionOnMatchingLocation(matcher, location);
                    if (explicitContextSupported()) {
                        addResourceDependencies(location, version);
                    }
                }
            } else {
                setVersionOnMatchingLocation(matcher, location);
            }
        }
    }

    private void addResourceDependencies(String location, IRI version) {
        if (!isCompositeHashIRI(RefNodeFactory.toIRI(location))) {
            contextDeps.put(URI.create(location), version);
            if (StringUtils.startsWith(location, resourceLocation.getIRIString())) {
                String relativeLocation = StringUtils.removeStart(location, resourceLocation.getIRIString());
                contextDeps.put(URI.create("/" + relativeLocation), version);
                contextDeps.put(URI.create(relativeLocation), version);
            }
        }
    }

    static boolean isCompositeHashIRI(IRI iri) {
        boolean isCompositeHashURI = false;
        if (HashKeyUtil.isLikelyCompositeHashURI(iri)) {
            IRI embeddedContentHash = HashKeyUtil.extractContentHash(iri);
            if (embeddedContentHash != null && !StringUtils.startsWith(iri.getIRIString(), embeddedContentHash.getIRIString())) {
                isCompositeHashURI = true;
            }
        }
        return isCompositeHashURI;
    }

    private void handleFormat(String line) {
        Matcher matcher = FORMAT_PATTERN.matcher(line);
        if (matcher.matches()) {
            String location = matcher.group("location");
            if (resourceLocation == null
                    || !StringUtils.equals(location, resourceLocation.getIRIString())) {
                if (getResourceNamespace() == null) {
                    resetContext();
                    resourceLocation = RefNodeFactory.toIRI(location);
                    resourceNamespace = RefNodeFactory.toIRI(URN_LSID_GLOBALBIOTICINTERACTIONS_ORG + "local");
                }
            }
            setResourceFormat(matcher.group("format"));
        }
        // possible format statement
    }

    private Dataset handleAssociation(String line, Dataset dataset) {
        Matcher matcher = NAMESPACE_PATTERN.matcher(line);
        if (matcher.matches()) {
            dataset = datasetForContextOrReset();
            setResourceLocation(RefNodeFactory.toIRI(matcher.group("location")));
            setResourceNamespace(RefNodeFactory.toIRI(matcher.group("namespace")));
            setResourceFormat(APPLICATION_GLOBI);

            startNamespaceContext(RefNodeFactory.toIRI(matcher.group("activity")));
        }
        return dataset;
    }

    private void startNamespaceContext(IRI activity) {
        String parentActivity = activityRelations.get(activity.getIRIString());
        resourceActivityContext = parentActivity == null ? activity : RefNodeFactory.toIRI(parentActivity);
        contextComplete = false;
    }

    private IRI setVersionOnMatchingLocation(Matcher matcher, String location) {
        IRI version = RefNodeFactory.toIRI(matcher.group("version"));
        if (resourceLocation != null && StringUtils.equals(location, resourceLocation.getIRIString())) {
            resourceVersion = version;
        }
        return version;
    }

    private boolean inSameActivity(Matcher matcher) {
        return inSameActivity(matcher.group("activity"));
    }

    private boolean inActiveNamespaceContext(Matcher matcher) {
        String activity = matcher.group("activity");
        boolean sameActivity = false;
        if (inSameActivity(activity)) {
            sameActivity = true;
        } else {
            String parentRelation = activityRelations.get(activity);
            while (StringUtils.isNotBlank(parentRelation)
                    && !StringUtils.equals(parentRelation, activity)) {
                if (inSameActivity(parentRelation)) {
                    sameActivity = true;
                    break;
                } else {
                    parentRelation = activityRelations.get(parentRelation);
                }
            }
        }
        return sameActivity;
    }

    private boolean inSameActivity(String activity) {
        boolean sameActivity = false;
        if (resourceActivityContext != null && StringUtils.equals(activity, resourceActivityContext.getIRIString())) {
            sameActivity = true;
        }
        return sameActivity;
    }

    public Dataset datasetForContextOrReset() {
        Dataset dataset = null;
        if (explicitContextSupported()) {
            dataset = createDataset();
        } else {
            resetContext();
        }
        return dataset;
    }

    private Dataset createDatasetIfComplete(Dataset dataset) {
        if (resourceLocation != null
                && getResourceNamespace() != null
                && getResourceFormat() != null
                && resourceVersion != null) {
            if (explicitContextSupported()) {
                if (contextComplete) {
                    dataset = createDataset();
                }
            } else {
                dataset = createDataset();
            }
        }
        return dataset;
    }

    private IRI getResourceNamespace() {
        return resourceNamespace;
    }

    private void setResourceNamespace(IRI resourceNamespace) {
        this.resourceNamespace = resourceNamespace;
    }

    private void setResourceLocation(IRI resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    private String getResourceFormat() {
        return resourceFormat;
    }

    private boolean explicitContextSupported() {
        return SUPPORTED_FORMATS.contains(getResourceFormat());
    }

    private Dataset createDataset() {
        String resourceNamespaceIRI = getResourceNamespace().getIRIString();
        String namespace = StringUtils.removeStart(resourceNamespaceIRI, URN_LSID_GLOBALBIOTICINTERACTIONS_ORG);

        Dataset dataset = new DatasetWithResourceMapping(
                namespace,
                URI.create(resourceVersion == null ? resourceLocation.getIRIString() : resourceVersion.getIRIString()),
                resourceService
        );
        ObjectNode config = new ObjectMapper().createObjectNode();
        ObjectNode resourceVersions = new ObjectMapper().createObjectNode();
        contextDeps.forEach((location, version) -> {
            resourceVersions.put(location.toString(), version.getIRIString());
        });
        config.set("resources", resourceVersions);
        if (!StringUtils.startsWith(resourceNamespaceIRI, URN_LSID_GLOBALBIOTICINTERACTIONS_ORG)) {
            config.put("format", getResourceFormat());
        }
        dataset.setConfig(config);

        resetContext();
        return dataset;
    }

    private void resetContext() {
        resourceLocation = null;
        setResourceFormat(resourceFormatDefault);
        resourceVersion = null;
        setResourceNamespace(resourceNamespaceDefault);
        resourceActivityContext = null;
        contextComplete = false;
        contextDeps.clear();
        clearActivityRelations();
    }

    private void clearActivityRelations() {
        activityRelations.clear();
        if (lastAddedActivityRelation != null) {
            activityRelations.put(lastAddedActivityRelation.getKey(), lastAddedActivityRelation.getValue());
        }
    }

    @Override
    public void close() throws IOException {
        contextComplete = true;
    }

    public void setResourceFormat(String resourceFormat) {
        this.resourceFormat = resourceFormat;
    }
}
