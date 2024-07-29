package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeConstants;
import org.apache.commons.lang3.StringUtils;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CmdLogTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void logTemplate() throws URISyntaxException {
        CmdLog cmd = new CmdLog();
        cmd.setCacheDir(CmdTestUtil.cacheDirTest());
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);
        cmd.run();
        List<String> split = toVersionStatements(out1);
        assertThat(split.size(), is(3));
        assertThat(split.get(0), startsWith("<https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <http://purl.org/pav/hasVersion> <hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f> "));
        assertThat(split.get(1), startsWith("<zip:hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f!/globalbioticinteractions-template-dataset-e68f448/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80> "));
        assertThat(split.get(2), startsWith("<zip:hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f!/globalbioticinteractions-template-dataset-e68f448/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha256/c1b37add5ee5f30916f19811c59c2960e3b68ecf1a3846afe1776014c4c96271> "));

    }

    @Test
    public void logRSSTemplate() throws URISyntaxException, IOException {
        String cacheDir = folder.newFolder().getAbsolutePath();
        CmdUpdate pull = new CmdUpdate();
        pull.setCacheDir(cacheDir);
        pull.setWorkDir(new File(getClass().getResource("/dataset-rss-cache/globi.json").toURI()).getParent());
        pull.setRegistryNames(Collections.singletonList(DatasetRegistryUtil.NAMESPACE_LOCAL));
        pull.run();

        CmdLog cmd = new CmdLog();
        cmd.setCacheDir(cacheDir);
        cmd.setNamespaces(Collections.singletonList(DatasetRegistryUtil.NAMESPACE_LOCAL));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);
        cmd.run();

        List<String> split = toVersionStatements(out1);


        assertThat(split.size(), is(4));
        assertThat(split.get(0), containsString("/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/fda47d72a95c91aa37e77ae8486db5364b8fc22ec3b56aa970d5dd9665b69d5c> "));
        assertThat(split.get(1), containsString("/rss.xml> <http://purl.org/pav/hasVersion> <hash://sha256/6b3795feff24580fa482a6037a3baab3ff95ddf8f97045bb9fa17baa12240ca6> "));
        assertThat(split.get(split.size() - 1), containsString("/dwca.zip> <http://purl.org/pav/hasVersion> <hash://sha256/ab4b32fc3bb81c6ec0e16f42dcc284e3725274501219f704af7022221db64abe> "));

    }

    @Test
    public void logTemplateSha1() throws URISyntaxException {
        CmdLog cmd = new CmdLog();
        cmd.setHashType(HashType.sha1);
        cmd.setCacheDir(CmdTestUtil.cacheDirTest());
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);
        cmd.run();

        List<String> split = toVersionStatements(out1);

        assertThat(split.size(), is(3));
        assertThat(split.get(0), startsWith("<https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <http://purl.org/pav/hasVersion> <hash://sha1/1bffa147ccca290482329e42a4f7d4c5db5f1d04> "));
        assertThat(split.get(1), startsWith("<zip:hash://sha1/1bffa147ccca290482329e42a4f7d4c5db5f1d04!/globalbioticinteractions-template-dataset-e68f448/globi.json> <http://purl.org/pav/hasVersion> <hash://sha1/bec707471bcd75ebb69ae4b2a155ff64cfe7221a> "));
        assertThat(split.get(2), startsWith("<zip:hash://sha1/1bffa147ccca290482329e42a4f7d4c5db5f1d04!/globalbioticinteractions-template-dataset-e68f448/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha1/ee1b4b58d9c356d5ef20e4076b929094516beb35> "));
    }

    @Test
    public void logTemplateMD5() throws URISyntaxException {
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        CmdLog cmd = new CmdLog();
        cmd.setHashType(HashType.md5);
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);
        cmd.setCacheDir(CmdTestUtil.cacheDirTest());
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        cmd.run();
        List<String> versionStatements = toVersionStatements(out1);

        assertThat(versionStatements.size(), is(3));
        assertThat(versionStatements.get(2), startsWith("<zip:hash://md5/98ea358786947a5c3217a12a0810ddea!/globalbioticinteractions-template-dataset-e68f448/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://md5/7a53de3ea4bde18126a32f2f95b56843> "));
        assertThat(versionStatements.get(1), startsWith("<zip:hash://md5/98ea358786947a5c3217a12a0810ddea!/globalbioticinteractions-template-dataset-e68f448/globi.json> <http://purl.org/pav/hasVersion> <hash://md5/5d4fa61630858b39ddf527f390883487>"));
        assertThat(versionStatements.get(0), startsWith("<https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <http://purl.org/pav/hasVersion> <hash://md5/98ea358786947a5c3217a12a0810ddea>"));
    }

    private List<String> toVersionStatements(ByteArrayOutputStream out1) {
        String[] split = out1.toString().split("\n");
        return Stream.of(split)
                .filter(x -> StringUtils.contains(x, RefNodeConstants.HAS_VERSION.getIRIString()))
                .collect(Collectors.toList());
    }

}