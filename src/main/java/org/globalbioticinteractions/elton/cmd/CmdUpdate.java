package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.StudyImporterException;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetFactory;
import org.globalbioticinteractions.dataset.DatasetFinderException;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;
import org.eol.globi.service.StudyImporterFactory;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;

import java.util.Arrays;
import java.util.List;

@Parameters(separators = "= ", commandDescription = "Update Local Datasets With Remote Sources")
public class CmdUpdate extends CmdDefaultParams {

    private final static Log LOG = LogFactory.getLog(CmdUpdate.class);

    @Override
    public void run() {
        InputStreamFactoryLogging inputStreamFactory = createInputStreamFactory();

        List<DatasetRegistry> registries = Arrays.asList(
                new DatasetRegistryZenodo(inputStreamFactory),
                new DatasetRegistryGitHubArchive(inputStreamFactory));

        DatasetRegistry registryProxy = new DatasetRegistryProxy(registries);
        NamespaceHandler namespaceHandler = namespace -> {
            getStderr().print("updating [" + namespace + "]... ");

            DatasetRegistry registry = CmdUtil.createDataFinderLoggingCaching(
                    registryProxy,
                    namespace,
                    getCacheDir(),
                    inputStreamFactory);

            Dataset dataset =
                    new DatasetFactory(registry, createInputStreamFactory())
                            .datasetFor(namespace);
            NodeFactory factory = new NodeFactoryNull();
            factory.getOrCreateDataset(dataset);
            try {
                new StudyImporterFactory()
                        .createImporter(dataset, factory)
                        .importStudy();
                getStderr().println("done.");
            } catch (StudyImporterException ex) {
                LOG.error("update of [" + namespace + "] failed.", ex);
                getStderr().println("failed with [ " + ex.getMessage() + "].");
            }
        };

        try {
            CmdUtil.handleNamespaces(registryProxy, namespaceHandler, getNamespaces());
        } catch (DatasetFinderException e) {
            throw new RuntimeException(e);
        }
    }

}
