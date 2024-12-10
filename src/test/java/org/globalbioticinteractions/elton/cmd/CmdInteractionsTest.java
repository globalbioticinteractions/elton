package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.core.Is;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertNotNull;

public class CmdInteractionsTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void interactionsNoHeader() throws URISyntaxException {
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);

        CmdInteractions cmd = new CmdInteractions();
        cmd.setDataDir(CmdTestUtil.cacheDirTest());
        cmd.setProvDir(CmdTestUtil.cacheDirTest());
        cmd.setSkipHeader(true);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        cmd.run(out);
        assertThat(out1.toString().split("\n")[0], is("https://en.wiktionary.org/wiki/support\t\t\t\t\t\t\tLeptoconchus incycloseris\t\t\t\t\t\t\t\t\t\t\thttp://purl.obolibrary.org/obo/RO_0002444\tparasiteOf\t\t\t\t\t\t\tFungia (Cycloseris) costulata\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t10.1007/s13127-011-0039-1\thttps://doi.org/10.1007/s13127-011-0039-1\tGittenberger, A., Gittenberger, E. (2011). Cryptic, adaptive radiation of endoparasitic snails: sibling species of Leptoconchus (Gastropoda: Coralliophilidae) in corals. Org Divers Evol, 11(1), 21–41. doi:10.1007/s13127-011-0039-1\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
    }

    @Test
    public void interactionsNoHeaderSeparateProvDir() throws URISyntaxException {
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);

        CmdInteractions cmd = new CmdInteractions();
        String name = "/dataset-prov-data-test/prov/globalbioticinteractions/template-dataset/access.tsv";
        URL accessURL = CmdNamesTest.class.getResource(name);
        assertThat(accessURL, Is.is(notNullValue()));
        File baseDir = new File(accessURL.toURI()).getParentFile().getParentFile().getParentFile().getParentFile();
        String provDir = new File(baseDir, "prov").getAbsolutePath();
        String dataDir = new File(baseDir, "data").getAbsolutePath();

        assertThat(new File(dataDir + "/globalbioticinteractions/template-dataset", "631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f").exists(), is(true));
        assertThat(new File(provDir + "/globalbioticinteractions/template-dataset", "access.tsv").exists(), is(true));

        cmd.setDataDir(dataDir);
        cmd.setProvDir(provDir);
        cmd.setSkipHeader(true);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        cmd.run(out);
        assertThat(out1.toString().split("\n")[0], is("https://en.wiktionary.org/wiki/support\t\t\t\t\t\t\tLeptoconchus incycloseris\t\t\t\t\t\t\t\t\t\t\thttp://purl.obolibrary.org/obo/RO_0002444\tparasiteOf\t\t\t\t\t\t\tFungia (Cycloseris) costulata\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t10.1007/s13127-011-0039-1\thttps://doi.org/10.1007/s13127-011-0039-1\tGittenberger, A., Gittenberger, E. (2011). Cryptic, adaptive radiation of endoparasitic snails: sibling species of Leptoconchus (Gastropoda: Coralliophilidae) in corals. Org Divers Evol, 11(1), 21–41. doi:10.1007/s13127-011-0039-1\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
    }

    @Test
    public void interactionsLocalWithCache() throws URISyntaxException {
        URL resource = getClass().getResource("/dataset-local-with-cache/data/local/access.tsv");
        File dataDir = new File(resource.toURI()).getParentFile().getParentFile();

        CmdInteractions cmd = new CmdInteractions();
        cmd.setDataDir(dataDir.getAbsolutePath());
        cmd.setProvDir(dataDir.getAbsolutePath());
        cmd.setWorkDir(dataDir.getParentFile().getAbsolutePath());
        cmd.setSkipHeader(true);

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        assertThat(out1.toString().split("\n")[0], is("https://en.wiktionary.org/wiki/support\t\t\t\t\t\t\tLeptoconchus incycloseris\t\t\t\t\t\t\t\t\t\t\thttp://purl.obolibrary.org/obo/RO_0002444\tparasiteOf\t\t\t\t\t\t\tFungia (Cycloseris) costulata\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t10.1007/s13127-011-0039-1\thttps://doi.org/10.1007/s13127-011-0039-1\tGittenberger, A., Gittenberger, E. (2011). Cryptic, adaptive radiation of endoparasitic snails: sibling species of Leptoconchus (Gastropoda: Coralliophilidae) in corals. Org Divers Evol, 11(1), 21–41. doi:10.1007/s13127-011-0039-1\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));

    }

    @Test
    public void interactionsWithHeader() throws URISyntaxException {
        CmdInteractions cmd = new CmdInteractions();
        cmd.setDataDir(CmdTestUtil.cacheDirTest());
        cmd.setProvDir(CmdTestUtil.cacheDirTest());
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        String actual1 = out1.toString();
        String[] lines = StringUtils.splitByWholeSeparator(actual1, "\n");
        assertThat(lines[0], is("argumentTypeId\tsourceOccurrenceId\tsourceCatalogNumber\tsourceCollectionCode\tsourceCollectionId\tsourceInstitutionCode\tsourceTaxonId\tsourceTaxonName\tsourceTaxonRank\tsourceTaxonPathIds\tsourceTaxonPath\tsourceTaxonPathNames\tsourceBodyPartId\tsourceBodyPartName\tsourceLifeStageId\tsourceLifeStageName\tsourceSexId\tsourceSexName\tinteractionTypeId\tinteractionTypeName\ttargetOccurrenceId\ttargetCatalogNumber\ttargetCollectionCode\ttargetCollectionId\ttargetInstitutionCode\ttargetTaxonId\ttargetTaxonName\ttargetTaxonRank\ttargetTaxonPathIds\ttargetTaxonPath\ttargetTaxonPathNames\ttargetBodyPartId\ttargetBodyPartName\ttargetLifeStageId\ttargetLifeStageName\ttargetSexId\ttargetSexName\tbasisOfRecordId\tbasisOfRecordName\thttp://rs.tdwg.org/dwc/terms/eventDate\tdecimalLatitude\tdecimalLongitude\tlocalityId\tlocalityName\treferenceDoi\treferenceUrl\treferenceCitation\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion"));
        assertThat(lines[1], is("https://en.wiktionary.org/wiki/support\t\t\t\t\t\t\tLeptoconchus incycloseris\t\t\t\t\t\t\t\t\t\t\thttp://purl.obolibrary.org/obo/RO_0002444\tparasiteOf\t\t\t\t\t\t\tFungia (Cycloseris) costulata\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t10.1007/s13127-011-0039-1\thttps://doi.org/10.1007/s13127-011-0039-1\tGittenberger, A., Gittenberger, E. (2011). Cryptic, adaptive radiation of endoparasitic snails: sibling species of Leptoconchus (Gastropoda: Coralliophilidae) in corals. Org Divers Evol, 11(1), 21–41. doi:10.1007/s13127-011-0039-1\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
        assertThat(lines[0].split("\t").length, is(lines[1].split("\t").length));
    }


    @Test
    public void refutingInteractionsWithHeader() throws URISyntaxException, IOException {
        URL resource = getClass().getResource("/dataset-local-refuting-test/interactions.tsv");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        File workDir = file.getParentFile();

        CmdInteractions cmd = new CmdInteractions();
        String cacheDir = tmpFolder.newFolder().getAbsolutePath();
        cmd.setDataDir(cacheDir);
        cmd.setProvDir(cacheDir);
        cmd.setWorkDir(workDir.getAbsolutePath());
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));


        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        String actual1 = out1.toString();
        String[] lines = StringUtils.splitByWholeSeparator(actual1, "\n");
        assertThat(lines.length, is(3));
        assertThat(lines[0], is("argumentTypeId\tsourceOccurrenceId\tsourceCatalogNumber\tsourceCollectionCode\tsourceCollectionId\tsourceInstitutionCode\tsourceTaxonId\tsourceTaxonName\tsourceTaxonRank\tsourceTaxonPathIds\tsourceTaxonPath\tsourceTaxonPathNames\tsourceBodyPartId\tsourceBodyPartName\tsourceLifeStageId\tsourceLifeStageName\tsourceSexId\tsourceSexName\tinteractionTypeId\tinteractionTypeName\ttargetOccurrenceId\ttargetCatalogNumber\ttargetCollectionCode\ttargetCollectionId\ttargetInstitutionCode\ttargetTaxonId\ttargetTaxonName\ttargetTaxonRank\ttargetTaxonPathIds\ttargetTaxonPath\ttargetTaxonPathNames\ttargetBodyPartId\ttargetBodyPartName\ttargetLifeStageId\ttargetLifeStageName\ttargetSexId\ttargetSexName\tbasisOfRecordId\tbasisOfRecordName\thttp://rs.tdwg.org/dwc/terms/eventDate\tdecimalLatitude\tdecimalLongitude\tlocalityId\tlocalityName\treferenceDoi\treferenceUrl\treferenceCitation\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion"));
        assertThat(lines[0].split("\t").length, is(lines[1].split("\t").length));
        assertThat(lines[1], startsWith("https://en.wiktionary.org/wiki/refute\t\t\t\t\t\t\tLeptoconchus incycloseris"));
    }


}