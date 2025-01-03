package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.globalbioticinteractions.cache.Cache;
import org.globalbioticinteractions.cache.CacheFactory;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetFactory;
import org.globalbioticinteractions.dataset.DatasetImpl;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryWithCache;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatasetConfigReaderProv implements DatasetConfigReader {
    private static final String ASSOCIATED_WITH = " <http://www.w3.org/ns/prov#wasAssociatedWith> ";
    private static final String FORMAT = " <http://purl.org/dc/elements/1.1/format> ";
    private static final String HAS_VERSION = " <http://purl.org/pav/hasVersion> ";
    private static final String URN_LSID_GLOBALBIOTICINTERACTIONS_ORG = "urn:lsid:globalbioticinteractions.org:";

    private IRI resourceLocation = null;
    private IRI resourceNamespace = null;
    private String resourceFormat = null;
    private IRI resourceVersion = null;

    @Override
    public Dataset readConfig(String line) throws IOException {
        Dataset dataset = null;
        if (StringUtils.contains(line, ASSOCIATED_WITH)) {
            // possible namespace statement
            Pattern namespacePattern = Pattern.compile("<(?<namespace>" + URN_LSID_GLOBALBIOTICINTERACTIONS_ORG + "[^>]+)>" + ASSOCIATED_WITH + "<(?<location>[^>]+)>.*");
            Matcher matcher = namespacePattern.matcher(line);
            if (matcher.matches()) {
                resetContext();
                String location = matcher.group("location");
                resourceLocation = RefNodeFactory.toIRI(location);
                resourceNamespace = RefNodeFactory.toIRI(matcher.group("namespace"));
                resourceFormat = "application/globi";
            }
        } else if (StringUtils.contains(line, FORMAT)) {
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
        } else if (StringUtils.contains(line, HAS_VERSION)) {
            // possible version statement
            Pattern namespacePattern = Pattern.compile("<(?<location>[^>]+)>" + HAS_VERSION + "<(?<version>[^>]+)>.*");
            Matcher matcher = namespacePattern.matcher(line);
            if (matcher.matches()) {
                String location = matcher.group("location");
                if (resourceLocation != null && StringUtils.equals(location, resourceLocation.getIRIString())) {
                    resourceVersion = RefNodeFactory.toIRI(matcher.group("version"));
                }
            }
        }

        if (resourceLocation != null
                && resourceNamespace != null
                && resourceFormat != null
                && resourceVersion != null) {
            String resourceNamespaceString = resourceNamespace.getIRIString();
            String namespace = StringUtils.removeStart(resourceNamespaceString, URN_LSID_GLOBALBIOTICINTERACTIONS_ORG);
            dataset = new DatasetImpl(namespace, null, URI.create(resourceVersion.getIRIString()));
            resetContext();
        }
        return dataset;
    }

    private void resetContext() {
        resourceLocation = null;
        resourceFormat = null;
        resourceVersion = null;
        resourceNamespace = null;
    }
}
