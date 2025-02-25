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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bio.guoda.preston.RefNodeConstants.HAS_FORMAT;

public class DatasetConfigReaderProv implements DatasetConfigReader, Closeable {
    private static final String ASSOCIATED_WITH = " " + RefNodeConstants.WAS_ASSOCIATED_WITH + " ";
    private static final String FORMAT = " " + HAS_FORMAT + " ";
    private static final String HAS_VERSION = " " + RefNodeConstants.HAS_VERSION + " ";
    private static final String URN_LSID_GLOBALBIOTICINTERACTIONS_ORG = "urn:lsid:globalbioticinteractions.org:";
    public static final String ENDED_AT_TIME = " " + RefNodeConstants.ENDED_AT_TIME + " ";
    public static final String WAS_INFORMED_BY = " " + RefNodeConstants.WAS_INFORMED_BY + " ";
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
        } else if (StringUtils.contains(line, WAS_INFORMED_BY)) {
            Pattern namespacePattern = Pattern.compile("<(?<activity>[^>]+)>" + WAS_INFORMED_BY + "<(?<parentActivity>[^>]+)>" + ".*");
            Matcher matcher = namespacePattern.matcher(line);
            if (matcher.matches()) {
                String activity = matcher.group("activity");
                String parentActivity = matcher.group("parentActivity");
                lastAddedActivityRelation = org.apache.commons.lang3.tuple.Pair.of(activity, parentActivity);
                activityRelations.put(activity, parentActivity);

            }
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
                if (inSameActivityOrSharedParentActivity(matcher)) {
                    IRI version = setVersionOnMatchingLocation(matcher, location);
                    if (contextSupported()) {
                        if (!isCompositeHashIRI(RefNodeFactory.toIRI(location))) {
                            contextDeps.put(URI.create(location), version);
                            if (StringUtils.startsWith(location, resourceLocation.getIRIString())) {
                                contextDeps.put(URI.create("/" + StringUtils.removeStart(location, resourceLocation.getIRIString())), version);
                                contextDeps.put(URI.create(StringUtils.removeStart(location, resourceLocation.getIRIString())), version);
                            }
                        }
                    }
                }
            } else {
                setVersionOnMatchingLocation(matcher, location);
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
            dataset = datasetForContextOrReset();
            String location = matcher.group("location");
            resourceLocation = RefNodeFactory.toIRI(location);
            resourceNamespace = RefNodeFactory.toIRI(matcher.group("namespace"));
            resourceFormat = "application/globi";
            IRI activity = RefNodeFactory.toIRI(matcher.group("activity"));
            String parentActivity = activityRelations.get(activity.getIRIString());
            resourceActivityContext = parentActivity == null ? activity : RefNodeFactory.toIRI(parentActivity);
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

    private boolean inSameActivityOrSharedParentActivity(Matcher matcher) {
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
        return Arrays.asList("application/globi").contains(resourceFormat);
    }

    private Dataset createDataset() {
        String resourceNamespaceString = resourceNamespace.getIRIString();
        String namespace = StringUtils.removeStart(resourceNamespaceString, URN_LSID_GLOBALBIOTICINTERACTIONS_ORG);

        Dataset dataset = new DatasetWithResourceMapping(namespace, URI.create(resourceVersion == null ? resourceLocation.getIRIString() : resourceVersion.getIRIString()), resourceService);
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
}
