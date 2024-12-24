package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CmdNanoPubsTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void interactions() throws URISyntaxException, IOException {
        CmdNanoPubs cmd = new CmdNanoPubs();
        String dataDir = CmdTestUtil.cacheDirTest(tmpFolder);
        cmd.setDataDir(dataDir);
        cmd.setProvDir(dataDir);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));
        cmd.setIdGenerator(() -> "1");

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        String firstLine = "@prefix this: <http://purl.org/np/RAM--3NEBQBShWIe6LfGgwADTY4xDLL7-hIcxs7b5uI7o> ." +
                " @prefix sub: <http://purl.org/np/RAM--3NEBQBShWIe6LfGgwADTY4xDLL7-hIcxs7b5uI7o#> ." +
                " @prefix np: <http://www.nanopub.org/nschema#> ." +
                " @prefix dcterms: <http://purl.org/dc/terms/> ." +
                " @prefix prov: <http://www.w3.org/ns/prov#> ." +
                " @prefix pav: <http://swan.mindinformatics.org/ontologies/1.2/pav/> ." +
                " @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." +
                " @prefix obo: <http://purl.obolibrary.org/obo/> ." +
                "  sub:Head {" +
                "   this: np:hasAssertion sub:Assertion;" +
                "     np:hasProvenance sub:Provenance;" +
                "     np:hasPublicationInfo sub:Pubinfo;" +
                "     a np:Nanopublication ." +
                " }" +
                "  sub:Assertion {" +
                "   sub:Interaction obo:RO_0000057 sub:Organism_1, sub:Organism_2;" +
                "     a obo:GO_0044419 ." +
                "      sub:Organism_1 obo:RO_0002444 sub:Organism_2;" +
                "     rdfs:label \"Leptoconchus incycloseris\" ." +
                "      sub:Organism_2 rdfs:label \"Fungia (Cycloseris) costulata\" ." +
                " }" +
                "  sub:Provenance {" +
                "   sub:Assertion prov:wasDerivedFrom <https://doi.org/10.1007/s13127-011-0039-1> ." +
                "      <https://doi.org/10.1007/s13127-011-0039-1> dcterms:bibliographicCitation \"Gittenberger, A., Gittenberger, E. (2011). Cryptic, adaptive radiation of endoparasitic snails: sibling species of Leptoconchus (Gastropoda: Coralliophilidae) in corals. Org Divers Evol, 11(1), 21&#8211;41. doi:10.1007/s13127-011-0039-1\" ." +
                " }" +
                "  sub:Pubinfo {" +
                "   this: dcterms:license <https://creativecommons.org/licenses/by/4.0/>;" +
                "     pav:createdBy <https://doi.org/10.5281/zenodo.998263>;" +
                "     prov:wasDerivedFrom <https://doi.org/10.5281/zenodo.207958> ." +
                "      <https://doi.org/10.5281/zenodo.207958> dcterms:bibliographicCitation \"Jorrit H. Poelen. 2014. Species associations manually extracted from literature. doi:10.5281/zenodo.207958\" ." +
                " }";
        assertThat(StringUtils.trim(out1.toString().split("\\n")[0]), is(firstLine));
    }

}