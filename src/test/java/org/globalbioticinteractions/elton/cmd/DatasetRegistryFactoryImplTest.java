package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.cmd.ActivityContext;
import org.apache.commons.rdf.api.IRI;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;

public class DatasetRegistryFactoryImplTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void listAndCreateSupportedRegistries() throws DatasetRegistryException, IOException {
        File tmpDir = folder.newFolder("tmp");

        List<DatasetRegistry> registries = new ArrayList<>();
        Set<String> supportedRegistries = DatasetRegistryFactoryImpl.getSupportedRegistries();
        for (String supportedRegistry : supportedRegistries) {
            DatasetRegistry registry = new DatasetRegistryFactoryImpl(
                    tmpDir.toURI(),
                    in -> in,
                    "someDataDir",
                    "someProvDir",
                    getListener(),
                    getCtx(),
                    getActivityIdFactory(),
                    null,
                    HashType.sha256).createRegistryByName(supportedRegistry);
            registries.add(registry);
        }
        assertThat(registries.size(), Is.is(supportedRegistries.size()));
    }

    private Supplier<IRI> getActivityIdFactory() {
        return new Supplier<IRI>() {
            @Override
            public IRI get() {
                return null;
            }
        };
    }

    private ActivityContext getCtx() {
        return new ActivityContext() {
            @Override
            public IRI getActivity() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }
        };
    }

    private ActivityListener getListener() {
        return new ActivityListener() {

            @Override
            public void onStarted(IRI parentActivityId, IRI activityId, IRI request) {

            }

            @Override
            public void onCompleted(IRI parentActivityId, IRI activityId, IRI request, IRI response, URI localPathOfResponseData) {

            }
        };
    }

}