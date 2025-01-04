package org.globalbioticinteractions.elton.cmd;

import org.globalbioticinteractions.dataset.Dataset;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNull;

public class DatasetConfigReaderProvTest {

    @Test
    public void readDatasetEltonProv() {

        String provLogGeneratedByElton = "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/template-dataset> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:41389744-0f4d-47e2-8506-76999e1b5c34> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <http://purl.org/pav/hasVersion> <hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:cce97773-a8e2-4af4-94f9-0ac2699cb28e> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/globi.json> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/94bc19a3b0f172f63138fdc9384bb347f110e6fae6d42613a6eba019df6268d2> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:c7b1a849-8230-4e34-a0d5-7b663bc87e01> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/interactions.tsv> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha256/d84999936296e4b85086f2851f4459605502f4eb80b9484049b81d34f43b2ff1> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n";

        String[] lines = provLogGeneratedByElton.split("\n");
        DatasetConfigReaderProv datasetConfigReaderProv = new DatasetConfigReaderProv();
        Dataset dataset = null;
        for (String line : lines) {
            try {
                dataset = datasetConfigReaderProv.readConfig(line);
                if (dataset != null) {
                    break;
                }
            } catch (IOException e) {
                //
            }
        }

        assertNotNull(dataset);
        assertThat(dataset.getNamespace(), Is.is("globalbioticinteractions/template-dataset"));
        assertThat(dataset.getArchiveURI(), Is.is(URI.create("hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44")));
        assertThat(dataset.getConfig(), Is.is(nullValue()));
    }

    @Test
    public void readDatasetPrestonGBIFProv() {

        String provLogGeneratedByElton = "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:c30e646d-81f5-463d-bf2e-340cf4fab56a> .\n" +
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

        String[] lines = provLogGeneratedByElton.split("\n");
        DatasetConfigReaderProv datasetConfigReaderProv = new DatasetConfigReaderProv();
        Dataset dataset = null;
        for (String line : lines) {
            try {
                dataset = datasetConfigReaderProv.readConfig(line);
                if (dataset != null) {
                    break;
                }
            } catch (IOException e) {
                //
            }
        }

        assertNotNull(dataset);
        assertThat(dataset.getNamespace(), Is.is("local"));
        assertThat(dataset.getArchiveURI(), Is.is(URI.create("hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423")));
        assertThat(dataset.getConfig(), Is.is(nullValue()));
    }
    @Test
    public void readDatasetPrestonPlainProv() {

        String provLogGeneratedByElton = "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<https://preston.guoda.bio> <http://purl.org/dc/terms/description> \"Preston is a software program that finds, archives and provides access to biodiversity datasets.\"@en <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> <http://purl.org/dc/terms/description> \"A crawl event that discovers biodiversity archives.\"@en <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T00:27:18.110Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> <http://www.w3.org/ns/prov#wasStartedBy> <https://preston.guoda.bio> <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Icaro Alzuru, & Michael Elliott. 2018-2024. Preston: a biodiversity dataset tracker (Version 0.10.3-SNAPSHOT) [Software]. Zenodo. https://doi.org/10.5281/zenodo.1410543\"@en <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> .\n" +
                "<hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> <urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> .\n" +
                "<hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> <urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> .\n" +
                "<urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T00:27:19.902Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> .\n" +
                "<urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> .\n" +
                "<urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:fde6b827-42f0-44b7-b577-b2fbfc4977b2> <urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> .\n" +
                "<urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> <http://www.w3.org/ns/prov#used> <https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> .\n" +
                "<https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <http://purl.org/pav/hasVersion> <hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <urn:uuid:0eda1d2e-e0dd-40d1-be56-73903b5af5ff> .\n";

        String[] lines = provLogGeneratedByElton.split("\n");
        DatasetConfigReaderProv datasetConfigReaderProv = new DatasetConfigReaderProv();
        Dataset dataset = null;
        for (String line : lines) {
            try {
                dataset = datasetConfigReaderProv.readConfig(line);
                if (dataset != null) {
                    break;
                }
            } catch (IOException e) {
                //
            }
        }

        assertNull(dataset);
    }


