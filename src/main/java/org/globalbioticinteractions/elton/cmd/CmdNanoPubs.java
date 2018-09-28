package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.eol.globi.service.DatasetRegistry;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.IdGenerator;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NanoPubWriter;
import org.globalbioticinteractions.elton.util.NodeFactoryForDataset;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;

import java.io.PrintStream;
import java.util.UUID;

@Parameters(separators = "= ", commandDescription = "Generate Nanopubs Describing Interactions of Published Datasets")
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

    InteractionWriter createSerializer(PrintStream out) {
        return new NanoPubWriter(out, this.getIdGenerator());
    }

    void run(PrintStream out) {
        DatasetRegistry finder = DatasetRegistryUtil.forCacheDir(getCacheDir());

        InteractionWriter serializer = createSerializer(out);
        NodeFactoryNull nodeFactory = new NodeFactoryForDataset(serializer, dataset -> dataset);

        CmdUtil.handleNamespaces(finder
                , nodeFactory
                , getNamespaces()
                , "generating trusty nanopubs for");
    }

}


