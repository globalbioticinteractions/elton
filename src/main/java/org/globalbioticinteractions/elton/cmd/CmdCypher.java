package org.globalbioticinteractions.elton.cmd;

import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.util.IdGenerator;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NanoPubWriter;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.UUID;

@CommandLine.Command(
        name = "cypher",
        description = CmdCypher.DESCRIPTION
)
public class CmdCypher extends CmdDefaultParams {

    public static final String DESCRIPTION = "Generate Cypher scripts to help import interaction data in neo4j, a graph database.";

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

        CmdUtil.handleNamespaces(
                registry,
                getNamespaces(),
                "generating Cypher import scripts for namespace",
                getStderr(),
                new CypherLoaderForNamespace(out)
        );
    }

}


