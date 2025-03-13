package org.globalbioticinteractions.elton.util;

import org.apache.commons.io.IOUtils;
import org.eol.globi.domain.Environment;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.LocationImpl;
import org.eol.globi.domain.StudyImpl;
import org.eol.globi.domain.TaxonImpl;
import org.eol.globi.util.DateUtil;
import org.eol.globi.util.ResourceServiceLocal;
import org.globalbioticinteractions.dataset.DatasetImpl;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DataPackageWriterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void writeZip() throws IOException {
        AtomicInteger counter = new AtomicInteger(0);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataPackageWriter dataPackageWriter = new DataPackageWriter(out, () -> Integer.toString(counter.incrementAndGet()), folder.newFolder("bla"));

        DatasetImpl dataset = new DatasetImpl(
                "some/namespace",
                new ResourceServiceLocal(is -> is),
                URI.create("some:uri")
        );

        StudyImpl study = new StudyImpl("some study");
        SpecimenImpl specimen = new SpecimenImpl(
                dataset, study, dataPackageWriter, new TaxonImpl("some taxon", "boo:123")
        );

        specimen.setEventDate(DateUtil.parseDateUTC("1969-07-20").toDate());

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

        try {
            dataPackageWriter.write(specimen, InteractType.ATE, specimen, study, dataset);
        } finally {
            dataPackageWriter.close();
        }

        ByteArrayInputStream generated = new ByteArrayInputStream(out.toByteArray());

        File archive = folder.newFile("package.zip");
        IOUtils.copy(generated, new FileOutputStream(archive));

        ZipFile zip = new ZipFile(archive);

        List<String> filenames = zip.stream().map(ZipEntry::getName).collect(Collectors.toList());

        assertThat(filenames, Matchers.hasItems("datapackage.json", "event.csv", "occurrence.csv", "organism-interaction.csv"));

        String prefix = "/org/globalbioticinteractions/elton/dwc-dp-expected/";
        assertEntryContent(zip, "datapackage.json", prefix + "datapackage.json");
        assertEntryContent(zip, "event.csv", prefix + "event.csv");
        assertEntryContent(zip, "occurrence.csv", prefix + "occurrence.csv");
        assertEntryContent(zip, "organism-interaction.csv", prefix + "organism-interaction.csv");

    }

    private void assertEntryContent(ZipFile zip, String zipEntryName, String expectedResourceContent) throws IOException {
        ZipEntry entry = zip.getEntry(zipEntryName);

        InputStream inputStream = zip.getInputStream(entry);

        ByteArrayOutputStream datapackage = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, datapackage);

        assertThat(new String(datapackage.toByteArray(), StandardCharsets.UTF_8),
                is(IOUtils.toString(getClass().getResourceAsStream(expectedResourceContent), StandardCharsets.UTF_8))
        );
    }


}