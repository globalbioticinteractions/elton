package org.globalbioticinteractions.dataset;

import org.eol.globi.service.GitHubUtil;
import org.eol.globi.service.ResourceService;

import java.io.IOException;
import java.util.regex.Pattern;

public class DatasetRegistryGitHubArchive extends DatasetRegistryGitHub {

    public static final Pattern GITHUB_REPOSITORY_PATTERN = Pattern.compile("([^/]+/[^/]+)");

    public DatasetRegistryGitHubArchive(ResourceService resourceService) {
        super(resourceService);
    }

    @Override
    public Dataset datasetFor(String namespace) throws DatasetRegistryException {
        try {
            GitHubUtil.supportedNamespacePatternOrThrow(namespace);
            String commitSha = GitHubUtil.lastCommitSHA(namespace, getResourceService());
            return GitHubUtil.getArchiveDataset(namespace, commitSha, getResourceService());
        } catch (IOException e) {
            throw new DatasetRegistryException(e);
        }
    }

}
