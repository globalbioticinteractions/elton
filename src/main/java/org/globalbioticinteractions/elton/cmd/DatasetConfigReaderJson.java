package org.globalbioticinteractions.elton.cmd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetImpl;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;

import java.io.IOException;

public class DatasetConfigReaderJson implements DatasetConfigReader {

    @Override
    public Dataset readConfig(String line) throws IOException {
        Dataset dataset = null;
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(line);
            String namespace = jsonNode.at("/namespace").asText(DatasetRegistryUtil.NAMESPACE_LOCAL);
            if (StringUtils.isNotBlank(namespace)) {
                dataset = new DatasetImpl(namespace, null, null);
                dataset.setConfig(jsonNode);
            }
        } catch (JsonProcessingException e) {
            // ignore non-json lines
        }
        return dataset;
    }
}
