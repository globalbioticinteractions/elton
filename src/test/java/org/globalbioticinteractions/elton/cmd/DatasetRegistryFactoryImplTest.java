package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.rdf.api.IRI;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;

public class DatasetRegistryFactoryImplTest {

    @Test
    public void list() throws DatasetRegistryException {
        List<DatasetRegistry> registries = new ArrayList<>();
        Set<String> supportedRegistries = DatasetRegistryFactoryImpl.getSupportedRegistries();
        for (String supportedRegistry : supportedRegistries) {
            DatasetRegistry registry = new DatasetRegistryFactoryImpl(
                    URI.create("some:uri"),
                    in -> in,
                    "someDataDir",
                    "someProvDir",
                    new ActivityListener() {

                        @Override
                        public void onStarted(UUID activityId, IRI request) {

                        }

                        @Override
                        public void onCompleted(UUID activityId, IRI request, IRI response, URI localPathOfResponseData) {

                        }
                    }).createRegistryByName(supportedRegistry);
            registries.add(registry);
        }
        assertThat(registries.size(), Is.is(supportedRegistries.size()));
    }

}