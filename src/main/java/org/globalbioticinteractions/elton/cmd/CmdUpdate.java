package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinder;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetFinderGitHubArchive;
import org.eol.globi.service.DatasetFinderProxy;
import org.eol.globi.service.DatasetFinderZenodo;
import org.eol.globi.service.GitHubImporterFactory;
import org.globalbioticinteractions.dataset.DatasetFinderCaching;

import java.util.Arrays;

@Parameters(separators = "= ", commandDescription = "Update Datasets with Local Repository")
public class CmdUpdate extends CmdDefaultParams {

    @Override
    public void run() {
        DatasetFinder finder = new DatasetFinderProxy(Arrays.asList(new DatasetFinderZenodo(), new DatasetFinderGitHubArchive()));
        try {
            NamespaceHandler handler = namespace -> {
                Dataset dataset =
                        DatasetFactory.datasetFor(namespace, new DatasetFinderCaching(finder, getCacheDir()));
                NodeFactoryNull nodeFactoryNull = new NodeFactoryNull();
                nodeFactoryNull.getOrCreateDataset(dataset);
                new GitHubImporterFactory()
                        .createImporter(dataset, nodeFactoryNull)
                        .importStudy();
            };
            CmdUtil.handleNamespaces(finder, handler, getNamespaces());
        } catch (DatasetFinderException e) {
            throw new RuntimeException(e);
        }
    }

}
