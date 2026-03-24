package org.globalbioticinteractions.dataset;

import org.eol.globi.service.ResourceService;
import org.eol.globi.util.InputStreamFactoryNoop;
import org.eol.globi.util.ResourceServiceHTTP;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

public class DatasetRegistryChecklistBankIT  {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void datasetIds() throws IOException, DatasetRegistryException {
        ResourceService resourceService
                = new ResourceServiceHTTP(new InputStreamFactoryNoop(), folder.newFolder());

        DatasetRegistryChecklistBank checklistBank
                = new DatasetRegistryChecklistBank(resourceService);

        checklistBank.setBatchSize(10);

        ArrayList<String> ids = new ArrayList<>();

        checklistBank.findNamespaces(ids::add);

        assertThat(ids.size(), greaterThan(0));

        assertThat(ids, hasItem("urn:lsid:checklistbank.org:dataset:1049"));
        assertThat(ids, hasItem("urn:lsid:checklistbank.org:dataset:2017"));
    }

}