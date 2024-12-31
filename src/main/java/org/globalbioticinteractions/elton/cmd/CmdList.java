package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.KeyTo1LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.eol.globi.service.ResourceService;
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
import org.globalbioticinteractions.elton.store.CachePullThroughPrestonStore;
import org.globalbioticinteractions.elton.store.LocalPathToHashIRI;
import org.globalbioticinteractions.elton.store.ProvLoggerWithClock;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.ResourceServiceListening;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
                ? getResourceServiceWithProv(inputStreamFactory, activityListener, tmpDir)
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

    private ResourceService getResourceServiceWithProv(InputStreamFactoryLogging inputStreamFactory, ActivityListener activityListener, File tmpDir) {
        File dataFolder = new File(getDataDir());

        KeyTo1LevelPath keyToPath = new KeyTo1LevelPath(dataFolder.toURI());
        BlobStoreAppendOnly blobStore = new BlobStoreAppendOnly(
                new KeyValueStoreLocalFileSystem(
                        dataFolder,
                        keyToPath,
                        new ValidatingKeyValueStreamContentAddressedFactory()
                ),
                true,
                HashType.sha256
        );

        return new ResourceService() {

            private final ResourceServiceRemote resourceServiceRemote = new ResourceServiceRemote(inputStreamFactory, tmpDir);

            private final ProvLoggerWithClock logger = new ProvLoggerWithClock(getStatementListener(), new Supplier<Literal>() {
                @Override
                public Literal get() {
                    return RefNodeFactory.nowDateTimeLiteral();
                }
            });

            @Override
            public InputStream retrieve(URI uri) throws IOException {
                IRI activityId = getActivityIdFactory().get();
                IRI request = RefNodeFactory.toIRI(uri);
                final ActivityListener proxy = new ActivityListener() {
                    List<ActivityListener> listeners = Arrays.asList(logger, activityListener);

                    @Override
                    public void onStarted(IRI parentActivityId, IRI activityId, IRI request) {
                        listeners.forEach(listener -> listener.onStarted(parentActivityId, activityId, request));
                    }

                    @Override
                    public void onCompleted(IRI parentActivityId, IRI activityId, IRI request, IRI response, URI localPathOfResponseData) {
                        listeners.forEach(listener -> listener.onCompleted(parentActivityId, activityId, request, response, localPathOfResponseData));
                    }
                };
                proxy.onStarted(getActivityContext().getActivity(), activityId, request);
                InputStream retrieve = resourceServiceRemote.retrieve(uri);
                IRI put = blobStore.put(retrieve);
                proxy.onCompleted(getActivityContext().getActivity(), activityId, request, put, null);
                return blobStore.get(put);
            }
        };
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
