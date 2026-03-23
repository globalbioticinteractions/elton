package org.globalbioticinteractions.dataset;

import org.junit.Test;

import java.io.InputStream;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

public class DatasetRegistryChecklistBankTest {

    @Test
    public void datasetsPage1() throws DatasetRegistryException {
        InputStream resourceAsStream = getClass().getResourceAsStream("checklistbank-list-page1.json");
        Collection<String> refs = DatasetRegistryChecklistBank.getRelations(resourceAsStream);
        assertThat(refs.size(), is(5));
        assertThat(refs, hasItem("urn:lsid:checklistbank.org:dataset:1049"));
        assertThat(refs, not(hasItem("urn:lsid:checklistbank.org:dataset:2017")));
    }

    @Test
    public void datasetsPage2() throws DatasetRegistryException {
        InputStream resourceAsStream = getClass().getResourceAsStream("checklistbank-list-page2.json");
        Collection<String> refs = DatasetRegistryChecklistBank.getRelations(resourceAsStream);
        assertThat(refs.size(), is(5));
        assertThat(refs, not(hasItem("urn:lsid:checklistbank.org:dataset:1049")));
        assertThat(refs, hasItem("urn:lsid:checklistbank.org:dataset:2017"));
    }

    @Test
    public void datasetsPage3() throws DatasetRegistryException {
        InputStream resourceAsStream = getClass().getResourceAsStream("checklistbank-list-page3.json");
        Collection<String> refs = DatasetRegistryChecklistBank.getRelations(resourceAsStream);
        assertThat(refs.size(), is(1));
        assertThat(refs, hasItem("urn:lsid:checklistbank.org:dataset:265709"));
        assertThat(refs, not(hasItem("urn:lsid:checklistbank.org:dataset:2017")));
    }


}