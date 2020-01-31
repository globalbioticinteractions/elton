package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetRegistry;
import org.eol.globi.service.DatasetRegistryGitHubArchive;
import org.eol.globi.service.DatasetRegistryProxy;
import org.eol.globi.service.DatasetRegistryZenodo;
import org.eol.globi.service.GitHubImporterFactory;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;

import java.util.Arrays;

@Parameters(separators = "= ", commandDescription = "Sync Datasets With Remote Sources")
public class CmdSync extends CmdDefaultParams {

    private final static Log LOG = LogFactory.getLog(CmdSync.class);

    @Override
    public void run() {
        InputStreamFactoryLogging inputStreamFactory = createInputStreamFactory();
        DatasetRegistry finder = new DatasetRegistryProxy(Arrays.asList(new DatasetRegistryZenodo(inputStreamFactory), new DatasetRegistryGitHubArchive(inputStreamFactory)));
        NamespaceHandler handler = namespace -> {
            getStderr().print("updating [" + namespace + "]... ");

            DatasetRegistry registry = CmdUtil.createDataFinderLoggingCaching(finder, namespace, getCacheDir(), inputStreamFactory);
            Dataset dataset =
                    new DatasetFactory(registry, createInputStreamFactory())
                            .datasetFor(namespace);
            NodeFactory factory = new NodeFactoryNull();
            factory.getOrCreateDataset(dataset);
            try {
                new GitHubImporterFactory()
                        .createImporter(dataset, factory)
                        .importStudy();
            } catch (StudyImporterException ex) {
                LOG.error("\bupdate of [" + namespace + "] failed.", ex);
                getStderr().println("failed with [ " + ex.getMessage() + "]");
            } finally {
                getStderr().println("\bdone.");
            }
        };

        try {
            CmdUtil.handleNamespaces(finder, handler, getNamespaces());
        } catch (DatasetFinderException e) {
            throw new RuntimeException(e);
        }
    }

}
