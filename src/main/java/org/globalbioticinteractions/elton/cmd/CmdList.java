package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.lang.StringUtils;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        DatasetRegistry registryLocal = DatasetRegistryUtil.forCacheDirOrLocalDir(getCacheDir(), getWorkDir(), inputStreamFactory);
        DatasetRegistry registry = isOnline()
                ? new DatasetRegistryProxy(Arrays.asList(new DatasetRegistryZenodo(inputStreamFactory), new DatasetRegistryGitHubArchive(inputStreamFactory), registryLocal))
                : registryLocal;
        try {
            List<String> namespaces = registry.findNamespaces()
                    .stream()
                    .filter(StringUtils::isNotEmpty)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            out.println(StringUtils.join(namespaces, "\n"));
        } catch (DatasetRegistryException e) {
            throw new RuntimeException(e);
        }
    }


}
