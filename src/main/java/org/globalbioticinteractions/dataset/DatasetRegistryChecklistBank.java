package org.globalbioticinteractions.dataset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eol.globi.service.ResourceService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class DatasetRegistryChecklistBank implements DatasetRegistry {
    private final ResourceService resourceService;
    private int batchSize;

    public DatasetRegistryChecklistBank(ResourceService resourceService) {
        this.resourceService = resourceService;
        this.batchSize = 50;
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
        collectDatasetIds(resourceService, namespaceConsumer, this.batchSize);
    }


    @Override
    public Dataset datasetFor(String namespace) throws DatasetRegistryException {
        return null;
    }

    private static void collectDatasetIds(ResourceService resourceService, Consumer<String> datasetIdListener, int batchSize) throws DatasetRegistryException {
        Long offset = 0L;
        boolean mayHaveMore = true;
        try {
            while (mayHaveMore) {
                URI requestURI = getRegisteryPage(batchSize, offset);
                try (InputStream retrieve = resourceService.retrieve(requestURI)) {
                    AtomicInteger count = new AtomicInteger();
                    collectDatasetIds(retrieve, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            count.incrementAndGet();
                            datasetIdListener.accept(s);
                        }
                    });
                    mayHaveMore = count.get() >= batchSize;
                    if (mayHaveMore) {
                        offset += batchSize;
                    }
                }
            }
        } catch (IOException e) {
            throw new DatasetRegistryException("failed to find published github repos in zenodo", e);
        }
    }

    private static URI getRegisteryPage(int batchSize, long offset) {
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

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
