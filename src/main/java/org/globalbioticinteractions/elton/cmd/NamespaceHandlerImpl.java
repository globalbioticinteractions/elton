package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.util.DatasetImportUtil;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetFactoryImpl;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.util.NamespaceHandler;

import java.io.File;

public class NamespaceHandlerImpl implements NamespaceHandler {

    private final DatasetRegistry registry;
    private final NodeFactory nodeFactory;
    private final ImportLogger logger;
    private final File workDir;

    public NamespaceHandlerImpl(
            DatasetRegistry registry,
            NodeFactory nodeFactory,
            ImportLogger logger,
            File workDir
    ) {
        this.registry = registry;
        this.nodeFactory = nodeFactory;
        this.logger = logger;
        this.workDir = workDir;
    }

    @Override
    public void onNamespace(String namespace) throws Exception {
        Dataset dataset = new DatasetFactoryImpl(registry).datasetFor(namespace);

        DatasetImportUtil.importDataset(
                null,
                dataset,
                nodeFactory,
                logger,
                CmdUtil.createDummyGeoNamesService(),
                workDir
        );
    }
}
