package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.util.ResourceServiceRemote;

import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

@CommandLine.Command(
        name = "list",
        aliases = "ls",
        description = "List Available Datasets"
)
public class CmdList extends CmdOnlineParams {

    @Override
    public void run() {
        run(getStdout());
    }

    public void run(PrintStream out) {
        InputStreamFactoryLogging inputStreamFactory = createInputStreamFactory();
        DatasetRegistry registryLocal = DatasetRegistryUtil.forCacheDirOrLocalDir(getCacheDir(), getWorkDir(), inputStreamFactory, getContentPathFactory(), getProvenancePathFactory());

        File cacheDir = new File(getCacheDir());
        List<DatasetRegistry> registries =
                isOnline()
                ? Arrays.asList(
                new DatasetRegistryZenodo(new ResourceServiceRemote(inputStreamFactory, cacheDir)),
                new DatasetRegistryGitHubArchive(new ResourceServiceRemote(inputStreamFactory, cacheDir)),
                registryLocal)
                : Arrays.asList(registryLocal);

        DatasetRegistry registry = new DatasetRegistryProxy(registries);
        try {
            registry.findNamespaces(out::println);
        } catch (
                DatasetRegistryException e) {
            throw new RuntimeException(e);
        }
    }


}
