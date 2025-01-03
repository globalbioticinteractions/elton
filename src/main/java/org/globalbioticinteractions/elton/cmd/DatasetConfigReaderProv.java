package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetImpl;

import java.io.IOException;
import java.net.URI;
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
                resetOnLocationSwitch(matcher);
                resourceNamespace = RefNodeFactory.toIRI(matcher.group("namespace"));
            }
        } else if (StringUtils.contains(line, FORMAT)) {
            Pattern namespacePattern = Pattern.compile("<(?<location>[^>]+)>" + FORMAT + "\"(?<format>[^\"]+)\".*");
            Matcher matcher = namespacePattern.matcher(line);
            if (matcher.matches()) {
                resetOnLocationSwitch(matcher);
                resourceFormat = matcher.group("format");
            }
            // possible format statement
        } else if (StringUtils.contains(line, HAS_VERSION)) {
            // possible version statement
            Pattern namespacePattern = Pattern.compile("<(?<location>[^>]+)>" + HAS_VERSION + "<(?<version>[^>]+)>.*");
            Matcher matcher = namespacePattern.matcher(line);
            if (matcher.matches()) {
                resetOnLocationSwitch(matcher);
                resourceVersion = RefNodeFactory.toIRI(matcher.group("version"));
            }
        }

        if (resourceLocation != null
                && resourceFormat != null
                && resourceVersion != null) {
            String resourceNamespaceString = (resourceNamespace == null
                    ? RefNodeFactory.toIRI(URN_LSID_GLOBALBIOTICINTERACTIONS_ORG + "local")
                    : resourceNamespace).getIRIString();

            String format = resourceFormat == null ? "application/dwca" : resourceFormat;

            String namespace = StringUtils.removeStart(resourceNamespaceString, URN_LSID_GLOBALBIOTICINTERACTIONS_ORG);
            if (StringUtils.equals(resourceFormat, "application/globi")) {
                dataset = new DatasetImpl(namespace, null, null);
                ObjectNode globiConfig = new ObjectMapper().createObjectNode();
                globiConfig.put("namespace", resourceVersion.getIRIString());
                globiConfig.put("url", resourceLocation.getIRIString());
                globiConfig.put("format", "globi");
                globiConfig.put("citation", RefNodeFactory.toStatement(resourceLocation, RefNodeConstants.HAS_VERSION, resourceVersion).toString());
                dataset.setConfig(globiConfig);
            } else {
                ObjectNode globiConfig = new ObjectMapper().createObjectNode();
                globiConfig.put("namespace", resourceVersion.getIRIString());
                globiConfig.put("url", "https://linker.bio/" + resourceVersion.getIRIString());
                globiConfig.put("format", StringUtils.replace(format, "application/globi", "globi"));
                globiConfig.put("citation", RefNodeFactory.toStatement(resourceLocation, RefNodeConstants.HAS_VERSION, resourceVersion).toString());
                ArrayNode resourceMapping = new ObjectMapper().createArrayNode();
                ObjectNode objectNode = new ObjectMapper().createObjectNode();
                objectNode.put(resourceLocation.getIRIString(), "https://linker.bio/" + resourceVersion.getIRIString());
                resourceMapping.add(objectNode);
                //globiConfig.set("resources", resourceMapping);
                dataset = new DatasetImpl(namespace, null, null);
                dataset.setConfig(globiConfig);
            }
            resetContext();
        }
        return dataset;
    }

    private void resetOnLocationSwitch(Matcher matcher) {
        String location = matcher.group("location");
        if (resourceLocation == null || !StringUtils.equals(location, resourceLocation.getIRIString())) {
            resetContext();
        }
        resourceLocation = RefNodeFactory.toIRI(location);
    }

    private void resetContext() {
        resourceLocation = null;
        resourceFormat = null;
        resourceVersion = null;
        resourceNamespace = null;
    }
}
