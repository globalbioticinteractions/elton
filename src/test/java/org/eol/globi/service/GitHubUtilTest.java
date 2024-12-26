package org.eol.globi.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class GitHubUtilTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void isGloBIRepo() throws IOException {
        String repo = GitHubUtilIT.TEMPLATE_DATA_REPOSITORY_TSV;

        ResourceService resourceService = getResourceService();

        String lastCommitSHA = GitHubUtil.lastCommitSHA(
                repo,
                resourceService
        );

        assertThat(
                GitHubUtil.isGloBIRepository(repo, lastCommitSHA, resourceService),
                is(true)
        );
    }

    @Test
    public void nonGloBIRepo() throws IOException {
        String repo = "ropensci/rgbif";

        ResourceService resourceService = getResourceService();

        String lastCommitSHA = GitHubUtil.lastCommitSHA(
                repo,
                resourceService
        );

        assertThat(GitHubUtil.isGloBIRepository(repo, lastCommitSHA, resourceService),
                is(false));
    }

    private ResourceService getResourceService() throws IOException {


        Map<String, String> lookup = new TreeMap<String, String>() {{
            put("https://raw.githubusercontent.com/globalbioticinteractions/template-dataset/b92cd44dcba945c760229a14d3b9becb2dd0c147/globi.json", "template-dataset-globi.json");
            put("https://github.com/globalbioticinteractions/template-dataset/info/refs?service=git-upload-pack", "template-dataset-git-upload-pack.bin");
            put("https://github.com/ropensci/rgbif/info/refs?service=git-upload-pack", "ropensci-rgbif-git-upload-pack.bin");
        }};

        return new ResourceService() {
            @Override
            public InputStream retrieve(URI uri) throws IOException {
                String resourceName = lookup.get(uri.toString());
                return getClass().getResourceAsStream(resourceName);
            }
        };
    }

}
