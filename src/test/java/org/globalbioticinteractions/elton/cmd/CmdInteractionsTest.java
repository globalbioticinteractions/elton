package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class CmdInteractionsTest {

    @Test
    public void interactionsNoHeader() throws URISyntaxException {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("interactions",
                "--cache-dir=" + CmdTestUtil.cacheDirTest(),
                "--skip-header",
                "globalbioticinteractions/template-dataset");

        Assert.assertEquals(jc.getParsedCommand(), "interactions");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdInteractions.class);
        CmdInteractions cmdInteractions = (CmdInteractions) actual.getObjects().get(0);
        assertThat(cmdInteractions.getNamespaces(), hasItem("globalbioticinteractions/template-dataset"));

        if (actual.getObjects().get(0) instanceof Runnable) {
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(out1);
            ((CmdInteractions) actual.getObjects().get(0)).run(out);
            assertThat(out1.toString().split("\n")[0], is("https://en.wiktionary.org/wiki/support\t\t\t\t\t\t\tLeptoconchus incycloseris\t\t\t\t\t\t\t\t\t\t\thttp://purl.obolibrary.org/obo/RO_0002444\tparasiteOf\t\t\t\t\t\t\tFungia (Cycloseris) costulata\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t10.1007/s13127-011-0039-1\thttps://doi.org/10.1007/s13127-011-0039-1\tGittenberger, A., Gittenberger, E. (2011). Cryptic, adaptive radiation of endoparasitic snails: sibling species of Leptoconchus (Gastropoda: Coralliophilidae) in corals. Org Divers Evol, 11(1), 21–41. doi:10.1007/s13127-011-0039-1\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
        }
    }

    @Ignore
    @Test
    public void interactionsLocalWithCache() throws URISyntaxException {
        JCommander jc = new CmdLine().buildCommander();
        URL resource = getClass().getResource("/dataset-local-with-cache/data/local/access.tsv");
        File dataDir = new File(resource.toURI()).getParentFile().getParentFile();
        jc.parse("interactions",
                "--cache-dir=" + dataDir.getAbsolutePath(),
                "--work-dir=" + dataDir.getParentFile().getAbsolutePath(),
                "--skip-header");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdInteractions.class);

        if (actual.getObjects().get(0) instanceof Runnable) {
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(out1);
            ((CmdInteractions) actual.getObjects().get(0)).run(out);
            assertThat(out1.toString().split("\n")[0], is("https://en.wiktionary.org/wiki/support\t\t\t\t\t\t\tLeptoconchus incycloseris\t\t\t\t\t\t\t\t\t\t\thttp://purl.obolibrary.org/obo/RO_0002444\tparasiteOf\t\t\t\t\t\t\tFungia (Cycloseris) costulata\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t10.1007/s13127-011-0039-1\thttps://doi.org/10.1007/s13127-011-0039-1\tGittenberger, A., Gittenberger, E. (2011). Cryptic, adaptive radiation of endoparasitic snails: sibling species of Leptoconchus (Gastropoda: Coralliophilidae) in corals. Org Divers Evol, 11(1), 21–41. doi:10.1007/s13127-011-0039-1\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
        }
    }

    @Test
    public void interactionsWithHeader() throws URISyntaxException {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("interactions",
                "--cache-dir=" + CmdTestUtil.cacheDirTest(),
                "globalbioticinteractions/template-dataset");
        JCommander actual = jc.getCommands().get(jc.getParsedCommand());

        if (actual.getObjects().get(0) instanceof Runnable) {
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(out1);
            ((CmdInteractions) actual.getObjects().get(0)).run(out);
            String actual1 = out1.toString();
            String[] lines = StringUtils.splitByWholeSeparator(actual1, "\n");
            assertThat(lines[0], is("argumentTypeId\tsourceOccurrenceId\tsourceCatalogNumber\tsourceCollectionCode\tsourceCollectionId\tsourceInstitutionCode\tsourceTaxonId\tsourceTaxonName\tsourceTaxonRank\tsourceTaxonPathIds\tsourceTaxonPath\tsourceTaxonPathNames\tsourceBodyPartId\tsourceBodyPartName\tsourceLifeStageId\tsourceLifeStageName\tsourceSexId\tsourceSexName\tinteractionTypeId\tinteractionTypeName\ttargetOccurrenceId\ttargetCatalogNumber\ttargetCollectionCode\ttargetCollectionId\ttargetInstitutionCode\ttargetTaxonId\ttargetTaxonName\ttargetTaxonRank\ttargetTaxonPathIds\ttargetTaxonPath\ttargetTaxonPathNames\ttargetBodyPartId\ttargetBodyPartName\ttargetLifeStageId\ttargetLifeStageName\ttargetSexId\ttargetSexName\tbasisOfRecordId\tbasisOfRecordName\thttp://rs.tdwg.org/dwc/terms/eventDate\tdecimalLatitude\tdecimalLongitude\tlocalityId\tlocalityName\treferenceDoi\treferenceUrl\treferenceCitation\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion"));
            assertThat(lines[1], is("https://en.wiktionary.org/wiki/support\t\t\t\t\t\t\tLeptoconchus incycloseris\t\t\t\t\t\t\t\t\t\t\thttp://purl.obolibrary.org/obo/RO_0002444\tparasiteOf\t\t\t\t\t\t\tFungia (Cycloseris) costulata\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t10.1007/s13127-011-0039-1\thttps://doi.org/10.1007/s13127-011-0039-1\tGittenberger, A., Gittenberger, E. (2011). Cryptic, adaptive radiation of endoparasitic snails: sibling species of Leptoconchus (Gastropoda: Coralliophilidae) in corals. Org Divers Evol, 11(1), 21–41. doi:10.1007/s13127-011-0039-1\tglobalbioticinteractions/template-dataset\tJorrit H. Poelen. 2014. Species associations manually extracted from literature.\thttps://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip\t2017-09-19T17:01:39Z\t631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f\tdev"));
            assertThat(lines[0].split("\t").length, is(lines[1].split("\t").length));
        }
    }

    @Test
    public void refutingInteractionsWithHeader() throws URISyntaxException {
        URL resource = getClass().getResource("/dataset-local-refuting-test/interactions.tsv");
        assertNotNull(resource);
        File file = new File(resource.toURI());
        File workDir = file.getParentFile();

        JCommander jc = new CmdLine().buildCommander();
        jc.parse("interactions",
                "--cache-dir=" + CmdTestUtil.cacheDirTestFor("/dataset-cache-null/empty/empty/access.tsv"));
        JCommander actual = jc.getCommands().get(jc.getParsedCommand());

        if (actual.getObjects().get(0) instanceof Runnable) {
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(out1);
            ((CmdInteractions) actual.getObjects().get(0)).setWorkDir(workDir.getAbsolutePath());
            ((CmdInteractions) actual.getObjects().get(0)).run(out);
            String actual1 = out1.toString();
            String[] lines = StringUtils.splitByWholeSeparator(actual1, "\n");
            assertThat(lines.length, is(3));
            assertThat(lines[0], is("argumentTypeId\tsourceOccurrenceId\tsourceCatalogNumber\tsourceCollectionCode\tsourceCollectionId\tsourceInstitutionCode\tsourceTaxonId\tsourceTaxonName\tsourceTaxonRank\tsourceTaxonPathIds\tsourceTaxonPath\tsourceTaxonPathNames\tsourceBodyPartId\tsourceBodyPartName\tsourceLifeStageId\tsourceLifeStageName\tsourceSexId\tsourceSexName\tinteractionTypeId\tinteractionTypeName\ttargetOccurrenceId\ttargetCatalogNumber\ttargetCollectionCode\ttargetCollectionId\ttargetInstitutionCode\ttargetTaxonId\ttargetTaxonName\ttargetTaxonRank\ttargetTaxonPathIds\ttargetTaxonPath\ttargetTaxonPathNames\ttargetBodyPartId\ttargetBodyPartName\ttargetLifeStageId\ttargetLifeStageName\ttargetSexId\ttargetSexName\tbasisOfRecordId\tbasisOfRecordName\thttp://rs.tdwg.org/dwc/terms/eventDate\tdecimalLatitude\tdecimalLongitude\tlocalityId\tlocalityName\treferenceDoi\treferenceUrl\treferenceCitation\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion"));
            assertThat(lines[0].split("\t").length, is(lines[1].split("\t").length));
            assertThat(lines[1], startsWith("https://en.wiktionary.org/wiki/refute\t\t\t\t\t\t\tLeptoconchus incycloseris"));
        }
    }


}