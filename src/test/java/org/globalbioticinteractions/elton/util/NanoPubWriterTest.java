package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.Environment;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.LocationImpl;
import org.eol.globi.domain.StudyImpl;
import org.eol.globi.domain.TaxonImpl;
import org.eol.globi.util.ResourceServiceLocal;
import org.globalbioticinteractions.dataset.DatasetImpl;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Date;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class NanoPubWriterTest {

    @Test
    public void testLocale() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NanoPubWriter nanoPubWriter = new NanoPubWriter(new PrintStream(out), () -> "1");

        DatasetImpl dataset = new DatasetImpl(
                "some/namespace",
                new ResourceServiceLocal(is -> is),
                URI.create("some:uri")
        );


        StudyImpl study = new StudyImpl("some study");
        SpecimenImpl specimen = new SpecimenImpl(
                dataset, study, nanoPubWriter, new TaxonImpl("some taxon", "boo:123")
        );
        specimen.setEventDate(new Date(0));
        LocationImpl location = new LocationImpl(12.2d, 1.2d, 2d, null);
        location.setLocality("some locality");
        location.setLocalityId("GEONAMES:123");
        location.addEnvironment(new Environment() {
            @Override
            public String getName() {
                return "some envo";
            }

            @Override
            public void setExternalId(String externalId) {

            }

            @Override
            public String getExternalId() {
                return "ENVO:123";
            }
        });
        specimen.caughtIn(location);

        nanoPubWriter.write(specimen, InteractType.ATE, specimen, study, dataset);

        assertThat(out.toString(), containsString("some taxon"));
        assertThat(out.toString(), containsString("http://www.geonames.org/123"));
        assertThat(out.toString(), containsString("ENVO"));
        assertThat(out.toString(), containsString("<some:uri>"));
        assertThat(out.toString(), containsString("geo:latitude \"12.2\"^^xsd:decimal"));
        assertThat(out.toString(), containsString("prov:atTime \"1970-01-01T00:00:00Z\"^^xsd:dateTime"));
    }

    @Test
    public void testReference() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NanoPubWriter nanoPubWriter = new NanoPubWriter(new PrintStream(out), () -> "1");

        DatasetImpl dataset = new DatasetImpl(
                "some/namespace",
                new ResourceServiceLocal(is -> is),
                URI.create("some:uri")
        );

        StudyImpl study = new StudyImpl("some study");
        study.setCitation("@article{spegazzini1912amnhnba,\r   title = {Contibuci<U+03CC>n al estudio de las {Laboulbeniomicetas} {Argentinas}},   volume = {23},   url = {http://www.museonacional.gov.co/Paginas/default.aspx},   journal = {Anales del Museo Nacional de Historia Natural de Buenos Aires},   author = {Spegazzini, Carlos},   year = {1912},   pages = {167--228},  }");
        SpecimenImpl specimen = new SpecimenImpl(
                dataset, study, nanoPubWriter, new TaxonImpl("Chlaenius purpuricollis Randall, 1838", "boo:123")
        );
        specimen.setEventDate(new Date(0));
        LocationImpl location = new LocationImpl(12.2d, 1.2d, 2d, null);
        location.setLocality("some locality");
        location.setLocalityId("GEONAMES:123");
        location.addEnvironment(new Environment() {
            @Override
            public String getName() {
                return "some envo";
            }

            @Override
            public void setExternalId(String externalId) {

            }

            @Override
            public String getExternalId() {
                return "ENVO:123";
            }
        });
        specimen.caughtIn(location);

        nanoPubWriter.write(specimen, InteractType.ATE, specimen, study, dataset);

        assertThat(out.toString(), containsString("Chlaenius purpuricollis Randall, 1838"));
        assertThat(out.toString(), containsString("http://www.geonames.org/123"));
        assertThat(out.toString(), containsString("ENVO"));
        assertThat(out.toString(), containsString("<some:uri>"));
        assertThat(out.toString(), containsString("geo:latitude \"12.2\"^^xsd:decimal"));
        assertThat(out.toString(), containsString("prov:atTime \"1970-01-01T00:00:00Z\"^^xsd:dateTime"));
        assertThat(out.toString(), containsString("spegazzini1912amnhnba"));
    }

    @Test
    public void extractDatasetURIGitHub() {
        String datasetURI = NanoPubWriter.extractDatasetURI(
                new DatasetImpl(
                        "some/namespace",
                        new ResourceServiceLocal(is -> is),
                        URI.create("https://github.com/hurlbertlab/dietdatabase/archive/f98e5b3dc7480c92a468433400a725d71c2ad51c.zip")
                )
        );
        assertThat(datasetURI, is("https://github.com/hurlbertlab/dietdatabase"));
    }

    @Test
    public void extractDatasetURIDOI() {
        String datasetURI = NanoPubWriter.extractDatasetURI(
                new DatasetImpl(
                        "some/namespace",
                        new ResourceServiceLocal(is -> is),
                        URI.create("https://doi.org/10.123")
                )
        );
        assertThat(datasetURI, is("https://doi.org/10.123"));
    }

    @Test
    public void extractDatasetURISomeNamespace() {
        String datasetURI = NanoPubWriter.extractDatasetURI(
                new DatasetImpl(
                        "some/namespace",
                        new ResourceServiceLocal(is -> is),
                        URI.create("some:namespace")
                )
        );
        assertThat(datasetURI, is("some:namespace"));
    }

}