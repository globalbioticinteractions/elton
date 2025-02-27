package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.ActivityUtil;
import org.apache.commons.io.output.NullAppendable;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.service.ResourceService;
import org.eol.globi.tool.NullImportLogger;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetProxy;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.elton.Elton;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;

@CommandLine.Command(
        name = "log",
        aliases = {"prov"},
        description = CmdLog.DESCRIPTION
)
public class CmdLog extends CmdDefaultParams {

    public static final String DESCRIPTION = "lists provenance of original resources";

    private ActivityContext ctx = ActivityUtil.createNewActivityContext("Tracking the origins of species interaction dataset");

    private final LoggingEmitter emitter = new LoggingEmitter(this);

    @Override
    public void doRun() {
        DatasetRegistry registry = getDatasetRegistry();

        emitter.emit(Elton.getEltonDescription(ctx));


        DatasetRegistry proxy = new DatasetRegistryProxy(Collections.singletonList(registry)) {
            public Dataset datasetFor(String namespace) throws DatasetRegistryException {
                Dataset dataset = super.datasetFor(namespace);
                return new DatasetProxy(dataset) {
                    ResourceService service = new LoggingResourceService(
                            dataset, getHashType(), ctx, emitter
                    );

                    public InputStream retrieve(URI resourcePath) throws IOException {
                        return service.retrieve(resourcePath);
                    }
                };
            }
        };

        NodeFactory nodeFactory = new NodeFactoryNull();

        final NullImportLogger nullImportLogger = new NullImportLogger();
        final File file = new File(getWorkDir()
        );
        CmdUtil.handleNamespaces(
                proxy,
                getNamespaces(),
                "logging provenance",
                NullAppendable.INSTANCE,
                getNamespaceHandler(proxy, nodeFactory, file, nullImportLogger)
        );
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

}


