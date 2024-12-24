package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.data.NodeFactory;
import org.eol.globi.tool.NullImportLogger;

import org.globalbioticinteractions.dataset.DatasetRegistry;
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
    public static final String RECORD_TYPE_NAME = "name";

    @Override
    public void doRun() {
        run(getStdout());
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    void run(PrintStream out) {

        DatasetRegistry registry = getDatasetRegistryWithProv();

        NodeFactory nodeFactory = getNodeFactoryForProv(out);

        final File file = new File(getWorkDir());
        CmdUtil.handleNamespaces(registry,
                getNamespaces(),
                "listing taxa",
                getStderr(),
                getNamespaceHandler(registry, nodeFactory, file, getLogger())
        );
    }

    @Override
    public String getRecordType() {
        return RECORD_TYPE_NAME;
    }
}


