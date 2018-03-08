package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.startsWith;
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
            System.err.println(out1.toString().split("\\n")[0]);
            String firstLine = "@prefix np: <http://www.nanopub.org/nschema#> .@prefix dcterms: <http://purl.org/dc/terms/> .@prefix opm: <http://purl.org/net/opmv/ns#> .@prefix pav: <http://swan.mindinformatics.org/ontologies/1.2/pav/> .@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .@prefix sio: <http://semanticscience.org/resource/> .@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .@prefix obo: <http://purl.obolibrary.org/obo/> .@prefix : <http://purl.org/nanopub/temp/NanoPub_1#> . :Head {  : a np:Nanopublication ;    np:hasAssertion :Assertion ;    np:hasProvenance :Provenance ;    np:hasPublicationInfo :Pubinfo .}   :Assertion {  :Interaction a obo:GO_0044419 ;    obo:RO_0000057 :Organism_1 ;    obo:RO_0000057 :Organism_2 .  :Organism_1 <http://purl.obolibrary.org/obo/RO_0002444> :Organism_2 .   :Organism_1 rdfs:label \"Leptoconchus incycloseris\" .   :Organism_2 rdfs:label \"Fungia (Cycloseris) costulata\" . } :Provenance {  :Assertion opm:wasDerivedFrom <https://doi.org/10.5281/zenodo.207958> ;    opm:wasGeneratedBy <https://doi.org/10.5281/zenodo.998263> .} :Pubinfo {  : pav:authoredBy <https://orcid.org/0000-0003-3138-4118> .  : pav:createdBy <https://doi.org/10.5281/zenodo.998263> .}";
            assertThat(out1.toString().split("\\n")[0], is(firstLine));
        }
    }


}