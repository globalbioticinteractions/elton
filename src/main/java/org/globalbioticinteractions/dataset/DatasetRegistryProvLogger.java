package org.globalbioticinteractions.dataset;

import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.StatementListener;
import org.apache.commons.rdf.api.IRI;
import org.globalbioticinteractions.elton.store.ActivityListener;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class DatasetRegistryProvLogger implements DatasetRegistry {
    private final DatasetRegistry registry;

    private final StatementListener listener;
    private final ActivityContext ctx;

    public DatasetRegistryProvLogger(DatasetRegistry registryProxy,
                                     StatementListener listener,
                                     ActivityContext activityContext) {
        this.registry = registryProxy;
        this.listener = listener;
        this.ctx = activityContext;

    }

    public Iterable<String> findNamespaces() throws DatasetRegistryException {
        return this.getRegistry().findNamespaces();
    }

    @Override
    public void findNamespaces(Consumer<String> consumer) throws DatasetRegistryException {
        this.getRegistry().findNamespaces(consumer);
    }

    public Dataset datasetFor(String namespace) throws DatasetRegistryException {
        Dataset dataset = this.getRegistry().datasetFor(namespace);

        getStatementListener().on(
                RefNodeFactory.toStatement(ctx.getActivity(),
                        RefNodeFactory.toIRI("urn:lsid:globalbioticinteractions.org:" + namespace),
                        RefNodeConstants.WAS_ASSOCIATED_WITH,
                        RefNodeFactory.toIRI(dataset.getArchiveURI()))
        );

        return dataset;
    }

    private DatasetRegistry getRegistry() {
        return this.registry;
    }

    private StatementListener getStatementListener() {
        return this.listener;
    }


}