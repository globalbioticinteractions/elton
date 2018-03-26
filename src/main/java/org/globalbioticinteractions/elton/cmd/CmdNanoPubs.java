package org.globalbioticinteractions.elton.cmd;

import java.io.PrintStream;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globalbioticinteractions.dataset.DatasetFinderLocal;
import org.globalbioticinteractions.elton.util.IdGenerator;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NanoPubWriter;
import org.globalbioticinteractions.elton.util.NodeFactoryForDataset;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;

import com.beust.jcommander.Parameters;

@Parameters(separators = "= ", commandDescription = "Generate Nanopubs Describing Interactions in Local Datasets")
public class CmdNanoPubs extends CmdInteractions {

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

    InteractionWriter createSerializer(PrintStream out) {
        return new NanoPubWriter(out, this.getIdGenerator());
    }

    void run(PrintStream out) {
        DatasetFinderLocal finder = CmdUtil.getDatasetFinderLocal(getCacheDir());

        InteractionWriter serializer = createSerializer(out);

        NodeFactoryNull nodeFactory = new NodeFactoryForDataset(serializer, dataset -> dataset);

        CmdUtil.handleNamespaces(finder
                , nodeFactory
                , getNamespaces()
                , "generating trusty nanopubs for");
    }

}


