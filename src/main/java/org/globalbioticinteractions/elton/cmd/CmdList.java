package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.elton.util.ResourceServiceRemote;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;
import org.globalbioticinteractions.elton.store.AccessLogger;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@CommandLine.Command(
        name = "list",
        aliases = "ls",
        description = CmdList.DESCRIPTION
)
public class CmdList extends CmdOnlineParams {

    public static final String DESCRIPTION = "List Available Datasets";

    @Override
    public void doRun() {
        run(getStdout());
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    public void run(PrintStream out) {
        InputStreamFactoryLogging inputStreamFactory = createInputStreamFactory();

        DatasetRegistry registryLocal = getDatasetRegistryWithProv();

        ActivityListener activityListener =
                getEnableProvMode()
                        ? getActivityListenerWithProv()
                        : new AccessLogger(DatasetRegistryUtil.NAMESPACE_LOCAL, getProvDir());


        File tmpDir = new File(getWorkDir());

        ResourceService serviceRemote = getEnableProvMode()
                ? getResourceServiceRemoteWithProv(inputStreamFactory, activityListener, tmpDir)
                : new ResourceServiceRemote(inputStreamFactory, tmpDir);

        List<DatasetRegistry> registries =
                isOnline()
                        ? getOnlineAndOfflineRegistries(registryLocal, serviceRemote)
                        : Collections.singletonList(registryLocal);

        PrintStream dataSink = getDataSink(out);

        DatasetRegistry registry = new DatasetRegistryProxy(registries);
        try {
            registry.findNamespaces(namespace -> {
                        Dataset dataset;
                        try {
                            dataset = registry.datasetFor(namespace);

                            CmdUtil.stateDatasetArchiveAssociations(dataset, getActivityContext())
                                    .forEach(getStatementListener()::on);

                            String contentHash = dataset.getOrDefault("contentHash", null);
                            if (StringUtils.isNotBlank(contentHash)) {
                                IRI archiveContentId = RefNodeFactory.toIRI("hash://sha256/" + contentHash);
                                getStatementListener().on(RefNodeFactory.toStatement(
                                        getActivityContext().getActivity(),
                                        RefNodeFactory.toIRI(dataset.getArchiveURI()),
                                        RefNodeConstants.HAS_VERSION,
                                        archiveContentId)
                                );
                                getDependencies().add(archiveContentId.getIRIString());
                            }
                        } catch (DatasetRegistryException e) {
                            // opportunistic association
                        }

                        dataSink.println(namespace);

                    }
            );
        } catch (
                DatasetRegistryException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DatasetRegistry> getOnlineAndOfflineRegistries(DatasetRegistry registryLocal, ResourceService resourceServiceRemote) {
        List<DatasetRegistry> onlineAndOffline = new ArrayList<>();

        List<String> registryNames = getRegistryNames();
        if (registryNames.contains("zenodo")) {
            onlineAndOffline.add(new DatasetRegistryZenodo(resourceServiceRemote));
        }
        if (registryNames.contains("github")) {
            onlineAndOffline.add(new DatasetRegistryGitHubArchive(resourceServiceRemote));
        }
        onlineAndOffline.add(registryLocal);
        return onlineAndOffline;
    }


}
