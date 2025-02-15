package org.globalbioticinteractions.dataset;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DatasetRegistryProxy implements DatasetRegistry {

    private final static Logger LOG = LoggerFactory.getLogger(DatasetRegistryProxy.class);

    private final ArrayList<DatasetRegistry> registries;
    private Map<String, DatasetRegistry> registryForNamespace = null;
    ;

    public DatasetRegistryProxy(List<DatasetRegistry> registries) {
        this.registries = new ArrayList<DatasetRegistry>() {{
            addAll(registries);
        }};
    }

    @Override
    public Iterable<String> findNamespaces() throws DatasetRegistryException {
        Collection<String> namespacesAll = new ArrayList<>();
        for (DatasetRegistry registry : registries) {
            Iterable<String> namespaces = registry.findNamespaces();
            Collection<String> newNamespaces = CollectionUtils.subtract(namespaces, namespacesAll);
            for (String newNamespace : newNamespaces) {
                LOG.info("associating [" + newNamespace + "] with [" + registry.getClass().getSimpleName() + "]");
                associateNamespaceWithRegistry(registry, newNamespace);
            }
            namespacesAll.addAll(newNamespaces);
        }

        return namespacesAll;
    }

    @Override
    public void findNamespaces(Consumer<String> namespaceConsumer) throws DatasetRegistryException {
        for (String namespace : findNamespaces()) {
            namespaceConsumer.accept(namespace);
        }
    }


    public void associateNamespaceWithRegistry(DatasetRegistry registry, String newNamespace) {
        if (registryForNamespace == null) {
            registryForNamespace = new HashMap<>();
        }
        registryForNamespace.put(newNamespace, registry);
    }


    @Override
    public Dataset datasetFor(String namespace) throws DatasetRegistryException {
        DatasetRegistry registry = registryForNamespace == null
                ? null
                : registryForNamespace.get(namespace);

        Dataset dataset = registry == null
                ? queryForDataset(namespace)
                : registry.datasetFor(namespace);

        if (dataset == null) {
            throw new DatasetRegistryException("failed to find dataset for [" + namespace + "]");
        }

        return dataset;
    }

    private Dataset queryForDataset(String namespace) throws DatasetRegistryException {
        Dataset datasetFirst = null;
        DatasetRegistryException lastException = null;
        List<DatasetRegistry> availableRegistriesForNamespace = new ArrayList<>();
        for (DatasetRegistry datasetRegistry : registries) {
            try {
                Dataset dataset = datasetRegistry.datasetFor(namespace);
                if (dataset != null) {
                    availableRegistriesForNamespace.add(datasetRegistry);
                }
                if (datasetFirst == null) {
                    datasetFirst = dataset;
                }
            } catch (DatasetRegistryException ex) {
                lastException = ex;
            }

        }
        if (availableRegistriesForNamespace.size() < 1 && lastException != null) {
            throw new DatasetRegistryException("failed to find dataset for [" + namespace + "] possibly due to unexpected error", lastException);
        }

        if (availableRegistriesForNamespace.size() > 0) {
            associateNamespaceWithRegistry(availableRegistriesForNamespace.get(0), namespace);
        }

        return datasetFirst;
    }

}
