package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.tool.NullImportLogger;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.util.DataPackageWriter;
import org.globalbioticinteractions.elton.util.IdGenerator;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NodeFactoryForDataset;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

@CommandLine.Command(
        name = "dwc-dp",
        aliases = "dwc-data-package",
        description = CmdDwCDataPackage.DESCRIPTION
)
public class CmdDwCDataPackage extends CmdDefaultParams {

    public static final String DESCRIPTION = "Collect interaction claims and stream (experimental) DarwinCore data package zip.";
    private IdGenerator idGenerator = () -> UUID.randomUUID().toString();

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public IdGenerator getIdGenerator() {
        return this.idGenerator;
    }

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

        try(InteractionWriter serializer = new DataPackageWriter(out, this.getIdGenerator(), new File(getWorkDir()))) {

            NodeFactoryNull nodeFactory = new NodeFactoryForDataset(serializer, dataset -> dataset);

            final NullImportLogger nullImportLogger = new NullImportLogger();
            final File file = new File(getWorkDir());
            CmdUtil.handleNamespaces(
                    registry,
                    getNamespaces(),
                    "streaming dwc data package to stdout",
                    getStderr(),
                    getNamespaceHandler(registry, nodeFactory, file, nullImportLogger)
            );
        } catch (IOException e) {
            throw new RuntimeException("failed to stream dwc-dp archive", e);
        }
    }

}


