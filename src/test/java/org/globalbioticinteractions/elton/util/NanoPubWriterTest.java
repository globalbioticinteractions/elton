package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.Environment;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.LocationImpl;
import org.eol.globi.domain.StudyImpl;
import org.eol.globi.domain.TaxonImpl;
import org.eol.globi.service.DatasetImpl;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class NanoPubWriterTest {

    @Test
    public void testLocale() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NanoPubWriter nanoPubWriter = new NanoPubWriter(new PrintStream(out), new IdGenerator() {
            @Override
            public String generate() {
                return "1";
            }
        });

        DatasetImpl dataset = new DatasetImpl("some/namespace", URI.create("some:uri"));
        Stream<String> datasetInfo = Stream.of("someinfo");
        StudyImpl study = new StudyImpl("some study");
        SpecimenTaxonOnly specimen = new SpecimenTaxonOnly(dataset, datasetInfo, study, nanoPubWriter, new TaxonImpl("some taxon", "boo:123"));
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
        nanoPubWriter.write(specimen, InteractType.ATE, specimen, study, dataset, datasetInfo);

        assertThat(out.toString(), containsString("some taxon"));
        assertThat(out.toString(), containsString("http://www.geonames.org/123"));
        assertThat(out.toString(), containsString("ENVO"));
    }


}