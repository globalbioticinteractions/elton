package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.ActivityUtil;
import org.apache.commons.io.output.NullAppendable;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.service.ResourceService;
import org.eol.globi.tool.NullImportLogger;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetProxy;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.elton.Elton;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;

@CommandLine.Command(
        name = "log",
        aliases = {"prov"},
        description = "lists provenance of original resources"
)
public class CmdLog extends CmdDefaultParams {

    @CommandLine.Option(
            names = {"--hash-algorithm", "--algo", "-a"},
            description = "Hash algorithm used to generate primary content identifiers. Supported values: ${COMPLETION-CANDIDATES}."
    )
    private HashType hashType = HashType.sha256;

    private ActivityContext ctx = ActivityUtil.createNewActivityContext("Tracking the origins of species interaction dataset");

    private final LoggingEmitter emitter = new LoggingEmitter(this);

    @Override
    public void run() {
        DatasetRegistry registry = DatasetRegistryUtil.forCacheDirOrLocalDir(
                getCacheDir(),
                getWorkDir(),
                createInputStreamFactory()
        );

        DatasetRegistry proxy = new DatasetRegistryProxy(Collections.singletonList(registry)) {
            public Dataset datasetFor(String namespace) throws DatasetRegistryException {
                Dataset dataset = super.datasetFor(namespace);
                return new DatasetProxy(dataset) {
                    ResourceService service = new LoggingResourceService(dataset, hashType, ctx, emitter);

                    public InputStream retrieve(URI resourcePath) throws IOException {
                        return service.retrieve(resourcePath);
                    }
                };
            }
        };

        NodeFactory nodeFactory = new NodeFactoryNull();

        CmdUtil.handleNamespaces(
                proxy,
                nodeFactory,
                getNamespaces(),
                "logging provenance",
                NullAppendable.INSTANCE,
                new NullImportLogger());
    }

    public void setHashType(HashType hashType) {
        this.hashType = hashType;
    }

}


