package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.lang.StringUtils;
import org.globalbioticinteractions.dataset.DatasetFinderException;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(separators = "= ", commandDescription = "List Available Datasets")
public class CmdList extends CmdOnlineParams {

    @Override
    public void run() {
        run(getStdout());
    }

    public void run(PrintStream out) {
        InputStreamFactoryLogging inputStreamFactory = createInputStreamFactory();
        DatasetRegistry finderLocal = DatasetRegistryUtil.forCacheDirOrLocalDir(getCacheDir(), getWorkDir(), getTmpDir(), inputStreamFactory);
        DatasetRegistry finder = isOnline()
                ? new DatasetRegistryProxy(Arrays.asList(new DatasetRegistryZenodo(inputStreamFactory), new DatasetRegistryGitHubArchive(inputStreamFactory), finderLocal))
                : finderLocal;
        try {
            List<String> namespaces = finder.findNamespaces()
                    .stream()
                    .filter(StringUtils::isNotEmpty)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            out.println(StringUtils.join(namespaces, "\n"));
        } catch (DatasetFinderException e) {
            throw new RuntimeException(e);
        }
    }


}
