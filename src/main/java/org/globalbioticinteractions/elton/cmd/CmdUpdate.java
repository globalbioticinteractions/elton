package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.service.StudyImporterFactory;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetFactory;
import org.globalbioticinteractions.dataset.DatasetFinderException;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;

import java.util.ArrayList;
import java.util.List;

@Parameters(separators = "= ", commandDescription = "Update Local Datasets With Remote Sources")
public class CmdUpdate extends CmdDefaultParams {

    private final static Log LOG = LogFactory.getLog(CmdUpdate.class);

    @Parameter(names = {"--registries", "--registry"},
            description = "[registry1] [registry2] ..."
            )

    private List<String> registryNames = new ArrayList<String>() {{
        add("zenodo");
        add("github");
    }};

    @Override
    public void run() {
        InputStreamFactoryLogging inputStreamFactory = createInputStreamFactory();

        List<DatasetRegistry> registries = new ArrayList<>();
        for (String registryName : registryNames) {
            DatasetRegistryFactoryImpl datasetRegistryFactory = new DatasetRegistryFactoryImpl(inputStreamFactory);
            try {
                DatasetRegistry registry = datasetRegistryFactory.createRegistryByName(registryName);
                registries.add(registry);
            } catch (DatasetFinderException e) {
                throw new RuntimeException("unsupported registry with name [" + registryName + "]");
            }
        }

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

    public List<String> getRegistryNames() {
        return registryNames;
    }

}
