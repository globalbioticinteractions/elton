package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.tool.NullImportLogger;

import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.IdGenerator;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NanoPubWriter;
import org.globalbioticinteractions.elton.util.NodeFactoryForDataset;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.UUID;

@CommandLine.Command(
        name = "nanopubs",
        description = "List Interaction Nanopubs, see http://nanopub.org"
)
public class CmdNanoPubs extends CmdDefaultParams {

    private IdGenerator idGenerator = () -> UUID.randomUUID().toString().replaceAll("-", "");

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public IdGenerator getIdGenerator() {
        return this.idGenerator;
    }

    @Override
    public void run() {
        run(System.out);
    }

    private InteractionWriter createSerializer(PrintStream out) {
        return new NanoPubWriter(out, this.getIdGenerator());
    }

    void run(PrintStream out) {
        DatasetRegistry registry = DatasetRegistryUtil.forCacheOrLocalDir(
                getDataDir(),
                getProvDir(),
                getWorkDir(),
                createInputStreamFactory(),
                getContentPathFactory(), getProvenancePathFactory()
        );

        InteractionWriter serializer = createSerializer(out);

        NodeFactoryNull nodeFactory = new NodeFactoryForDataset(serializer, dataset -> dataset);

        CmdUtil.handleNamespaces(registry
                , nodeFactory
                , getNamespaces()
                , "listing nanopubs",
                getStderr(),
                new NullImportLogger(),
                new File(getWorkDir())
        );
    }

}


