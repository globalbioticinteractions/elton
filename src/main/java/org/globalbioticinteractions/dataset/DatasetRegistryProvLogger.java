package org.globalbioticinteractions.dataset;

import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.StatementListener;
import org.apache.commons.rdf.api.Quad;
import org.globalbioticinteractions.elton.cmd.CmdUtil;

import java.util.List;
import java.util.function.Consumer;

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


        List<Quad> associateDatasetArchive = CmdUtil.stateDatasetArchiveAssociations(dataset, ctx);


        associateDatasetArchive.forEach(getStatementListener()::on);

        return dataset;
    }

    private DatasetRegistry getRegistry() {
        return this.registry;
    }

    private StatementListener getStatementListener() {
        return this.listener;
    }


}