package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.ActivityUtil;
import org.apache.commons.io.output.NullAppendable;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
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
import java.util.List;

@CommandLine.Command(
        name = "log",
        aliases = {"prov"},
        description = CmdLog.DESCRIPTION
)
public class CmdLog extends CmdDefaultParams {

    public static final String DESCRIPTION = "lists provenance of original resources";
    public static final String ELTON_CITATION_PREFIX = "Jorrit Poelen, Tobias Kuhn & Katrin Leinweber. (2023). globalbioticinteractions/elton: ";
    public static final String ELTON_DESCRIPTION = "Elton helps to access, review and index existing species interaction datasets.";
    public static final IRI ELTON_CONCEPT_DOI = RefNodeFactory.toIRI("https://zenodo.org/doi/10.5281/zenodo.998263");
    @CommandLine.Option(
            names = {"--hash-algorithm", "--algo", "-a"},
            description = "Hash algorithm used to generate primary content identifiers. Supported values: ${COMPLETION-CANDIDATES}."
    )
    private HashType hashType = HashType.sha256;

    private ActivityContext ctx = ActivityUtil.createNewActivityContext("Tracking the origins of species interaction dataset");

    private final LoggingEmitter emitter = new LoggingEmitter(this);

    @Override
    public void doRun() {
        DatasetRegistry registry = getDatasetRegistry();

        emitter.emit(getEltonDescription(ctx));


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

    public static List<Quad> getEltonDescription(ActivityContext ctx) {
        String citationString = "Jorrit Poelen, Tobias Kuhn & Katrin Leinweber. (2017/2024). globalbioticinteractions/elton: "
                + Elton.getVersionString()
                + ". Zenodo. "
                + ELTON_CONCEPT_DOI.getIRIString();
        return ActivityUtil.generateSoftwareAgentProcessDescription(
                ctx,
                ELTON_CONCEPT_DOI,
                ELTON_CONCEPT_DOI,
                citationString,
                ELTON_DESCRIPTION);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public void setHashType(HashType hashType) {
        this.hashType = hashType;
    }

}