    @Test
    public void externalReference() {
        String prov = "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://purl.org/dc/terms/description> \"Elton helps to access, review and index existing species interaction datasets.\"@en <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <http://purl.org/dc/terms/description> \"Update Local Datasets With Remote Sources\"@en <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:35:56.999Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <http://www.w3.org/ns/prov#wasStartedBy> <https://globalbioticinteractions.org/elton> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Tobias Kuhn & Katrin Leinweber. (2017/2024). globalbioticinteractions/elton: 0.13.10-SNAPSHOT. Zenodo. https://zenodo.org/doi/10.5281/zenodo.998263\"@en <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:35:57.128Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:35:57.128Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/35d91f7f6a6595e3266445e8d218eb9ebf24454273bb15b934684691050d757d> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/35d91f7f6a6595e3266445e8d218eb9ebf24454273bb15b934684691050d757d> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:35:59.902Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <http://purl.org/pav/hasVersion> <hash://sha256/35d91f7f6a6595e3266445e8d218eb9ebf24454273bb15b934684691050d757d> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:35:59.902Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/35d91f7f6a6595e3266445e8d218eb9ebf24454273bb15b934684691050d757d> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/35d91f7f6a6595e3266445e8d218eb9ebf24454273bb15b934684691050d757d> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:35:59.903Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <http://purl.org/pav/hasVersion> <hash://sha256/35d91f7f6a6595e3266445e8d218eb9ebf24454273bb15b934684691050d757d> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:157b3b29-6077-4fba-b719-9eb77d4c287a> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:35:59.903Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:00.182Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:00.182Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/7ffd8a5d5ecf63a80c3bf455bfc5ebd5ab0c0aff74fa06ba682bdc4a48c2c765> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/7ffd8a5d5ecf63a80c3bf455bfc5ebd5ab0c0aff74fa06ba682bdc4a48c2c765> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:02.456Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUFvgjAAhf9Lz2yhVaeQeNAYIGQYrSmFXkxLq1QqOFoj0ey_T5Odl73jy_u-9wBWKQlC_308gWg2RdBH448AzmZTD1z4UYEQeaC58f5oQfh4rh0IwdWq_u1oOsGN0J3TlW6d6nnldNda4IGzclxyxze9OujhCXRc719FpZ0C3x6wVd8Zs9fPZxDF5lqiAbI4P5QouMo4H8sogJKaZtVGkBXpJI9rIxpmZLzuFgMzGTLbLEl3wuC-pLglhBW8wKOSwuXilbpa7E6W8ialNL4N6lTvyCr1VY4P-T1zmK5HcmCYEcmFL895tPbxnW1EgRkvLoNK5K_n8_TFb1RAgzOaYnKvcZ4YQmgUq6S2-Bwst_i_Hvu3Zzufg-8f8xuGMQ.Z3h0Xw.hQmy17tGJyy9AO1Zu8RG83sw8uw> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUFvgjAAhf9Lz2yhVaeQeNAYIGQYrSmFXkxLq1QqOFoj0ey_T5Odl73jy_u-9wBWKQlC_308gWg2RdBH448AzmZTD1z4UYEQeaC58f5oQfh4rh0IwdWq_u1oOsGN0J3TlW6d6nnldNda4IGzclxyxze9OujhCXRc719FpZ0C3x6wVd8Zs9fPZxDF5lqiAbI4P5QouMo4H8sogJKaZtVGkBXpJI9rIxpmZLzuFgMzGTLbLEl3wuC-pLglhBW8wKOSwuXilbpa7E6W8ialNL4N6lTvyCr1VY4P-T1zmK5HcmCYEcmFL895tPbxnW1EgRkvLoNK5K_n8_TFb1RAgzOaYnKvcZ4YQmgUq6S2-Bwst_i_Hvu3Zzufg-8f8xuGMQ.Z3h0Xw.hQmy17tGJyy9AO1Zu8RG83sw8uw> <http://purl.org/pav/hasVersion> <hash://sha256/7ffd8a5d5ecf63a80c3bf455bfc5ebd5ab0c0aff74fa06ba682bdc4a48c2c765> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:02.457Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/7ffd8a5d5ecf63a80c3bf455bfc5ebd5ab0c0aff74fa06ba682bdc4a48c2c765> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/7ffd8a5d5ecf63a80c3bf455bfc5ebd5ab0c0aff74fa06ba682bdc4a48c2c765> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:02.458Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUFvgjAAhf9Lz2yhVaeQeNAYIGQYrSmFXkxLq1QqOFoj0ey_T5Odl73jy_u-9wBWKQlC_308gWg2RdBH448AzmZTD1z4UYEQeaC58f5oQfh4rh0IwdWq_u1oOsGN0J3TlW6d6nnldNda4IGzclxyxze9OujhCXRc719FpZ0C3x6wVd8Zs9fPZxDF5lqiAbI4P5QouMo4H8sogJKaZtVGkBXpJI9rIxpmZLzuFgMzGTLbLEl3wuC-pLglhBW8wKOSwuXilbpa7E6W8ialNL4N6lTvyCr1VY4P-T1zmK5HcmCYEcmFL895tPbxnW1EgRkvLoNK5K_n8_TFb1RAgzOaYnKvcZ4YQmgUq6S2-Bwst_i_Hvu3Zzufg-8f8xuGMQ.Z3h0Xw.hQmy17tGJyy9AO1Zu8RG83sw8uw> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUFvgjAAhf9Lz2yhVaeQeNAYIGQYrSmFXkxLq1QqOFoj0ey_T5Odl73jy_u-9wBWKQlC_308gWg2RdBH448AzmZTD1z4UYEQeaC58f5oQfh4rh0IwdWq_u1oOsGN0J3TlW6d6nnldNda4IGzclxyxze9OujhCXRc719FpZ0C3x6wVd8Zs9fPZxDF5lqiAbI4P5QouMo4H8sogJKaZtVGkBXpJI9rIxpmZLzuFgMzGTLbLEl3wuC-pLglhBW8wKOSwuXilbpa7E6W8ialNL4N6lTvyCr1VY4P-T1zmK5HcmCYEcmFL895tPbxnW1EgRkvLoNK5K_n8_TFb1RAgzOaYnKvcZ4YQmgUq6S2-Bwst_i_Hvu3Zzufg-8f8xuGMQ.Z3h0Xw.hQmy17tGJyy9AO1Zu8RG83sw8uw> <http://purl.org/pav/hasVersion> <hash://sha256/7ffd8a5d5ecf63a80c3bf455bfc5ebd5ab0c0aff74fa06ba682bdc4a48c2c765> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:c2c41c06-6f24-43ad-b2dd-0d5674467e79> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:02.459Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:02.675Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:02.675Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/17e81a39b9c59b9ac78ff9bc0201c0cb7ffdd5796cf913a62ecce9069006f2c5> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/17e81a39b9c59b9ac78ff9bc0201c0cb7ffdd5796cf913a62ecce9069006f2c5> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:04.180Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV9vgjAAxL9Ln93CH92ExAeMAUKG0Zq20BfT0iqVCo7WSDR-92Gy52X3dLnc_e4BjJQChM77_DOYeVN_7nqzIPjwJ-DCjhKEo2lurD8aED7GrgUhuBrZvx11x5nmqrOqUq2VPaus6loDJuAsLRPMsk0vD2oYBx1T-1dQKSvBcwJM1Xda79X4C-JEX0tvcGmCD6UXXEWCpyIOXEF0s2pjlxbZDCe15g3VIll30UB17ultnmY7rmFfEtgiRAtWQL8k7jJ6qa6i3ckQ1mSEJLdBnuodWmWOxPCA77mFZO2LgUKKBOOOOON47cA73fACUlZcBpmKX87X6ZvdCHc1zEkG0b2GONUIkTiRaW3gOVhu4X855m_OdrEAzx9lPoXY.Z3h0YQ.CgKS-vrWLkoG9M3qnDcic80W6XA> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV9vgjAAxL9Ln93CH92ExAeMAUKG0Zq20BfT0iqVCo7WSDR-92Gy52X3dLnc_e4BjJQChM77_DOYeVN_7nqzIPjwJ-DCjhKEo2lurD8aED7GrgUhuBrZvx11x5nmqrOqUq2VPaus6loDJuAsLRPMsk0vD2oYBx1T-1dQKSvBcwJM1Xda79X4C-JEX0tvcGmCD6UXXEWCpyIOXEF0s2pjlxbZDCe15g3VIll30UB17ultnmY7rmFfEtgiRAtWQL8k7jJ6qa6i3ckQ1mSEJLdBnuodWmWOxPCA77mFZO2LgUKKBOOOOON47cA73fACUlZcBpmKX87X6ZvdCHc1zEkG0b2GONUIkTiRaW3gOVhu4X855m_OdrEAzx9lPoXY.Z3h0YQ.CgKS-vrWLkoG9M3qnDcic80W6XA> <http://purl.org/pav/hasVersion> <hash://sha256/17e81a39b9c59b9ac78ff9bc0201c0cb7ffdd5796cf913a62ecce9069006f2c5> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:04.181Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/17e81a39b9c59b9ac78ff9bc0201c0cb7ffdd5796cf913a62ecce9069006f2c5> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/17e81a39b9c59b9ac78ff9bc0201c0cb7ffdd5796cf913a62ecce9069006f2c5> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:04.182Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV9vgjAAxL9Ln93CH92ExAeMAUKG0Zq20BfT0iqVCo7WSDR-92Gy52X3dLnc_e4BjJQChM77_DOYeVN_7nqzIPjwJ-DCjhKEo2lurD8aED7GrgUhuBrZvx11x5nmqrOqUq2VPaus6loDJuAsLRPMsk0vD2oYBx1T-1dQKSvBcwJM1Xda79X4C-JEX0tvcGmCD6UXXEWCpyIOXEF0s2pjlxbZDCe15g3VIll30UB17ultnmY7rmFfEtgiRAtWQL8k7jJ6qa6i3ckQ1mSEJLdBnuodWmWOxPCA77mFZO2LgUKKBOOOOON47cA73fACUlZcBpmKX87X6ZvdCHc1zEkG0b2GONUIkTiRaW3gOVhu4X855m_OdrEAzx9lPoXY.Z3h0YQ.CgKS-vrWLkoG9M3qnDcic80W6XA> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV9vgjAAxL9Ln93CH92ExAeMAUKG0Zq20BfT0iqVCo7WSDR-92Gy52X3dLnc_e4BjJQChM77_DOYeVN_7nqzIPjwJ-DCjhKEo2lurD8aED7GrgUhuBrZvx11x5nmqrOqUq2VPaus6loDJuAsLRPMsk0vD2oYBx1T-1dQKSvBcwJM1Xda79X4C-JEX0tvcGmCD6UXXEWCpyIOXEF0s2pjlxbZDCe15g3VIll30UB17ultnmY7rmFfEtgiRAtWQL8k7jJ6qa6i3ckQ1mSEJLdBnuodWmWOxPCA77mFZO2LgUKKBOOOOON47cA73fACUlZcBpmKX87X6ZvdCHc1zEkG0b2GONUIkTiRaW3gOVhu4X855m_OdrEAzx9lPoXY.Z3h0YQ.CgKS-vrWLkoG9M3qnDcic80W6XA> <http://purl.org/pav/hasVersion> <hash://sha256/17e81a39b9c59b9ac78ff9bc0201c0cb7ffdd5796cf913a62ecce9069006f2c5> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:cd7dbe3e-1441-49a5-839a-9aaf9e6c52f5> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:04.183Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:04.419Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:04.420Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/c084290974156f5c334e2fab9dec52be9e0e5a4294180b052433d8f010106599> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/c084290974156f5c334e2fab9dec52be9e0e5a4294180b052433d8f010106599> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:06.414Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1vgjAARf9Ln9nCh04g8UFjKCHDaE0p9MW0tEqlgqM1Es3--zTZ87L7eHPPuQ9gpBQgdt_9cPYRhp43jYJwGvgzB1zYUYJ44oD2xoajAfHjObYgBlcjh7ej7jnTXPVW1aqzcmC1VX1ngAPO0jLBLNsM8qDGJ9AztX8VtbISfDvA1EOv9V49j0EC9bXyR4_C4lD50VXAYiKSyBNEt6su8WiZTQvYaN5SLeC6X4xU577e5mm24xoNFUEdxrRkJQoq4i0XrzT1YncyhLUZIfA2ylOzw6vMlQU6FPfcIrIOxEgRxYJxV5yLZO2iO93wElFWXkaZil_P5-mL3Qj3NMpJhvC9QUWqMSYJlGlj0DlabtF_PeZvz3Y-B98_te-GCw.Z3h0Yw.OULQIv02fpPYNOSRdUqufQusIzM> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1vgjAARf9Ln9nCh04g8UFjKCHDaE0p9MW0tEqlgqM1Es3--zTZ87L7eHPPuQ9gpBQgdt_9cPYRhp43jYJwGvgzB1zYUYJ44oD2xoajAfHjObYgBlcjh7ej7jnTXPVW1aqzcmC1VX1ngAPO0jLBLNsM8qDGJ9AztX8VtbISfDvA1EOv9V49j0EC9bXyR4_C4lD50VXAYiKSyBNEt6su8WiZTQvYaN5SLeC6X4xU577e5mm24xoNFUEdxrRkJQoq4i0XrzT1YncyhLUZIfA2ylOzw6vMlQU6FPfcIrIOxEgRxYJxV5yLZO2iO93wElFWXkaZil_P5-mL3Qj3NMpJhvC9QUWqMSYJlGlj0DlabtF_PeZvz3Y-B98_te-GCw.Z3h0Yw.OULQIv02fpPYNOSRdUqufQusIzM> <http://purl.org/pav/hasVersion> <hash://sha256/c084290974156f5c334e2fab9dec52be9e0e5a4294180b052433d8f010106599> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:06.415Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/c084290974156f5c334e2fab9dec52be9e0e5a4294180b052433d8f010106599> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/c084290974156f5c334e2fab9dec52be9e0e5a4294180b052433d8f010106599> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:06.416Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1vgjAARf9Ln9nCh04g8UFjKCHDaE0p9MW0tEqlgqM1Es3--zTZ87L7eHPPuQ9gpBQgdt_9cPYRhp43jYJwGvgzB1zYUYJ44oD2xoajAfHjObYgBlcjh7ej7jnTXPVW1aqzcmC1VX1ngAPO0jLBLNsM8qDGJ9AztX8VtbISfDvA1EOv9V49j0EC9bXyR4_C4lD50VXAYiKSyBNEt6su8WiZTQvYaN5SLeC6X4xU577e5mm24xoNFUEdxrRkJQoq4i0XrzT1YncyhLUZIfA2ylOzw6vMlQU6FPfcIrIOxEgRxYJxV5yLZO2iO93wElFWXkaZil_P5-mL3Qj3NMpJhvC9QUWqMSYJlGlj0DlabtF_PeZvz3Y-B98_te-GCw.Z3h0Yw.OULQIv02fpPYNOSRdUqufQusIzM> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1vgjAARf9Ln9nCh04g8UFjKCHDaE0p9MW0tEqlgqM1Es3--zTZ87L7eHPPuQ9gpBQgdt_9cPYRhp43jYJwGvgzB1zYUYJ44oD2xoajAfHjObYgBlcjh7ej7jnTXPVW1aqzcmC1VX1ngAPO0jLBLNsM8qDGJ9AztX8VtbISfDvA1EOv9V49j0EC9bXyR4_C4lD50VXAYiKSyBNEt6su8WiZTQvYaN5SLeC6X4xU577e5mm24xoNFUEdxrRkJQoq4i0XrzT1YncyhLUZIfA2ylOzw6vMlQU6FPfcIrIOxEgRxYJxV5yLZO2iO93wElFWXkaZil_P5-mL3Qj3NMpJhvC9QUWqMSYJlGlj0DlabtF_PeZvz3Y-B98_te-GCw.Z3h0Yw.OULQIv02fpPYNOSRdUqufQusIzM> <http://purl.org/pav/hasVersion> <hash://sha256/c084290974156f5c334e2fab9dec52be9e0e5a4294180b052433d8f010106599> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:4e2e4b07-8deb-4999-b4bc-1874974385e8> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:06.416Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:06.539Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:06.541Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/0593e819221d29a6396b0d57cdfe4588ad55276ea6ab50ec4620be5632d5c958> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/0593e819221d29a6396b0d57cdfe4588ad55276ea6ab50ec4620be5632d5c958> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:08.372Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjcGOgjAARP-lZ3YDKLKSeNAYIGQxWtMWejEtrVKp4NIaicZ_V5M9b3aOk3lv7sBIKUDkfvrBZOL746_QDcJwHE4ccGYHCaLAAc2V9QcDovtrbEEELkb2Hwfdcaa56qyqVGtlzyqrutYAB5ykZYJZtu7lXg0voGNq9y4qZSV4OMBUfaf1Tr2OQZzoS-kPHk3wvvSnF5HgsYinniC6WbaxR4sswEmteUO1SFbdfKA69_UmT7Mt17AvCWwRogUr4Kgk3mL-Tl3Nt0dDWJMRklwHeay3aJm5EsM9vuUWktVIDBRSJBh3xQnHKxfe6JoXkLLiPMhU_Hq-jz_sSrinYU4yiG41xKlGiMSJTGsDT9PFBv7XY_72bGYz8HgCr5OGBw.Z3h0ZQ.ElVc6ostMLc6mP0r6ttb3T_JCoE> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjcGOgjAARP-lZ3YDKLKSeNAYIGQxWtMWejEtrVKp4NIaicZ_V5M9b3aOk3lv7sBIKUDkfvrBZOL746_QDcJwHE4ccGYHCaLAAc2V9QcDovtrbEEELkb2Hwfdcaa56qyqVGtlzyqrutYAB5ykZYJZtu7lXg0voGNq9y4qZSV4OMBUfaf1Tr2OQZzoS-kPHk3wvvSnF5HgsYinniC6WbaxR4sswEmteUO1SFbdfKA69_UmT7Mt17AvCWwRogUr4Kgk3mL-Tl3Nt0dDWJMRklwHeay3aJm5EsM9vuUWktVIDBRSJBh3xQnHKxfe6JoXkLLiPMhU_Hq-jz_sSrinYU4yiG41xKlGiMSJTGsDT9PFBv7XY_72bGYz8HgCr5OGBw.Z3h0ZQ.ElVc6ostMLc6mP0r6ttb3T_JCoE> <http://purl.org/pav/hasVersion> <hash://sha256/0593e819221d29a6396b0d57cdfe4588ad55276ea6ab50ec4620be5632d5c958> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:08.373Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/0593e819221d29a6396b0d57cdfe4588ad55276ea6ab50ec4620be5632d5c958> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/0593e819221d29a6396b0d57cdfe4588ad55276ea6ab50ec4620be5632d5c958> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:08.373Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjcGOgjAARP-lZ3YDKLKSeNAYIGQxWtMWejEtrVKp4NIaicZ_V5M9b3aOk3lv7sBIKUDkfvrBZOL746_QDcJwHE4ccGYHCaLAAc2V9QcDovtrbEEELkb2Hwfdcaa56qyqVGtlzyqrutYAB5ykZYJZtu7lXg0voGNq9y4qZSV4OMBUfaf1Tr2OQZzoS-kPHk3wvvSnF5HgsYinniC6WbaxR4sswEmteUO1SFbdfKA69_UmT7Mt17AvCWwRogUr4Kgk3mL-Tl3Nt0dDWJMRklwHeay3aJm5EsM9vuUWktVIDBRSJBh3xQnHKxfe6JoXkLLiPMhU_Hq-jz_sSrinYU4yiG41xKlGiMSJTGsDT9PFBv7XY_72bGYz8HgCr5OGBw.Z3h0ZQ.ElVc6ostMLc6mP0r6ttb3T_JCoE> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjcGOgjAARP-lZ3YDKLKSeNAYIGQxWtMWejEtrVKp4NIaicZ_V5M9b3aOk3lv7sBIKUDkfvrBZOL746_QDcJwHE4ccGYHCaLAAc2V9QcDovtrbEEELkb2Hwfdcaa56qyqVGtlzyqrutYAB5ykZYJZtu7lXg0voGNq9y4qZSV4OMBUfaf1Tr2OQZzoS-kPHk3wvvSnF5HgsYinniC6WbaxR4sswEmteUO1SFbdfKA69_UmT7Mt17AvCWwRogUr4Kgk3mL-Tl3Nt0dDWJMRklwHeay3aJm5EsM9vuUWktVIDBRSJBh3xQnHKxfe6JoXkLLiPMhU_Hq-jz_sSrinYU4yiG41xKlGiMSJTGsDT9PFBv7XY_72bGYz8HgCr5OGBw.Z3h0ZQ.ElVc6ostMLc6mP0r6ttb3T_JCoE> <http://purl.org/pav/hasVersion> <hash://sha256/0593e819221d29a6396b0d57cdfe4588ad55276ea6ab50ec4620be5632d5c958> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:5fdcf9f9-7c9a-4ac4-a174-8306cd3a36c1> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:08.374Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:08.468Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:08.469Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/ec62ce2fe7f024ee81026dfb2968ad8aaa0270dd9eb5f80f098088639b2cce09> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/ec62ce2fe7f024ee81026dfb2968ad8aaa0270dd9eb5f80f098088639b2cce09> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:09.898Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjcGOgjAARP-lZ3YDqLiQeNAYIGQxWtMWejEtrVKp4NIaicZ_V5M9b3aOk3lv7sBIKUDkfgbjSTD9CsLJNHDDwA8dcGYHCaLAAc2V9QcDovtrbEEELkb2Hwfdcaa56qyqVGtlzyqrutYAB5ykZYJZtu7lXg0voGNq9y4qZSV4OMBUfaf1Tr2OQZzoS-kPHk3wvvTDi0jwWMShJ4hulm3s0SKb4KTWvKFaJKtuPlCd-3qTp9mWa9iXBLYI0YIVcFQSbzF_p67m26MhrMkISa6DPNZbtMxcieEe33ILyWokBgopEoy74oTjlQtvdM0LSFlxHmQqfj3fxx92JdzTMCcZRLca4lQjROJEprWBp3Cxgf_1mL89m9kMPJ7KoIYZ.Z3h0Zw.a0N2xR9f0Bxb98n8vtHWqmPjOmU> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjcGOgjAARP-lZ3YDqLiQeNAYIGQxWtMWejEtrVKp4NIaicZ_V5M9b3aOk3lv7sBIKUDkfgbjSTD9CsLJNHDDwA8dcGYHCaLAAc2V9QcDovtrbEEELkb2Hwfdcaa56qyqVGtlzyqrutYAB5ykZYJZtu7lXg0voGNq9y4qZSV4OMBUfaf1Tr2OQZzoS-kPHk3wvvTDi0jwWMShJ4hulm3s0SKb4KTWvKFaJKtuPlCd-3qTp9mWa9iXBLYI0YIVcFQSbzF_p67m26MhrMkISa6DPNZbtMxcieEe33ILyWokBgopEoy74oTjlQtvdM0LSFlxHmQqfj3fxx92JdzTMCcZRLca4lQjROJEprWBp3Cxgf_1mL89m9kMPJ7KoIYZ.Z3h0Zw.a0N2xR9f0Bxb98n8vtHWqmPjOmU> <http://purl.org/pav/hasVersion> <hash://sha256/ec62ce2fe7f024ee81026dfb2968ad8aaa0270dd9eb5f80f098088639b2cce09> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:09.899Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/ec62ce2fe7f024ee81026dfb2968ad8aaa0270dd9eb5f80f098088639b2cce09> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/ec62ce2fe7f024ee81026dfb2968ad8aaa0270dd9eb5f80f098088639b2cce09> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:09.900Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjcGOgjAARP-lZ3YDqLiQeNAYIGQxWtMWejEtrVKp4NIaicZ_V5M9b3aOk3lv7sBIKUDkfgbjSTD9CsLJNHDDwA8dcGYHCaLAAc2V9QcDovtrbEEELkb2Hwfdcaa56qyqVGtlzyqrutYAB5ykZYJZtu7lXg0voGNq9y4qZSV4OMBUfaf1Tr2OQZzoS-kPHk3wvvTDi0jwWMShJ4hulm3s0SKb4KTWvKFaJKtuPlCd-3qTp9mWa9iXBLYI0YIVcFQSbzF_p67m26MhrMkISa6DPNZbtMxcieEe33ILyWokBgopEoy74oTjlQtvdM0LSFlxHmQqfj3fxx92JdzTMCcZRLca4lQjROJEprWBp3Cxgf_1mL89m9kMPJ7KoIYZ.Z3h0Zw.a0N2xR9f0Bxb98n8vtHWqmPjOmU> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjcGOgjAARP-lZ3YDqLiQeNAYIGQxWtMWejEtrVKp4NIaicZ_V5M9b3aOk3lv7sBIKUDkfgbjSTD9CsLJNHDDwA8dcGYHCaLAAc2V9QcDovtrbEEELkb2Hwfdcaa56qyqVGtlzyqrutYAB5ykZYJZtu7lXg0voGNq9y4qZSV4OMBUfaf1Tr2OQZzoS-kPHk3wvvTDi0jwWMShJ4hulm3s0SKb4KTWvKFaJKtuPlCd-3qTp9mWa9iXBLYI0YIVcFQSbzF_p67m26MhrMkISa6DPNZbtMxcieEe33ILyWokBgopEoy74oTjlQtvdM0LSFlxHmQqfj3fxx92JdzTMCcZRLca4lQjROJEprWBp3Cxgf_1mL89m9kMPJ7KoIYZ.Z3h0Zw.a0N2xR9f0Bxb98n8vtHWqmPjOmU> <http://purl.org/pav/hasVersion> <hash://sha256/ec62ce2fe7f024ee81026dfb2968ad8aaa0270dd9eb5f80f098088639b2cce09> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:680b6d81-8f1f-4c10-a2c4-154af9ddd538> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:09.901Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:12.431Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:12.432Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:12.645Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <http://purl.org/pav/hasVersion> <hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:12.646Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:12.646Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <http://purl.org/pav/hasVersion> <hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:aae0f3aa-97f3-4eee-9a2d-791c2d87d132> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:12.647Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/ucsb-izc> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:ccb4587c-245d-41fd-81ae-310936c97d3d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:ccb4587c-245d-41fd-81ae-310936c97d3d> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:ccb4587c-245d-41fd-81ae-310936c97d3d> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:12.691Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:ccb4587c-245d-41fd-81ae-310936c97d3d> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:ccb4587c-245d-41fd-81ae-310936c97d3d> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:ccb4587c-245d-41fd-81ae-310936c97d3d> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:13.075Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:ccb4587c-245d-41fd-81ae-310936c97d3d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:ccb4587c-245d-41fd-81ae-310936c97d3d> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <http://purl.org/pav/hasVersion> <hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:ccb4587c-245d-41fd-81ae-310936c97d3d> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:13.076Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:84bb1ed8-01c6-46c8-921f-af142eb63411> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:84bb1ed8-01c6-46c8-921f-af142eb63411> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:84bb1ed8-01c6-46c8-921f-af142eb63411> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:13.117Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:7b45304b-9961-40fe-9571-04c8bde06da5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:7b45304b-9961-40fe-9571-04c8bde06da5> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:7b45304b-9961-40fe-9571-04c8bde06da5> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:13.129Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:9a1235a9-da37-4cfc-a8d0-8593ca48dee8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:9a1235a9-da37-4cfc-a8d0-8593ca48dee8> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:9a1235a9-da37-4cfc-a8d0-8593ca48dee8> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:13.136Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/1a9f4d35f2511e03acf12d4c841a73b9d37964a2ecdde940130338bb07954eaa> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:9a1235a9-da37-4cfc-a8d0-8593ca48dee8> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/1a9f4d35f2511e03acf12d4c841a73b9d37964a2ecdde940130338bb07954eaa> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:9a1235a9-da37-4cfc-a8d0-8593ca48dee8> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:9a1235a9-da37-4cfc-a8d0-8593ca48dee8> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:13.142Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:9a1235a9-da37-4cfc-a8d0-8593ca48dee8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:9a1235a9-da37-4cfc-a8d0-8593ca48dee8> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/globi.json> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<jar:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/1a9f4d35f2511e03acf12d4c841a73b9d37964a2ecdde940130338bb07954eaa> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:9a1235a9-da37-4cfc-a8d0-8593ca48dee8> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:13.143Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:1af06b6c-44d4-4eab-851c-d7ae899c99bb> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:1af06b6c-44d4-4eab-851c-d7ae899c99bb> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:1af06b6c-44d4-4eab-851c-d7ae899c99bb> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:13.550Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:1af06b6c-44d4-4eab-851c-d7ae899c99bb> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:1af06b6c-44d4-4eab-851c-d7ae899c99bb> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:1af06b6c-44d4-4eab-851c-d7ae899c99bb> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:15.406Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:1af06b6c-44d4-4eab-851c-d7ae899c99bb> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:1af06b6c-44d4-4eab-851c-d7ae899c99bb> <http://www.w3.org/ns/prov#used> <https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <http://purl.org/pav/hasVersion> <hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:1af06b6c-44d4-4eab-851c-d7ae899c99bb> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:15.407Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:723a2a2f-97d9-4d59-9ac6-6ba3fba3084f> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:723a2a2f-97d9-4d59-9ac6-6ba3fba3084f> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:723a2a2f-97d9-4d59-9ac6-6ba3fba3084f> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:16.055Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:2adfae9c-50ca-44eb-804e-29b39e30df67> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:2adfae9c-50ca-44eb-804e-29b39e30df67> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:2adfae9c-50ca-44eb-804e-29b39e30df67> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-03T23:36:16.058Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/7fafdc19aa1899121e85f68fe05e4ca917157045e683600f203e921c7b99a426> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:2adfae9c-50ca-44eb-804e-29b39e30df67> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<hash://sha256/7fafdc19aa1899121e85f68fe05e4ca917157045e683600f203e921c7b99a426> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:2adfae9c-50ca-44eb-804e-29b39e30df67> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:2adfae9c-50ca-44eb-804e-29b39e30df67> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-03T23:36:16.060Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:2adfae9c-50ca-44eb-804e-29b39e30df67> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:2adfae9c-50ca-44eb-804e-29b39e30df67> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/interaction_types_mapping.csv> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<jar:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/interaction_types_mapping.csv> <http://purl.org/pav/hasVersion> <hash://sha256/7fafdc19aa1899121e85f68fe05e4ca917157045e683600f203e921c7b99a426> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:2adfae9c-50ca-44eb-804e-29b39e30df67> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:16.060Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n" +
                "<urn:uuid:1b9b1dab-8654-4a7a-a976-b5ee251bb4d2> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-03T23:36:20.668Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:db3d2ce3-6edc-4a8a-b572-1f829fa46073> .\n";

        String[] lines = prov.split("\n");
        DatasetConfigReaderProv datasetConfigReaderProv = new DatasetConfigReaderProv();
        Dataset dataset = null;
        for (String line : lines) {
            try {
                dataset = datasetConfigReaderProv.readConfig(line);
                if (dataset != null) {
                    break;
                }
            } catch (IOException e) {
                //
            }
        }

        assertThat(dataset.getNamespace(), Is.is("globalbioticinteractions/ucsb-izc"));

        assertThat(dataset.getArchiveURI().toString(), Is.is("hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43"));

    }


}