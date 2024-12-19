package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.cmd.ActivityContext;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.util.DatasetImportUtil;
import org.globalbioticinteractions.cache.CacheUtil;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetFactory;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
                    getActivityListener(), new ActivityContext() {
                        @Override
                        public IRI getActivity() {
                            return null;
                        }

                        @Override
                        public String getDescription() {
                            return null;
                        }
                    }, new Supplier<IRI>() {
                        @Override
                        public IRI get() {
                            return null;
                        }
                    }
            );
            try {
                DatasetRegistry registry = datasetRegistryFactory.createRegistryByName(registryName);
                registries.add(registry);
            } catch (DatasetRegistryException e) {
                throw new RuntimeException("unsupported registry with name [" + registryName + "]", e);
            }
        }

        DatasetRegistry registryProxy = new DatasetRegistryProxy(registries);
        NamespaceHandler namespaceHandler = namespace -> {
            getStderr().print("tracking [" + namespace + "]... ");

            CacheUtil.findOrMakeProvOrDataDirForNamespace(new File(getWorkDir()), namespace);
            CacheUtil.findOrMakeProvOrDataDirForNamespace(getProvDir(), namespace);
            CacheUtil.findOrMakeProvOrDataDirForNamespace(getDataDir(), namespace);

            DatasetRegistry registry = CmdUtil.createDataFinderLoggingCaching(
                    registryProxy,
                    namespace,
                    getDataDir(),
                    getProvDir(),
                    inputStreamFactory,
                    getContentPathFactory(),
                    getProvenancePathFactory(),
                    getActivityListener(namespace),
                    getCtx(),
                    getActivityIdFactory()
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
                getStderr().println("failed with [ " + ex.getMessage() + "].");
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
