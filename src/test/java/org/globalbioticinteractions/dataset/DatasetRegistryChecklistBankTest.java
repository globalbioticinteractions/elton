package org.globalbioticinteractions.dataset;

import org.eol.globi.service.ResourceService;
import org.eol.globi.util.InputStreamFactoryNoop;
import org.eol.globi.util.ResourceServiceHTTP;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.fail;

public class DatasetRegistryChecklistBankTest {

    @Test
    public void listDatasets() throws DatasetRegistryException {
        Map<String, String> requestResponse = new TreeMap<String, String>() {{
            put("https://api.checklistbank.org/dataset?origin=external&origin=project&rowType=col%3ASpeciesInteraction&limit=5&offset=0","checklistbank-list-page1.json");
            put("https://api.checklistbank.org/dataset?origin=external&origin=project&rowType=col%3ASpeciesInteraction&limit=5&offset=5","checklistbank-list-page2.json");
            put("https://api.checklistbank.org/dataset?origin=external&origin=project&rowType=col%3ASpeciesInteraction&limit=5&offset=10","checklistbank-list-page3.json");
        }};

        ResourceService resourceService
                = new ResourceService() {
            @Override
            public InputStream retrieve(URI uri) throws IOException {
                if (requestResponse.containsKey(uri.toString())) {
                    String resource = requestResponse.get(uri.toString());
                    requestResponse.remove(uri.toString());
                    return getClass().getResourceAsStream(resource);
                } else {
                    throw new IOException("unexpected request [" + uri.toString() + "]");
                }
            }
        };

        DatasetRegistryChecklistBank checklistBank
                = new DatasetRegistryChecklistBank(resourceService);
        checklistBank.setBatchSize(5);

        Iterable<String> datasetIds = checklistBank.findNamespaces();

        assertThat(datasetIds, hasItem("urn:lsid:checklistbank.org:dataset:1049"));

        assertThat(StreamSupport.stream(datasetIds.spliterator(), false).count(), is(11L));



    }

    @Test
    public void datasetsPage1() throws DatasetRegistryException {
        InputStream resourceAsStream = getClass().getResourceAsStream("checklistbank-list-page1.json");
        Collection<String> refs = DatasetRegistryChecklistBank.getDatasetIds(resourceAsStream);
        assertThat(refs.size(), is(5));
        assertThat(refs, hasItem("urn:lsid:checklistbank.org:dataset:1049"));
        assertThat(refs, not(hasItem("urn:lsid:checklistbank.org:dataset:2017")));
    }

    @Test
    public void datasetsPage2() throws DatasetRegistryException {
        InputStream resourceAsStream = getClass().getResourceAsStream("checklistbank-list-page2.json");
        Collection<String> refs = DatasetRegistryChecklistBank.getDatasetIds(resourceAsStream);
        assertThat(refs.size(), is(5));
        assertThat(refs, not(hasItem("urn:lsid:checklistbank.org:dataset:1049")));
        assertThat(refs, hasItem("urn:lsid:checklistbank.org:dataset:2017"));
    }

    @Test
    public void datasetsPage3() throws DatasetRegistryException {
        InputStream resourceAsStream = getClass().getResourceAsStream("checklistbank-list-page3.json");
        Collection<String> refs = DatasetRegistryChecklistBank.getDatasetIds(resourceAsStream);
        assertThat(refs.size(), is(1));
        assertThat(refs, hasItem("urn:lsid:checklistbank.org:dataset:265709"));
        assertThat(refs, not(hasItem("urn:lsid:checklistbank.org:dataset:2017")));
    }


}