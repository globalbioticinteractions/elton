package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CmdNanoPubsTest {

    @Test
    public void interactions() throws URISyntaxException {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("nanopubs", "--cache-dir=" + CmdTestUtil.cacheDirTest(), "globalbioticinteractions/template-dataset");

        Assert.assertEquals(jc.getParsedCommand(), "nanopubs");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdNanoPubs.class);
        CmdNanoPubs cmdNames = (CmdNanoPubs) actual.getObjects().get(0);
        assertThat(cmdNames.getNamespaces().size(), is(1));
        assertThat(cmdNames.getNamespaces(), hasItem("globalbioticinteractions/template-dataset"));
        cmdNames.setIdGenerator(() -> "1");
        if (actual.getObjects().get(0) instanceof Runnable) {
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(out1);
            ((CmdInteractions) actual.getObjects().get(0)).run(out);
//            System.err.println(out1.toString().split("\\n")[0]);
            String firstLine = "@prefix this: <http://purl.org/np/RAgHvlLudGEWWGb_nMxknXz7CREk0rLT6_VAHM3wd2QdA> . @prefix sub: <http://purl.org/np/RAgHvlLudGEWWGb_nMxknXz7CREk0rLT6_VAHM3wd2QdA#> . @prefix np: <http://www.nanopub.org/nschema#> . @prefix opm: <http://purl.org/net/opmv/ns#> . @prefix pav: <http://swan.mindinformatics.org/ontologies/1.2/pav/> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . @prefix obo: <http://purl.obolibrary.org/obo/> .  sub:Head { 	this: np:hasAssertion sub:Assertion ; 		np:hasProvenance sub:Provenance ; 		np:hasPublicationInfo sub:Pubinfo ; 		a np:Nanopublication . }  sub:Assertion { 	sub:Interaction obo:RO_0000057 sub:Organism_1 , sub:Organism_2 ; 		a obo:GO_0044419 . 	 	sub:Organism_1 obo:RO_0002444 sub:Organism_2 ; 		rdfs:label \"Leptoconchus incycloseris\" . 	 	sub:Organism_2 rdfs:label \"Fungia (Cycloseris) costulata\" . }  sub:Provenance { 	sub:Assertion opm:wasDerivedFrom <https://doi.org/10.5281/zenodo.207958> ; 		opm:wasGeneratedBy <https://doi.org/10.5281/zenodo.998263> . }  sub:Pubinfo { 	this: pav:authoredBy <https://orcid.org/0000-0003-3138-4118> ; 		pav:createdBy <https://doi.org/10.5281/zenodo.998263> . } ";
            assertThat(out1.toString().split("\\n")[0], is(firstLine));
        }
    }


}