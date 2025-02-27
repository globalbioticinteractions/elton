package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.KeyTo1LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.elton.util.ResourceServiceRemote;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.store.ActivityProxy;
import org.globalbioticinteractions.elton.store.ProvLoggerWithClock;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public abstract class CmdRegistry extends CmdDefaultParams {
    @CommandLine.Option(names = {"--registries", "--registry"},
            description = "[registry1],[registry2],..."
    )
    private List<String> registryNames = new ArrayList<String>() {{
        add("zenodo");
        add("github");
    }};

    public void setRegistryNames(List<String> registryNames) {
        this.registryNames = registryNames;
    }

    public List<String> getRegistryNames() {
        return registryNames;
    }

    protected ResourceService getResourceServiceRemoteWithProv(InputStreamFactoryLogging inputStreamFactory, ActivityListener activityListener, File tmpDir) {
        File dataFolder = new File(getDataDir());

        KeyTo1LevelPath keyToPath = new KeyTo1LevelPath(dataFolder.toURI());
        BlobStoreAppendOnly blobStore = new BlobStoreAppendOnly(
                new KeyValueStoreLocalFileSystem(
                        dataFolder,
                        keyToPath,
                        new ValidatingKeyValueStreamContentAddressedFactory()
                ),
                true,
                getHashType()
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
                final ActivityListener proxy = new ActivityProxy(Arrays.asList(logger, activityListener));
                proxy.onStarted(getActivityContext().getActivity(), activityId, request);
                InputStream retrieve = resourceServiceRemote.retrieve(uri);
                IRI put = blobStore.put(retrieve);
                proxy.onCompleted(getActivityContext().getActivity(), activityId, request, put, null);
                return blobStore.get(put);
            }
        };
    }
}
