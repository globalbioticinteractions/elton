package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.util.DatasetImportUtil;
import org.globalbioticinteractions.cache.CacheUtil;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetFactory;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryAccessLogger;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryProvLogger;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@CommandLine.Command(
        name = "sync",
        aliases = {"pull", "update", "track"},
        description = CmdUpdate.DESCRIPTION
)

public class CmdUpdate extends CmdDefaultParams {

    private final static Logger LOG = LoggerFactory.getLogger(CmdUpdate.class);
    public static final String DESCRIPTION = "Update Local Datasets With Remote Sources";

    public void setRegistryNames(List<String> registryNames) {
        this.registryNames = registryNames;
    }

    @CommandLine.Option(names = {"--registries", "--registry"},
            description = "[registry1],[registry2],..."
    )
    private List<String> registryNames = new ArrayList<String>() {{
        add("zenodo");
        add("github");
    }};

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    protected void doRun() {
        InputStreamFactoryLogging inputStreamFactory = createInputStreamFactory();

        List<DatasetRegistry> registries = new ArrayList<>();
        for (String registryName : registryNames) {
            DatasetRegistryFactoryImpl datasetRegistryFactory = new DatasetRegistryFactoryImpl(
                    getWorkDir(),
                    inputStreamFactory,
                    getDataDir(),
                    getProvDir(),
                    getActivityListener(),
                    getActivityContext(),
                    getActivityIdFactory()
            );
            try {
                DatasetRegistry registry = datasetRegistryFactory.createRegistryByName(registryName);
                registries.add(registry);
            } catch (DatasetRegistryException e) {
                throw new RuntimeException("unsupported registry with name [" + registryName + "]", e);
            }
        }

        DatasetRegistry registryProxy = new DatasetRegistryProxy(registries);
        DatasetRegistry registryProvenanceLogger = getEnableProvMode()
                ? new DatasetRegistryProvLogger(registryProxy, getStatementListener(), getActivityContext())
                : new DatasetRegistryAccessLogger(registryProxy, getProvDir());


        NamespaceHandler namespaceHandler = namespace -> {
            getStderr().print("tracking [" + namespace + "]... ");

            FileUtils.forceMkdir(new File(getWorkDir()));
            CacheUtil.findOrMakeProvOrDataDirForNamespace(getProvDir(), namespace);
            CacheUtil.findOrMakeProvOrDataDirForNamespace(getDataDir(), namespace);

            DatasetRegistry registry = CmdUtil.createDataFinderLoggingCaching(
                    namespace,
                    getDataDir(),
                    getProvDir(),
                    inputStreamFactory,
                    getContentPathFactory(),
                    getProvenancePathFactory(),
                    getActivityListener(namespace),
                    getActivityContext(),
                    getActivityIdFactory(),
                    registryProvenanceLogger
            );

            Dataset dataset =
                    new DatasetFactory(registry, createInputStreamFactory())
                            .datasetFor(namespace);

            NodeFactory factory = new NodeFactoryNull();
            factory.getOrCreateDataset(dataset);
            try {
                DatasetImportUtil.importDataset(
                        null,
                        dataset,
                        factory,
                        null,
                        new File(getWorkDir())
                );
                getStderr().println("done.");
            } catch (StudyImporterException ex) {
                LOG.error("tracking of [" + namespace + "] failed.", ex);
                getStderr().println("failed with [" + ex.getMessage() + "].");
                ex.printStackTrace(getStderr());
            }
        };

        try {
            CmdUtil.handleNamespaces(registryProxy, namespaceHandler, getNamespaces());
        } catch (DatasetRegistryException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getRegistryNames() {
        return registryNames;
    }

}
