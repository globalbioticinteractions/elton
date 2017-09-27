package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.lang.StringUtils;
import org.eol.globi.service.DatasetFinder;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetFinderGitHubArchive;
import org.eol.globi.service.DatasetFinderProxy;
import org.eol.globi.service.DatasetFinderZenodo;
import org.globalbioticinteractions.dataset.DatasetFinderLocal;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(separators = "= ", commandDescription = "List Available Datasets")
public class CmdList extends CmdOfflineParams {

    @Override
    public void run() {
        run(System.out);
    }

    public void run(PrintStream out) {
        DatasetFinderLocal finderLocal = CmdUtil.getDatasetFinderLocal(getCacheDir());
        DatasetFinder finder = isOffline()
                ? finderLocal
                : new DatasetFinderProxy(Arrays.asList(new DatasetFinderZenodo(), new DatasetFinderGitHubArchive(), finderLocal));
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
