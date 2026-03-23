package org.globalbioticinteractions.dataset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eol.globi.service.ResourceService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DatasetRegistryChecklistBank implements DatasetRegistry {
    private final ResourceService resourceService;

    public DatasetRegistryChecklistBank(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override
    public Iterable<String> findNamespaces() throws DatasetRegistryException {
        ArrayList<String> ids = new ArrayList<>();
        findNamespaces(new Consumer<String>() {
            @Override
            public void accept(String s) {
                ids.add(s);
            }
        });
        return ids;
    }

    @Override
    public void findNamespaces(Consumer<String> namespaceConsumer) throws DatasetRegistryException {
        collectDatasetIds(resourceService, namespaceConsumer);
    }


    @Override
    public Dataset datasetFor(String namespace) throws DatasetRegistryException {
        return null;
    }

    static void collectDatasetIds(
            ResourceService resourceService,
            Consumer<String> datasetIdListener) throws DatasetRegistryException {
        try {
            Long batchSize = 50L;
            Long offset = 0L;
            URI requestURI = getRegisteryPage(batchSize, offset);
            try (InputStream retrieve = resourceService.retrieve(requestURI)) {
                collectDatasetIds(retrieve, datasetIdListener);
            }
        } catch (IOException e) {
            throw new DatasetRegistryException("failed to find published github repos in zenodo", e);
        }
    }

    private static URI getRegisteryPage(Long batchSize, Long offset) {
        URI requestURI = URI.create("https://api.checklistbank.org/dataset?" +
                "origin=external" +
                "&" + "origin=project" +
                "&" + "rowType=col%3ASpeciesInteraction" +
                "&"
                + "limit=" + batchSize
                + "&"
                + "offset=" + offset);
        return requestURI;
    }

    static void collectDatasetIds(InputStream is, Consumer<String> datasetIdentifierConsumer) throws DatasetRegistryException {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(is);
            JsonNode result = jsonNode.at("/result");
            for (JsonNode hit : result) {
                JsonNode datasetKey = hit.at("/key");
                if (!datasetKey.isMissingNode()) {
                    datasetIdentifierConsumer.accept("urn:lsid:checklistbank.org:dataset:" + datasetKey.asText());
                }
            }
        } catch (IOException e) {
            throw new DatasetRegistryException("failed to read as checklist bank registry metadata", e);
        }
    }

    static Collection<String> getDatasetIds(InputStream is) throws DatasetRegistryException {
        ArrayList<String> datasets = new ArrayList<>();
        collectDatasetIds(is, datasets::add);
        return datasets;
    }

}
