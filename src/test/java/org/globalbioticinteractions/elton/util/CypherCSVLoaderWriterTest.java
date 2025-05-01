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

public class CypherCSVLoaderWriterTest {

    @Test
    public void testLocale() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        CypherCSVLoaderWriter writer = new CypherCSVLoaderWriter(new PrintStream(out), () -> "1");

        DatasetImpl dataset = new DatasetImpl(
                "some/namespace",
                new ResourceServiceLocal(is -> is),
                URI.create("some:uri")
        );


        StudyImpl study = new StudyImpl("some study");
        SpecimenImpl specimen = new SpecimenImpl(
                dataset, study, writer, new TaxonImpl("some taxon", "boo:123")
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

        writer.write(specimen, InteractType.ATE, specimen, study, dataset);

        assertThat(out.toString(), containsString("some taxon"));
        assertThat(out.toString(), containsString("http://www.geonames.org/123"));
        assertThat(out.toString(), containsString("ENVO"));
        assertThat(out.toString(), containsString("<some:uri>"));
        assertThat(out.toString(), containsString("geo:latitude \"12.2\"^^xsd:decimal"));
        assertThat(out.toString(), containsString("prov:atTime \"1970-01-01T00:00:00Z\"^^xsd:dateTime"));
    }

}