package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.process.StatementListener;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.tool.NullImportLogger;

import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;

@CommandLine.Command(
        name = "names",
        aliases = {"taxa", "taxon", "name"},
        description = CmdNames.DESCRIPTION
)
public class CmdNames extends CmdTabularWriterParams {

    public static final String DESCRIPTION = "List taxa";

    @Override
    public void doRun() {
        run(System.out);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    void run(PrintStream out) {

        DatasetRegistry registry = DatasetRegistryUtil.forCacheOrLocalDir(
                getDataDir(),
                getProvDir(),
                getWorkDir(),
                createInputStreamFactory(),
                getContentPathFactory(),
                getProvenancePathFactory(),
                getStatementListener()
        );

        NodeFactory nodeFactory = WriterUtil.nodeFactoryForTaxonWriting(!shouldSkipHeader(), out);

        CmdUtil.handleNamespaces(registry,
                nodeFactory,
                getNamespaces(),
                "listing taxa",
                getStderr(),
                new NullImportLogger(),
                new File(getWorkDir())
        );
    }

}


