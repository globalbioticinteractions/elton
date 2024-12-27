package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.util.ResourceServiceRemote;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;
import org.globalbioticinteractions.dataset.DatasetWithCache;
import org.globalbioticinteractions.elton.store.AccessLogger;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.store.LocalPathToHashIRI;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.ResourceServiceListening;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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


        File cacheDir = new File(getDataDir());

        ResourceServiceListening resourceServiceRemote
                = new ResourceServiceListening(
                getActivityIdFactory(),
                activityListener,
                getActivityContext(),
                new ResourceServiceRemote(inputStreamFactory, cacheDir),
                new LocalPathToHashIRI(new File(getDataDir()))
        );


        List<DatasetRegistry> onlineAndOffline = Arrays.asList(
                new DatasetRegistryZenodo(resourceServiceRemote),
                new DatasetRegistryGitHubArchive(resourceServiceRemote),
                registryLocal
        );

        List<DatasetRegistry> registries =
                isOnline()
                        ? onlineAndOffline
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


}
