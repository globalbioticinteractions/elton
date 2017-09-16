package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.lang.StringUtils;
import org.eol.globi.service.DatasetFinder;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetFinderGitHubArchive;
import org.eol.globi.service.DatasetFinderProxy;
import org.eol.globi.service.DatasetFinderZenodo;
import org.globalbioticinteractions.dataset.DatasetFinderLocal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Parameters(commandDescription = "List Available Datasets")
public class CmdList extends CmdDefaultParams {

    @Parameter(names = {"--offline", "-o"}, description = "offline")
    private boolean offlineOnly = false;


    @Override
    public void run() {
        DatasetFinderLocal finderLocal = new DatasetFinderLocal(getCacheDir());
        DatasetFinder finder = offlineOnly
                ? finderLocal
                : new DatasetFinderProxy(Arrays.asList(new DatasetFinderZenodo(), new DatasetFinderGitHubArchive(), finderLocal));
        try {
            List<String> namespaces = finder.findNamespaces()
                    .stream()
                    .filter(StringUtils::isNotEmpty)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            System.out.println(StringUtils.join(namespaces, "\n"));
        } catch (DatasetFinderException e) {
            throw new RuntimeException(e);
        }
    }
}
