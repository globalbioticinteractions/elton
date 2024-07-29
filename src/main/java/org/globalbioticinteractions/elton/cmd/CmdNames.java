package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.data.NodeFactory;
import org.eol.globi.tool.NullImportLogger;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import picocli.CommandLine;

import java.io.PrintStream;

@CommandLine.Command(
        name = "names",
        aliases = {"taxa", "taxon", "name"},
        description = "List taxa"
)
public class CmdNames extends CmdTabularWriterParams {

    @Override
    public void run() {
        run(System.out);
    }

    void run(PrintStream out) {

        DatasetRegistry registry = DatasetRegistryUtil.forCacheDirOrLocalDir(getCacheDir(), getWorkDir(), createInputStreamFactory());

        NodeFactory nodeFactory = WriterUtil.nodeFactoryForTaxonWriting(!shouldSkipHeader(), out);

        CmdUtil.handleNamespaces(registry, nodeFactory, getNamespaces(), "listing taxa", getStderr(), new NullImportLogger());
    }

}


