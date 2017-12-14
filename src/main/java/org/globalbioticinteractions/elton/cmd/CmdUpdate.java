package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinder;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetFinderGitHubArchive;
import org.eol.globi.service.DatasetFinderProxy;
import org.eol.globi.service.DatasetFinderZenodo;
import org.eol.globi.service.GitHubImporterFactory;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;

import java.util.Arrays;

@Parameters(separators = "= ", commandDescription = "Update Datasets with Local Repository")
public class CmdUpdate extends CmdDefaultParams {

    private final static Log LOG = LogFactory.getLog(CmdUpdate.class);

    @Override
    public void run() {
        DatasetFinder finder = new DatasetFinderProxy(Arrays.asList(new DatasetFinderZenodo(), new DatasetFinderGitHubArchive()));
        try {
            NamespaceHandler handler = namespace -> {
                LOG.info("update of [" + namespace + "] starting...");

                Dataset dataset =
                        DatasetFactory.datasetFor(namespace,
                                CmdUtil.createDataFinderLoggingCaching(finder, namespace, getCacheDir()));
                NodeFactoryNull nodeFactoryNull = new NodeFactoryNull();
                nodeFactoryNull.getOrCreateDataset(dataset);
                try {
                    new GitHubImporterFactory()
                            .createImporter(dataset, nodeFactoryNull)
                            .importStudy();
                } catch (StudyImporterException ex) {
                    LOG.error("update of [" + namespace + "] failed.", ex);
                } finally {
                    LOG.info("update of [" + namespace + "] done.");
                }
            };
            LOG.info("updates starting...");
            CmdUtil.handleNamespaces(finder, handler, getNamespaces());
            LOG.info("updates done.");
        } catch (DatasetFinderException e) {
            throw new RuntimeException(e);
        }
    }

}
