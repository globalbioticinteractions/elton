package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

public class CmdLogTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void logTemplate() throws URISyntaxException, IOException {
        CmdLog cmd = new CmdLog();
        String dataDir = CmdTestUtil.cacheDirTest(folder);
        cmd.setDataDir(dataDir);
        cmd.setProvDir(dataDir);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);

        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));

        cmd.run();

        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));

        String provenanceLog = new String(out1.toByteArray(), StandardCharsets.UTF_8);
        String archiveTypeValue = "application/globi";
        String archiveVersionStatementPart = "<http://purl.org/pav/hasVersion> <hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f>";
        assertThat(provenanceLog, containsString(archiveTypeValue));

        String archiveTypeStatement = Arrays.stream(provenanceLog.split("\n"))
                .filter(line -> StringUtils.contains(line, archiveTypeValue))
                .map(StringUtils::reverse)
                .findFirst()
                .orElse("");

        String archiveVersionStatement = Arrays.stream(provenanceLog.split("\n"))
                .filter(line -> StringUtils.contains(line, archiveVersionStatementPart))
                .map(StringUtils::reverse)
                .findFirst()
                .orElse("");

        String commonSuffix = StringUtils.reverse(StringUtils.getCommonPrefix(archiveTypeStatement, archiveVersionStatement));

        assertThat(commonSuffix, is(not("> .")));
        assertThat(commonSuffix.length(), is(50));

        int positionOfArchiveTypeStatement = provenanceLog.indexOf(archiveTypeValue);
        int positionOfArchiveVersionStatement = provenanceLog.indexOf
                (archiveVersionStatementPart);

        assertThat(positionOfArchiveTypeStatement, is(lessThan(positionOfArchiveVersionStatement)));

        List<String> split = toVersionStatements(provenanceLog);
        assertThat(split.size(), is(3));
        assertThat(split.get(0), startsWith("<https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <http://purl.org/pav/hasVersion> <hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f> "));
        assertThat(split.get(1), startsWith("<zip:hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f!/globalbioticinteractions-template-dataset-e68f448/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80> "));
        assertThat(split.get(2), startsWith("<zip:hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f!/globalbioticinteractions-template-dataset-e68f448/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha256/c1b37add5ee5f30916f19811c59c2960e3b68ecf1a3846afe1776014c4c96271> "));

    }

    @Test
    public void logRSSTemplate() throws URISyntaxException, IOException {
        String cacheDir = folder.newFolder().getAbsolutePath();
        CmdRegistry pull = new CmdUpdate();
        pull.setDataDir(cacheDir);
        pull.setProvDir(cacheDir);
        pull.setWorkDir(new File(getClass().getResource("/dataset-rss-cache/globi.json").toURI()).getParent());
        pull.setRegistryNames(Collections.singletonList(DatasetRegistryUtil.NAMESPACE_LOCAL));
        pull.run();

        CmdLog cmd = new CmdLog();
        cmd.setDataDir(cacheDir);
        cmd.setProvDir(cacheDir);
        cmd.setNamespaces(Collections.singletonList(DatasetRegistryUtil.NAMESPACE_LOCAL));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);
        int before = CmdTestUtil.numberOfDataFiles(cmd.getDataDir());
        cmd.run();
        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(before));


        List<String> split = toVersionStatements(out1.toString());


        assertThat(split.size(), is(12));
        assertThat(split.get(0), containsString("/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/fda47d72a95c91aa37e77ae8486db5364b8fc22ec3b56aa970d5dd9665b69d5c> "));
        assertThat(split.get(1), containsString("/rss.xml> <http://purl.org/pav/hasVersion> <hash://sha256/6b3795feff24580fa482a6037a3baab3ff95ddf8f97045bb9fa17baa12240ca6> "));
        assertThat(split.get(2), containsString("/rss.xml> <http://purl.org/pav/hasVersion> <hash://sha256/6b3795feff24580fa482a6037a3baab3ff95ddf8f97045bb9fa17baa12240ca6> "));
        assertThat(split.get(3), containsString("<classpath:/org/globalbioticinteractions/interaction_types_ignored.csv> <http://purl.org/pav/hasVersion> <hash://sha256/56616ab980d9b7dadd8c1d4102ab498d7f27ec710956429ed62c1964cb6c3652>"));
        assertThat(split.get(4), containsString("<classpath:/org/globalbioticinteractions/interaction_types_mapping.csv> <http://purl.org/pav/hasVersion> <hash://sha256/24f612c59b4a9ff0bc2bce8c773d10d3b61b6a23e48e8a86e24cc036d44a7dc4>"));
        assertThat(split.get(5), containsString("<classpath:/org/globalbioticinteractions/interaction_types_ro_unmapped.csv> <http://purl.org/pav/hasVersion> <hash://sha256/43aba7b90c686a4890aebd4a90a02d6f82259664524bdad1b22102a29fe9fa07>"));
        assertThat(split.get(6), containsString("<classpath:/org/globalbioticinteractions/interaction_types_ro.csv> <http://purl.org/pav/hasVersion> <hash://sha256/99c73166061bb898fa6624a594abd4225d5e23084350428dcd01cacff5742a6f>"));
        assertThat(split.get(split.size() - 1), containsString("/dwca.zip> <http://purl.org/pav/hasVersion> <hash://sha256/ab4b32fc3bb81c6ec0e16f42dcc284e3725274501219f704af7022221db64abe> "));

    }

    @Test
    public void logTemplateSha1() throws URISyntaxException, IOException {
        CmdLog cmd = new CmdLog();
        cmd.setHashType(HashType.sha1);
        String dataDir = CmdTestUtil.cacheDirTest(folder);
        cmd.setDataDir(dataDir);
        cmd.setProvDir(dataDir);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);

        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));

        cmd.run();

        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));

        List<String> split = toVersionStatements(out1.toString());

        assertThat(split.size(), is(3));
        assertThat(split.get(0), startsWith("<https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <http://purl.org/pav/hasVersion> <hash://sha1/1bffa147ccca290482329e42a4f7d4c5db5f1d04> "));
        assertThat(split.get(1), startsWith("<zip:hash://sha1/1bffa147ccca290482329e42a4f7d4c5db5f1d04!/globalbioticinteractions-template-dataset-e68f448/globi.json> <http://purl.org/pav/hasVersion> <hash://sha1/bec707471bcd75ebb69ae4b2a155ff64cfe7221a> "));
        assertThat(split.get(2), startsWith("<zip:hash://sha1/1bffa147ccca290482329e42a4f7d4c5db5f1d04!/globalbioticinteractions-template-dataset-e68f448/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha1/ee1b4b58d9c356d5ef20e4076b929094516beb35> "));
    }

    @Test
    public void logNHM() throws URISyntaxException, IOException {
        CmdLog cmd = new CmdLog();
        cmd.setHashType(HashType.sha256);
        String name = "/dataset-cache-nhm/globalbioticinteractions/natural-history-museum-london-interactions-bank/access.tsv";
        String dataStatic = CmdTestUtil.cacheDirTestFor(name);
        File tmpDir = folder.newFolder();
        FileUtils.copyDirectory(new File(dataStatic), tmpDir);
        String dataDir = tmpDir.getAbsolutePath();
        cmd.setDataDir(dataDir);
        cmd.setProvDir(dataDir);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/natural-history-museum-london-interactions-bank"));
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);

        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(9));

        cmd.run();

        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(9));

        String prov = out1.toString();
        List<String> split = toVersionStatements(prov);

        assertThat(prov, containsString("http://data.nhm.ac.uk/api/3/action/package_show?id=nhm-ib"));


        assertThat(split.size(), is(9));
        assertThat(split.get(0), startsWith("<https://github.com/globalbioticinteractions/natural-history-museum-london-interactions-bank/archive/bfde02e6cc980a2ac411952c862ce97c2e99e057.zip> <http://purl.org/pav/hasVersion> <hash://sha256/b15d553f4eb91797a7ca017d315158b3f48921a4c605be422d14598c712fe88c> "));
        assertThat(split.get(1), startsWith("<zip:hash://sha256/b15d553f4eb91797a7ca017d315158b3f48921a4c605be422d14598c712fe88c!/natural-history-museum-london-interactions-bank-bfde02e6cc980a2ac411952c862ce97c2e99e057/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/1e24b00158a3f0cb2d39d37134ad8f8f3566f85d36341f2c04242c995fb6a69a> "));
        assertThat(split.get(2), startsWith("<http://data.nhm.ac.uk/api/3/action/package_show?id=nhm-ib> <http://purl.org/pav/hasVersion> <hash://sha256/001ea8478d7f3ae2d9d98e46564d56fa656ae4d1fffe2e1da9f9800fc49ca83a> "));
    }

    @Test
    public void logTemplateMD5() throws URISyntaxException, IOException {
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        CmdLog cmd = new CmdLog();
        cmd.setHashType(HashType.md5);
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);
        String dataDir = CmdTestUtil.cacheDirTest(folder);
        cmd.setDataDir(dataDir);
        cmd.setProvDir(dataDir);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));

        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));

        cmd.run();

        assertThat(CmdTestUtil.numberOfDataFiles(cmd.getDataDir()), Is.is(4));

        List<String> versionStatements = toVersionStatements(out1.toString());

        assertThat(versionStatements.size(), is(3));
        assertThat(versionStatements.get(2), startsWith("<zip:hash://md5/98ea358786947a5c3217a12a0810ddea!/globalbioticinteractions-template-dataset-e68f448/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://md5/7a53de3ea4bde18126a32f2f95b56843> "));
        assertThat(versionStatements.get(1), startsWith("<zip:hash://md5/98ea358786947a5c3217a12a0810ddea!/globalbioticinteractions-template-dataset-e68f448/globi.json> <http://purl.org/pav/hasVersion> <hash://md5/5d4fa61630858b39ddf527f390883487>"));
        assertThat(versionStatements.get(0), startsWith("<https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <http://purl.org/pav/hasVersion> <hash://md5/98ea358786947a5c3217a12a0810ddea>"));
    }

    private List<String> toVersionStatements(String s) {
        String[] split = s.split("\n");
        return Stream.of(split)
                .filter(x -> StringUtils.contains(x, RefNodeConstants.HAS_VERSION.getIRIString()))
                .collect(Collectors.toList());
    }

}