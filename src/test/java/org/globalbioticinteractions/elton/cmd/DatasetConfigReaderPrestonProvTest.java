package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.IOUtils;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.dataset.Dataset;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DatasetConfigReaderPrestonProvTest {

    @Test
    public void readDatasetPrestonGBIFProv() {
        assertWithMeta("foo/meta.xml", "foo/eml.xml", "meta.xml");
    }

    @Test
    public void readDatasetCustomEml() {
        assertWithMeta(
                "foo/meta.xml",
                "foo/eml-custom.xml",
                "meta-custom-eml.xml"
        );
    }

    @Test
    public void readDatasetImplicitEml() {
        assertWithMeta(
                "foo/meta.xml",
                "foo/eml.xml",
                "meta-implicit-eml.xml"
        );
    }

    @Test
    public void readDatasetMissingMeta() {
        assertNull(getDataset("foo/metaz.xml", "eml.xml", "meta.xml"));
    }

    @Test
    public void readDatasetPrestonGBIFProvRootPath() {
        assertWithMeta("meta.xml", "eml.xml", "meta.xml");
    }

    private static void assertWithMeta(String metaPath, String emlPath, String metaResourceName) {
        Dataset dataset = getDataset(metaPath, emlPath, metaResourceName);

        assertNotNull(dataset);

        assertThat(dataset.getArchiveURI(), Is.is(URI.create("https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip")));
        assertThat(dataset.getFormat(), Is.is("dwca"));
        assertThat(dataset.getConfig()
                        .at("/url")
                        .asText(),
                Is.is("https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip"));
        assertThat(dataset.getConfig()
                        .at("/format")
                        .asText(),
                Is.is("dwca"));
        assertThat(dataset.getConfig()
                        .at("/resources")
                        .get("https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip")
                        .asText(),
                Is.is("hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423"));
        assertThat(dataset.getConfig()
                        .at("/resources")
                        .get("/eml.xml")
                        .asText(),
                Is.is("zip:hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423!/" + emlPath));
    }

    private static Dataset getDataset(final String metaPath, final String emlPath, final String metaResourceName) {
        String provLogPrestonGBIF = getProvLogPrestonGBIF();

        String[] lines = provLogPrestonGBIF.split("\n");
        DatasetConfigReaderPrestonProv reader = new DatasetConfigReaderPrestonProv(new ResourceService() {
            @Override
            public InputStream retrieve(URI uri) throws IOException {
                if (URI.create("hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423").equals(uri)) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                        zipOutputStream.putNextEntry(new ZipEntry(metaPath));
                        IOUtils.copy(DatasetConfigReaderPrestonProvTest.class.getResourceAsStream(metaResourceName), zipOutputStream);
                        zipOutputStream.putNextEntry(new ZipEntry(emlPath));
                        IOUtils.copy(DatasetConfigReaderPrestonProvTest.class.getResourceAsStream("eml-ucsb-izc.xml"), zipOutputStream);
                    }
                    return new ByteArrayInputStream(outputStream.toByteArray());
                } else if (URI.create("zip:hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423!/" + metaPath).equals(uri)) {
                    return DatasetConfigReaderPrestonProvTest.class.getResourceAsStream(metaResourceName);
                } else {
                    throw new IOException("[" + uri.toString() + "] unknown");
                }
            }
        });
        Dataset dataset = null;
        for (String line : lines) {
            try {
                dataset = reader.readConfig(line);
                if (dataset != null) {
                    break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return dataset;
    }

    public static String getProvLogPrestonGBIF() {
        // elton prov reader does not understand preston-GBIF prov
        String provLogPrestonGBIF = "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<https://preston.guoda.bio> <http://purl.org/dc/terms/description> \"Preston is a software program that finds, archives and provides access to biodiversity datasets.\"@en <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> <http://purl.org/dc/terms/description> \"A crawl event that discovers biodiversity archives.\"@en <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T00:13:49.185Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> <http://www.w3.org/ns/prov#wasStartedBy> <https://preston.guoda.bio> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Icaro Alzuru, & Michael Elliott. 2018-2024. Preston: a biodiversity dataset tracker (Version 0.10.3-SNAPSHOT) [Software]. Zenodo. https://doi.org/10.5281/zenodo.1410543\"@en <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
                "<hash://sha256/c7b80f007878c4abdb316fd22b022784a1916e29b5e0758908583558cdc88293> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> <urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> .\n" +
                "<hash://sha256/c7b80f007878c4abdb316fd22b022784a1916e29b5e0758908583558cdc88293> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> <urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> .\n" +
                "<urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T00:13:50.395Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> .\n" +
                "<urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> .\n" +
                "<urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> <urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> .\n" +
                "<urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> <http://www.w3.org/ns/prov#used> <https://api.gbif.org/v1/dataset/d6097f75-f99e-4c2a-b8a5-b0fc213ecbd0> <urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> .\n" +
                "<https://api.gbif.org/v1/dataset/d6097f75-f99e-4c2a-b8a5-b0fc213ecbd0> <http://purl.org/pav/hasVersion> <hash://sha256/c7b80f007878c4abdb316fd22b022784a1916e29b5e0758908583558cdc88293> <urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> .\n" +
                "<urn:uuid:ae1ca8b6-0d55-4591-9594-7fa32a02dffc> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:ae1ca8b6-0d55-4591-9594-7fa32a02dffc> .\n" +
                "<urn:uuid:ae1ca8b6-0d55-4591-9594-7fa32a02dffc> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:bfd1bef5-48f0-467f-8cab-9a61878d7ce2> <urn:uuid:ae1ca8b6-0d55-4591-9594-7fa32a02dffc> .\n" +
                "<hash://sha256/c7b80f007878c4abdb316fd22b022784a1916e29b5e0758908583558cdc88293> <http://www.w3.org/ns/prov#hadMember> <d6097f75-f99e-4c2a-b8a5-b0fc213ecbd0> <urn:uuid:ae1ca8b6-0d55-4591-9594-7fa32a02dffc> .\n" +
                "<d6097f75-f99e-4c2a-b8a5-b0fc213ecbd0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#seeAlso> <https://doi.org/10.15468/w6hvhv> <urn:uuid:ae1ca8b6-0d55-4591-9594-7fa32a02dffc> .\n" +
                "<d6097f75-f99e-4c2a-b8a5-b0fc213ecbd0> <http://www.w3.org/ns/prov#hadMember> <https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <urn:uuid:ae1ca8b6-0d55-4591-9594-7fa32a02dffc> .\n" +
                "<https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <http://purl.org/dc/elements/1.1/format> \"application/dwca\" <urn:uuid:ae1ca8b6-0d55-4591-9594-7fa32a02dffc> .\n" +
                "<hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> <urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> .\n" +
                "<hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> <urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> .\n" +
                "<urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T00:13:52.100Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> .\n" +
                "<urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> .\n" +
                "<urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:ae1ca8b6-0d55-4591-9594-7fa32a02dffc> <urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> .\n" +
                "<urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> <http://www.w3.org/ns/prov#used> <https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> .\n" +
                "<https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <http://purl.org/pav/hasVersion> <hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <urn:uuid:689d54b9-77da-45fb-83a2-40486f7f6cba> .\n";
        return provLogPrestonGBIF;
    }


}