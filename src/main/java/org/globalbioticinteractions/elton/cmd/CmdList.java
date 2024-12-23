package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.util.ResourceServiceRemote;

import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CommandLine.Command(
        name = "list",
        aliases = "ls",
        description = CmdList.DESCRIPTION
)
public class CmdList extends CmdOnlineParams {

    public static final String DESCRIPTION = "List Available Datasets";

    @Override
    public void doRun() {
        run(getStdout());
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public void run(PrintStream out) {
        InputStreamFactoryLogging inputStreamFactory = createInputStreamFactory();

        DatasetRegistry registryLocal = getDatasetRegistry(inputStreamFactory, getActivityListener());

        File cacheDir = new File(getDataDir());

        List<DatasetRegistry> onlineAndOffline = Arrays.asList(
                new DatasetRegistryZenodo(new ResourceServiceRemote(inputStreamFactory, cacheDir)),
                new DatasetRegistryGitHubArchive(new ResourceServiceRemote(inputStreamFactory, cacheDir)),
                registryLocal
        );

        List<DatasetRegistry> registries = isOnline() ? onlineAndOffline : Collections.singletonList(registryLocal);

        DatasetRegistry registry = new DatasetRegistryProxy(registries);
        try {
            registry.findNamespaces(out::println);
        } catch (
                DatasetRegistryException e) {
            throw new RuntimeException(e);
        }
    }


}
