package org.globalbioticinteractions.dataset;

import org.eol.globi.util.ResourceServiceHTTP;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

public class DatasetRegistryGitHubArchiveTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test(expected = DatasetRegistryException.class)
    public void unsupportedNamespacePattern() throws IOException, DatasetRegistryException {
        DatasetRegistryGitHubArchive registry = new DatasetRegistryGitHubArchive(
                new ResourceServiceHTTP(is -> {
                    throw new IllegalArgumentException("kaboom!");
                }, folder.newFolder())
        );

        try {
            registry.datasetFor("urn:lsid:checklistbank.org:dataset:2017");
        } catch (DatasetRegistryException ex) {
            assertThat(ex.getMessage(), Is.is("unsupported namespace [urn:lsid:checklistbank.org:dataset:2017]"));
            throw ex;
        }
    }

    @Test(expected = DatasetRegistryException.class)
    public void supportedNamespace() throws IOException, DatasetRegistryException {
        DatasetRegistryGitHubArchive registry = new DatasetRegistryGitHubArchive(
                resourceName -> {
                    throw new IOException("kaboom!");
                });

        try {
            registry.datasetFor("some/namespace");
        } catch (DatasetRegistryException ex) {
            assertThat(ex.getCause().getMessage(), Is.is("kaboom!"));
            throw ex;
        }
    }

}