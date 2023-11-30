package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CmdLogTest {

    @Test
    public void logTemplate() throws URISyntaxException {
        CmdLog cmd = new CmdLog();
        cmd.setCacheDir(CmdTestUtil.cacheDirTest());
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        String[] split = out1.toString().split("\n");
        assertThat(split.length, is(3));
        assertThat(split[0], is("<https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <http://purl.org/pav/hasVersion> <hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f> ."));
        assertThat(split[1], is("<zip:hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f!/globalbioticinteractions-template-dataset-e68f448/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80> ."));
        assertThat(split[2], is("<zip:hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f!/globalbioticinteractions-template-dataset-e68f448/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha256/c1b37add5ee5f30916f19811c59c2960e3b68ecf1a3846afe1776014c4c96271> ."));

    }

    @Test
    public void logTemplateSha1() throws URISyntaxException {
        CmdLog cmd = new CmdLog();
        cmd.setHashType(HashType.sha1);
        cmd.setCacheDir(CmdTestUtil.cacheDirTest());
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        String[] split = out1.toString().split("\n");
        assertThat(split.length, is(3));
        assertThat(split[0], is("<https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <http://purl.org/pav/hasVersion> <hash://sha1/1bffa147ccca290482329e42a4f7d4c5db5f1d04> ."));
        assertThat(split[1], is("<zip:hash://sha1/1bffa147ccca290482329e42a4f7d4c5db5f1d04!/globalbioticinteractions-template-dataset-e68f448/globi.json> <http://purl.org/pav/hasVersion> <hash://sha1/bec707471bcd75ebb69ae4b2a155ff64cfe7221a> ."));
        assertThat(split[2], is("<zip:hash://sha1/1bffa147ccca290482329e42a4f7d4c5db5f1d04!/globalbioticinteractions-template-dataset-e68f448/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha1/ee1b4b58d9c356d5ef20e4076b929094516beb35> ."));
    }

    @Test
    public void logTemplateMD5() throws URISyntaxException {
        CmdLog cmd = new CmdLog();
        cmd.setHashType(HashType.md5);
        cmd.setCacheDir(CmdTestUtil.cacheDirTest());
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        String[] split = out1.toString().split("\n");
        assertThat(split.length, is(3));
        assertThat(split[0], is("<https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <http://purl.org/pav/hasVersion> <hash://md5/98ea358786947a5c3217a12a0810ddea> ."));
        assertThat(split[1], is("<zip:hash://md5/98ea358786947a5c3217a12a0810ddea!/globalbioticinteractions-template-dataset-e68f448/globi.json> <http://purl.org/pav/hasVersion> <hash://md5/5d4fa61630858b39ddf527f390883487> ."));
        assertThat(split[2], is("<zip:hash://md5/98ea358786947a5c3217a12a0810ddea!/globalbioticinteractions-template-dataset-e68f448/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://md5/7a53de3ea4bde18126a32f2f95b56843> ."));
    }

}