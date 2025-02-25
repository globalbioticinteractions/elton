package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeFactory;
import com.fasterxml.jackson.databind.JsonNode;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.dataset.Dataset;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
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
                "<jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha256/d84999936296e4b85086f2851f4459605502f4eb80b9484049b81d34f43b2ff1> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:34.689Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n";

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
        assertThat(dataset.getConfig(), Is.is(not(nullValue())));
    }

    @Test
    public void readDatasetEltonProvImplicitlyEnded() {

        String provLogGeneratedByElton = "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/template-dataset> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:41389744-0f4d-47e2-8506-76999e1b5c34> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <http://purl.org/pav/hasVersion> <hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:cce97773-a8e2-4af4-94f9-0ac2699cb28e> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/globi.json> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/94bc19a3b0f172f63138fdc9384bb347f110e6fae6d42613a6eba019df6268d2> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:c7b1a849-8230-4e34-a0d5-7b663bc87e01> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/interactions.tsv> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha256/d84999936296e4b85086f2851f4459605502f4eb80b9484049b81d34f43b2ff1> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/template-dataset> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <urn:uuid:efad3d9b-10bb-4d20-b790-02e570ea40e3> .\n";

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
        assertThat(dataset.getConfig(), Is.is(not(nullValue())));
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
        assertThat(dataset.getConfig(), Is.is(not(nullValue())));
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
    public void globiDatasetWithExternalReference() throws IOException {
        String prov = "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<https://preston.guoda.bio> <http://purl.org/dc/terms/description> \"Preston is a software program that finds, archives and provides access to biodiversity datasets.\"@en <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> <http://purl.org/dc/terms/description> \"An event that (re-) processes existing biodiversity datasets graphs and their provenance.\"@en <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:15.833Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> <http://www.w3.org/ns/prov#wasStartedBy> <https://preston.guoda.bio> <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Icaro Alzuru, & Michael Elliott. 2018-2024. Preston: a biodiversity dataset tracker (Version 0.10.3-SNAPSHOT) [Software]. Zenodo. https://doi.org/10.5281/zenodo.1410543\"@en <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:8de5e11b-c8e3-4587-856d-60d15ffbf452> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://purl.org/dc/terms/description> \"Elton helps to access, review and index existing species interaction datasets.\"@en <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <http://purl.org/dc/terms/description> \"Update Local Datasets With Remote Sources\"@en <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:15.756Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <http://www.w3.org/ns/prov#wasStartedBy> <https://globalbioticinteractions.org/elton> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Tobias Kuhn & Katrin Leinweber. (2017/2024). globalbioticinteractions/elton: 0.13.10-SNAPSHOT. Zenodo. https://zenodo.org/doi/10.5281/zenodo.998263\"@en <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:15.828Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:15.828Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/433a6fb7441a60122bfc60a84913a45031b6d7efea7888dec8ba0f6a640ebdbe> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/433a6fb7441a60122bfc60a84913a45031b6d7efea7888dec8ba0f6a640ebdbe> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:18.138Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <http://purl.org/pav/hasVersion> <hash://sha256/433a6fb7441a60122bfc60a84913a45031b6d7efea7888dec8ba0f6a640ebdbe> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:18.138Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/433a6fb7441a60122bfc60a84913a45031b6d7efea7888dec8ba0f6a640ebdbe> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/433a6fb7441a60122bfc60a84913a45031b6d7efea7888dec8ba0f6a640ebdbe> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:18.138Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <http://purl.org/pav/hasVersion> <hash://sha256/433a6fb7441a60122bfc60a84913a45031b6d7efea7888dec8ba0f6a640ebdbe> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:9f947555-cf7b-46c0-8975-966cbfa5de03> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:18.138Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:18.303Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:18.304Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/413023d20a6fe78d2705a7d145df3e5ad68cdac6f503a61b95699b77583354db> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/413023d20a6fe78d2705a7d145df3e5ad68cdac6f503a61b95699b77583354db> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:19.907Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1rwjAARf9LnrvRLx0t7EGRphQqGpekzYskTbRZYyNtxKL436ew57H7eLnn3DsYlZIg9d9nYeyH83kcBVHgf8zmHjjzowJp6IHuyofjCNL7c-xACi6jGt6OxgpuhLZON7p3auCN07YfgQdOynHJHd8M6qCnJ2C53r-KRjsFHh4Ym8Eas9fPY5BBc6nDKWCQHOowuUhIYpklgaSmW_VZwKpiRmBrRMeMhGu7mBhiWHLhyxPJ1j66sY2oEOPVeVK5XC5e0QsLv2IqAoNKWiB8axHJDcY0gypvR3RKllvETBmabZkXO2HQUFPUY8wqXqGopsGvB_GEbCnvCkrhdVLf7Q6vCl8RdCC30iG6juT0X0_5t-f6-QkeP_8ohNM.Z3waMQ.9L7QBrD0lHSBAqm1aDV0aBKoFls> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1rwjAARf9LnrvRLx0t7EGRphQqGpekzYskTbRZYyNtxKL436ew57H7eLnn3DsYlZIg9d9nYeyH83kcBVHgf8zmHjjzowJp6IHuyofjCNL7c-xACi6jGt6OxgpuhLZON7p3auCN07YfgQdOynHJHd8M6qCnJ2C53r-KRjsFHh4Ym8Eas9fPY5BBc6nDKWCQHOowuUhIYpklgaSmW_VZwKpiRmBrRMeMhGu7mBhiWHLhyxPJ1j66sY2oEOPVeVK5XC5e0QsLv2IqAoNKWiB8axHJDcY0gypvR3RKllvETBmabZkXO2HQUFPUY8wqXqGopsGvB_GEbCnvCkrhdVLf7Q6vCl8RdCC30iG6juT0X0_5t-f6-QkeP_8ohNM.Z3waMQ.9L7QBrD0lHSBAqm1aDV0aBKoFls> <http://purl.org/pav/hasVersion> <hash://sha256/413023d20a6fe78d2705a7d145df3e5ad68cdac6f503a61b95699b77583354db> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:19.907Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/413023d20a6fe78d2705a7d145df3e5ad68cdac6f503a61b95699b77583354db> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/413023d20a6fe78d2705a7d145df3e5ad68cdac6f503a61b95699b77583354db> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:19.908Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1rwjAARf9LnrvRLx0t7EGRphQqGpekzYskTbRZYyNtxKL436ew57H7eLnn3DsYlZIg9d9nYeyH83kcBVHgf8zmHjjzowJp6IHuyofjCNL7c-xACi6jGt6OxgpuhLZON7p3auCN07YfgQdOynHJHd8M6qCnJ2C53r-KRjsFHh4Ym8Eas9fPY5BBc6nDKWCQHOowuUhIYpklgaSmW_VZwKpiRmBrRMeMhGu7mBhiWHLhyxPJ1j66sY2oEOPVeVK5XC5e0QsLv2IqAoNKWiB8axHJDcY0gypvR3RKllvETBmabZkXO2HQUFPUY8wqXqGopsGvB_GEbCnvCkrhdVLf7Q6vCl8RdCC30iG6juT0X0_5t-f6-QkeP_8ohNM.Z3waMQ.9L7QBrD0lHSBAqm1aDV0aBKoFls> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1rwjAARf9LnrvRLx0t7EGRphQqGpekzYskTbRZYyNtxKL436ew57H7eLnn3DsYlZIg9d9nYeyH83kcBVHgf8zmHjjzowJp6IHuyofjCNL7c-xACi6jGt6OxgpuhLZON7p3auCN07YfgQdOynHJHd8M6qCnJ2C53r-KRjsFHh4Ym8Eas9fPY5BBc6nDKWCQHOowuUhIYpklgaSmW_VZwKpiRmBrRMeMhGu7mBhiWHLhyxPJ1j66sY2oEOPVeVK5XC5e0QsLv2IqAoNKWiB8axHJDcY0gypvR3RKllvETBmabZkXO2HQUFPUY8wqXqGopsGvB_GEbCnvCkrhdVLf7Q6vCl8RdCC30iG6juT0X0_5t-f6-QkeP_8ohNM.Z3waMQ.9L7QBrD0lHSBAqm1aDV0aBKoFls> <http://purl.org/pav/hasVersion> <hash://sha256/413023d20a6fe78d2705a7d145df3e5ad68cdac6f503a61b95699b77583354db> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:e1b1a418-dce9-4c47-8caa-91aadf66a9b5> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:19.908Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:19.976Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:19.976Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/c2da5767cb7dbf746ff151304c4a69bc55e4452d965e045c595e8752fc12556a> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/c2da5767cb7dbf746ff151304c4a69bc55e4452d965e045c595e8752fc12556a> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:21.476Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1rwjAARf9LnrvRD93Wgg-KNKVQ0bgkbV4kaaLNGhtpIxbF_z4Fn8fu4-Wec29gUEqCxH__-JzGUTQN4-jL9-Ng4oETPyiQRB5oL7w_DCC5PcYOJOA8qP7tYKzgRmjrdK07p3peO227AXjgqByX3PF1r_Z6fACW692zqLVT4O6Boe6tMTv9OAYpNOcqHAMGyb4K47OEZCLTOJDUtMsuDViZTwlsjGiZkXBl5yNDDEsufHkk6cpHV7YWJWK8PI0qk4v5M3pu4feEisCgguYIXxtEMoMxTaHKmgEd48UGMVOEZlNk-VYY1FcUdRizkpcoqmjw8iAekw3lbU4pvIzqp9niZe4rgvbkWjhEV5Ec_-sp_vZcZjNw_wUdZ4Tn.Z3waMw._lrZVN5TWCXSgw2AEKOsMCd331w> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1rwjAARf9LnrvRD93Wgg-KNKVQ0bgkbV4kaaLNGhtpIxbF_z4Fn8fu4-Wec29gUEqCxH__-JzGUTQN4-jL9-Ng4oETPyiQRB5oL7w_DCC5PcYOJOA8qP7tYKzgRmjrdK07p3peO227AXjgqByX3PF1r_Z6fACW692zqLVT4O6Boe6tMTv9OAYpNOcqHAMGyb4K47OEZCLTOJDUtMsuDViZTwlsjGiZkXBl5yNDDEsufHkk6cpHV7YWJWK8PI0qk4v5M3pu4feEisCgguYIXxtEMoMxTaHKmgEd48UGMVOEZlNk-VYY1FcUdRizkpcoqmjw8iAekw3lbU4pvIzqp9niZe4rgvbkWjhEV5Ec_-sp_vZcZjNw_wUdZ4Tn.Z3waMw._lrZVN5TWCXSgw2AEKOsMCd331w> <http://purl.org/pav/hasVersion> <hash://sha256/c2da5767cb7dbf746ff151304c4a69bc55e4452d965e045c595e8752fc12556a> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:21.477Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/c2da5767cb7dbf746ff151304c4a69bc55e4452d965e045c595e8752fc12556a> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/c2da5767cb7dbf746ff151304c4a69bc55e4452d965e045c595e8752fc12556a> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:21.478Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1rwjAARf9LnrvRD93Wgg-KNKVQ0bgkbV4kaaLNGhtpIxbF_z4Fn8fu4-Wec29gUEqCxH__-JzGUTQN4-jL9-Ng4oETPyiQRB5oL7w_DCC5PcYOJOA8qP7tYKzgRmjrdK07p3peO227AXjgqByX3PF1r_Z6fACW692zqLVT4O6Boe6tMTv9OAYpNOcqHAMGyb4K47OEZCLTOJDUtMsuDViZTwlsjGiZkXBl5yNDDEsufHkk6cpHV7YWJWK8PI0qk4v5M3pu4feEisCgguYIXxtEMoMxTaHKmgEd48UGMVOEZlNk-VYY1FcUdRizkpcoqmjw8iAekw3lbU4pvIzqp9niZe4rgvbkWjhEV5Ec_-sp_vZcZjNw_wUdZ4Tn.Z3waMw._lrZVN5TWCXSgw2AEKOsMCd331w> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1rwjAARf9LnrvRD93Wgg-KNKVQ0bgkbV4kaaLNGhtpIxbF_z4Fn8fu4-Wec29gUEqCxH__-JzGUTQN4-jL9-Ng4oETPyiQRB5oL7w_DCC5PcYOJOA8qP7tYKzgRmjrdK07p3peO227AXjgqByX3PF1r_Z6fACW692zqLVT4O6Boe6tMTv9OAYpNOcqHAMGyb4K47OEZCLTOJDUtMsuDViZTwlsjGiZkXBl5yNDDEsufHkk6cpHV7YWJWK8PI0qk4v5M3pu4feEisCgguYIXxtEMoMxTaHKmgEd48UGMVOEZlNk-VYY1FcUdRizkpcoqmjw8iAekw3lbU4pvIzqp9niZe4rgvbkWjhEV5Ec_-sp_vZcZjNw_wUdZ4Tn.Z3waMw._lrZVN5TWCXSgw2AEKOsMCd331w> <http://purl.org/pav/hasVersion> <hash://sha256/c2da5767cb7dbf746ff151304c4a69bc55e4452d965e045c595e8752fc12556a> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:c8652781-0d3f-4af5-acd6-8285a0b5188a> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:21.479Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:21.534Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:21.534Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/4a289e2695fdb0310374cdc1dc1baf7ce4bb1b5a5567ecda78454c7f6725e980> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/4a289e2695fdb0310374cdc1dc1baf7ce4bb1b5a5567ecda78454c7f6725e980> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:24.319Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjU2PgjAARP9Lz-ymIH5A4gFjKCHBaF1a6MW0tEqXCgZqJBr_-2qy583OcTLvzQMMSkkQwk8_CLz5HM7gbL6AU3fhgAs_KRD6DmhuvD8NIHy8xhaE4Dqo_uNkOsGN0J3VlW6t6nllddcOwAFnZbnklm97ddTjC-i4PryLSlsFng4Yqr4z5qBfxyBG5lp6o8sQOZZecJWI-DIOXElNs25jlxXplKDaiIYZiTZdNDLMcskFlGcSbyC-s60oMOPFZVSJXEXv6KhDXz4VrsEZTXF-rzFJTJ7TGKmkHvA5WO0wM5lndlmS7oXBfUlxm-es4AWelNT99WAekB3lTUopuo3qu97n6xQqgo_knllMNxM5_teT_e25LZfg-QMmM4Tt.Z3waNA.cc65xTePGv74K6pTnA1oZw93uZI> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjU2PgjAARP9Lz-ymIH5A4gFjKCHBaF1a6MW0tEqXCgZqJBr_-2qy583OcTLvzQMMSkkQwk8_CLz5HM7gbL6AU3fhgAs_KRD6DmhuvD8NIHy8xhaE4Dqo_uNkOsGN0J3VlW6t6nllddcOwAFnZbnklm97ddTjC-i4PryLSlsFng4Yqr4z5qBfxyBG5lp6o8sQOZZecJWI-DIOXElNs25jlxXplKDaiIYZiTZdNDLMcskFlGcSbyC-s60oMOPFZVSJXEXv6KhDXz4VrsEZTXF-rzFJTJ7TGKmkHvA5WO0wM5lndlmS7oXBfUlxm-es4AWelNT99WAekB3lTUopuo3qu97n6xQqgo_knllMNxM5_teT_e25LZfg-QMmM4Tt.Z3waNA.cc65xTePGv74K6pTnA1oZw93uZI> <http://purl.org/pav/hasVersion> <hash://sha256/4a289e2695fdb0310374cdc1dc1baf7ce4bb1b5a5567ecda78454c7f6725e980> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:24.320Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/4a289e2695fdb0310374cdc1dc1baf7ce4bb1b5a5567ecda78454c7f6725e980> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/4a289e2695fdb0310374cdc1dc1baf7ce4bb1b5a5567ecda78454c7f6725e980> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:24.320Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjU2PgjAARP9Lz-ymIH5A4gFjKCHBaF1a6MW0tEqXCgZqJBr_-2qy583OcTLvzQMMSkkQwk8_CLz5HM7gbL6AU3fhgAs_KRD6DmhuvD8NIHy8xhaE4Dqo_uNkOsGN0J3VlW6t6nllddcOwAFnZbnklm97ddTjC-i4PryLSlsFng4Yqr4z5qBfxyBG5lp6o8sQOZZecJWI-DIOXElNs25jlxXplKDaiIYZiTZdNDLMcskFlGcSbyC-s60oMOPFZVSJXEXv6KhDXz4VrsEZTXF-rzFJTJ7TGKmkHvA5WO0wM5lndlmS7oXBfUlxm-es4AWelNT99WAekB3lTUopuo3qu97n6xQqgo_knllMNxM5_teT_e25LZfg-QMmM4Tt.Z3waNA.cc65xTePGv74K6pTnA1oZw93uZI> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjU2PgjAARP9Lz-ymIH5A4gFjKCHBaF1a6MW0tEqXCgZqJBr_-2qy583OcTLvzQMMSkkQwk8_CLz5HM7gbL6AU3fhgAs_KRD6DmhuvD8NIHy8xhaE4Dqo_uNkOsGN0J3VlW6t6nllddcOwAFnZbnklm97ddTjC-i4PryLSlsFng4Yqr4z5qBfxyBG5lp6o8sQOZZecJWI-DIOXElNs25jlxXplKDaiIYZiTZdNDLMcskFlGcSbyC-s60oMOPFZVSJXEXv6KhDXz4VrsEZTXF-rzFJTJ7TGKmkHvA5WO0wM5lndlmS7oXBfUlxm-es4AWelNT99WAekB3lTUopuo3qu97n6xQqgo_knllMNxM5_teT_e25LZfg-QMmM4Tt.Z3waNA.cc65xTePGv74K6pTnA1oZw93uZI> <http://purl.org/pav/hasVersion> <hash://sha256/4a289e2695fdb0310374cdc1dc1baf7ce4bb1b5a5567ecda78454c7f6725e980> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bb037f6e-3357-4193-8a9c-62dcb69b76b6> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:24.321Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:24.386Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:24.387Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/ce7f2f0601d0826d2936daf777bd8548d6ce93089f287dc30e94ccc31ba5f119> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/ce7f2f0601d0826d2936daf777bd8548d6ce93089f287dc30e94ccc31ba5f119> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:26.150Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV9rwjAAxL9Lnt1I_Tdb8EGRphQqGpekzYskTbRZYyNtxKLsu6_Cnsfu6TjufvcEndYKRPA9XMwXUxgGEwg_Zov5bASu4qxBNJj6LtpzB6LnUPYgArdOt29n66Sw0jhvStN43YrSG9d0YAQu2gslvNi1-mT6YeCEOb6C0ngNvkegK1tn7dEMxyBG9laM-4AjeirG4U0hOlVxGChm600TBzxPZxRVVtbcKrR1q55jTpSQUF1ovIX4wXcyx1zk114nar16yawc-pwyGVicsRSTR4VpYglhMdJJ1eFLuN5jbrOx3WdJepAWtwXDDSE8FzmeFCz45WAR0j0TdcoYuvf6qzqQTQo1xSf6yDxm24nq_8vJ_ubcl0vw_QMnpYTu.Z3waNw.X3zF8y6-I95L9VYGwH3QxeDWYhw> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV9rwjAAxL9Lnt1I_Tdb8EGRphQqGpekzYskTbRZYyNtxKLsu6_Cnsfu6TjufvcEndYKRPA9XMwXUxgGEwg_Zov5bASu4qxBNJj6LtpzB6LnUPYgArdOt29n66Sw0jhvStN43YrSG9d0YAQu2gslvNi1-mT6YeCEOb6C0ngNvkegK1tn7dEMxyBG9laM-4AjeirG4U0hOlVxGChm600TBzxPZxRVVtbcKrR1q55jTpSQUF1ovIX4wXcyx1zk114nar16yawc-pwyGVicsRSTR4VpYglhMdJJ1eFLuN5jbrOx3WdJepAWtwXDDSE8FzmeFCz45WAR0j0TdcoYuvf6qzqQTQo1xSf6yDxm24nq_8vJ_ubcl0vw_QMnpYTu.Z3waNw.X3zF8y6-I95L9VYGwH3QxeDWYhw> <http://purl.org/pav/hasVersion> <hash://sha256/ce7f2f0601d0826d2936daf777bd8548d6ce93089f287dc30e94ccc31ba5f119> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:26.150Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/ce7f2f0601d0826d2936daf777bd8548d6ce93089f287dc30e94ccc31ba5f119> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/ce7f2f0601d0826d2936daf777bd8548d6ce93089f287dc30e94ccc31ba5f119> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:26.150Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV9rwjAAxL9Lnt1I_Tdb8EGRphQqGpekzYskTbRZYyNtxKLsu6_Cnsfu6TjufvcEndYKRPA9XMwXUxgGEwg_Zov5bASu4qxBNJj6LtpzB6LnUPYgArdOt29n66Sw0jhvStN43YrSG9d0YAQu2gslvNi1-mT6YeCEOb6C0ngNvkegK1tn7dEMxyBG9laM-4AjeirG4U0hOlVxGChm600TBzxPZxRVVtbcKrR1q55jTpSQUF1ovIX4wXcyx1zk114nar16yawc-pwyGVicsRSTR4VpYglhMdJJ1eFLuN5jbrOx3WdJepAWtwXDDSE8FzmeFCz45WAR0j0TdcoYuvf6qzqQTQo1xSf6yDxm24nq_8vJ_ubcl0vw_QMnpYTu.Z3waNw.X3zF8y6-I95L9VYGwH3QxeDWYhw> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV9rwjAAxL9Lnt1I_Tdb8EGRphQqGpekzYskTbRZYyNtxKLsu6_Cnsfu6TjufvcEndYKRPA9XMwXUxgGEwg_Zov5bASu4qxBNJj6LtpzB6LnUPYgArdOt29n66Sw0jhvStN43YrSG9d0YAQu2gslvNi1-mT6YeCEOb6C0ngNvkegK1tn7dEMxyBG9laM-4AjeirG4U0hOlVxGChm600TBzxPZxRVVtbcKrR1q55jTpSQUF1ovIX4wXcyx1zk114nar16yawc-pwyGVicsRSTR4VpYglhMdJJ1eFLuN5jbrOx3WdJepAWtwXDDSE8FzmeFCz45WAR0j0TdcoYuvf6qzqQTQo1xSf6yDxm24nq_8vJ_ubcl0vw_QMnpYTu.Z3waNw.X3zF8y6-I95L9VYGwH3QxeDWYhw> <http://purl.org/pav/hasVersion> <hash://sha256/ce7f2f0601d0826d2936daf777bd8548d6ce93089f287dc30e94ccc31ba5f119> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:bfa0de4b-bdad-4349-a5e2-8d06af59b826> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:26.151Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:26.186Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:26.186Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/92e716834d87b01232395fa6ad6c302d3e585eb6408884d2335e22be341fd653> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/92e716834d87b01232395fa6ad6c302d3e585eb6408884d2335e22be341fd653> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:27.076Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1vgjAARf9Ln9nCh7iVxAeNoYQEo3W00BdTaJWOSg3USDT-92Gy52X38eaecx9gkFKAyH33AxfCwA_hZxj6H-E8cMCFnySI5g5ob7w_DSB6TGsLInAdZP920qbiulLGqlp1Vva8tsp0A3DAWVouuOXbXh7VOAGGq8OrqJWV4OmAoe6N1gc1PYMY6Wvpjx5D5Fj68CoQmYkYeoLqdt3FHivSkKBGVy3TAm3McmSY5YJXrjiTeOPiO9tWBWa8uIwyEavlK2pp0NeMVp7GGU1xfm8wSXSe0xjJpBnwGa52mOnM17ssSfeVxn1JcZfnrOAFDkrq_Xowh2RHeZtSim6j_G72-Tp1JcFHcs8spptAjP_1ZH97bosFeP4AeyyFIw.Z3waOQ.DFWsJzaHP5vOO-Goi5tCjH7cGV4> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1vgjAARf9Ln9nCh7iVxAeNoYQEo3W00BdTaJWOSg3USDT-92Gy52X38eaecx9gkFKAyH33AxfCwA_hZxj6H-E8cMCFnySI5g5ob7w_DSB6TGsLInAdZP920qbiulLGqlp1Vva8tsp0A3DAWVouuOXbXh7VOAGGq8OrqJWV4OmAoe6N1gc1PYMY6Wvpjx5D5Fj68CoQmYkYeoLqdt3FHivSkKBGVy3TAm3McmSY5YJXrjiTeOPiO9tWBWa8uIwyEavlK2pp0NeMVp7GGU1xfm8wSXSe0xjJpBnwGa52mOnM17ssSfeVxn1JcZfnrOAFDkrq_Xowh2RHeZtSim6j_G72-Tp1JcFHcs8spptAjP_1ZH97bosFeP4AeyyFIw.Z3waOQ.DFWsJzaHP5vOO-Goi5tCjH7cGV4> <http://purl.org/pav/hasVersion> <hash://sha256/92e716834d87b01232395fa6ad6c302d3e585eb6408884d2335e22be341fd653> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:27.076Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/92e716834d87b01232395fa6ad6c302d3e585eb6408884d2335e22be341fd653> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/92e716834d87b01232395fa6ad6c302d3e585eb6408884d2335e22be341fd653> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:27.076Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1vgjAARf9Ln9nCh7iVxAeNoYQEo3W00BdTaJWOSg3USDT-92Gy52X38eaecx9gkFKAyH33AxfCwA_hZxj6H-E8cMCFnySI5g5ob7w_DSB6TGsLInAdZP920qbiulLGqlp1Vva8tsp0A3DAWVouuOXbXh7VOAGGq8OrqJWV4OmAoe6N1gc1PYMY6Wvpjx5D5Fj68CoQmYkYeoLqdt3FHivSkKBGVy3TAm3McmSY5YJXrjiTeOPiO9tWBWa8uIwyEavlK2pp0NeMVp7GGU1xfm8wSXSe0xjJpBnwGa52mOnM17ssSfeVxn1JcZfnrOAFDkrq_Xowh2RHeZtSim6j_G72-Tp1JcFHcs8spptAjP_1ZH97bosFeP4AeyyFIw.Z3waOQ.DFWsJzaHP5vOO-Goi5tCjH7cGV4> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjV1vgjAARf9Ln9nCh7iVxAeNoYQEo3W00BdTaJWOSg3USDT-92Gy52X38eaecx9gkFKAyH33AxfCwA_hZxj6H-E8cMCFnySI5g5ob7w_DSB6TGsLInAdZP920qbiulLGqlp1Vva8tsp0A3DAWVouuOXbXh7VOAGGq8OrqJWV4OmAoe6N1gc1PYMY6Wvpjx5D5Fj68CoQmYkYeoLqdt3FHivSkKBGVy3TAm3McmSY5YJXrjiTeOPiO9tWBWa8uIwyEavlK2pp0NeMVp7GGU1xfm8wSXSe0xjJpBnwGa52mOnM17ssSfeVxn1JcZfnrOAFDkrq_Xowh2RHeZtSim6j_G72-Tp1JcFHcs8spptAjP_1ZH97bosFeP4AeyyFIw.Z3waOQ.DFWsJzaHP5vOO-Goi5tCjH7cGV4> <http://purl.org/pav/hasVersion> <hash://sha256/92e716834d87b01232395fa6ad6c302d3e585eb6408884d2335e22be341fd653> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:18e1f1eb-7886-416a-9c6a-accc3a6af957> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:27.076Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:28.887Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:28.887Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:29.163Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <http://purl.org/pav/hasVersion> <hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:29.164Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:29.164Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <http://purl.org/pav/hasVersion> <hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:266d2d4d-65a2-457a-bc8b-944f99cbb043> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:29.165Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/ucsb-izc> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:29.176Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:29.177Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:29.228Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <http://purl.org/pav/hasVersion> <hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:29.229Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:29.229Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/info/refs?service=git-upload-pack> <http://purl.org/pav/hasVersion> <hash://sha256/cb7bf347dd51ccbfb0d82c2453e8a2ef0c7712c78461c7a8e0fe7545821084f3> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:2e5e62a5-cb98-4d5f-8e85-d5d0dabdc7ad> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:29.229Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:22e26737-2ad4-4e56-9a88-c44616faab79> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:22e26737-2ad4-4e56-9a88-c44616faab79> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:22e26737-2ad4-4e56-9a88-c44616faab79> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:29.243Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:22e26737-2ad4-4e56-9a88-c44616faab79> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:22e26737-2ad4-4e56-9a88-c44616faab79> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:22e26737-2ad4-4e56-9a88-c44616faab79> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:29.639Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:22e26737-2ad4-4e56-9a88-c44616faab79> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:22e26737-2ad4-4e56-9a88-c44616faab79> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <http://purl.org/pav/hasVersion> <hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:22e26737-2ad4-4e56-9a88-c44616faab79> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:29.640Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:38603073-d24a-4d8e-82ba-cb4e6b2f3038> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:38603073-d24a-4d8e-82ba-cb4e6b2f3038> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:38603073-d24a-4d8e-82ba-cb4e6b2f3038> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:29.660Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:95eb64f4-ff7d-47bb-a6cc-691b6fc1e9d1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:95eb64f4-ff7d-47bb-a6cc-691b6fc1e9d1> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:95eb64f4-ff7d-47bb-a6cc-691b6fc1e9d1> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:29.664Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:746792af-a53e-45f8-a804-848bb7cf10a4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:746792af-a53e-45f8-a804-848bb7cf10a4> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:746792af-a53e-45f8-a804-848bb7cf10a4> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:29.667Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/1a9f4d35f2511e03acf12d4c841a73b9d37964a2ecdde940130338bb07954eaa> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:746792af-a53e-45f8-a804-848bb7cf10a4> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/1a9f4d35f2511e03acf12d4c841a73b9d37964a2ecdde940130338bb07954eaa> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:746792af-a53e-45f8-a804-848bb7cf10a4> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:746792af-a53e-45f8-a804-848bb7cf10a4> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:29.669Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:746792af-a53e-45f8-a804-848bb7cf10a4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:746792af-a53e-45f8-a804-848bb7cf10a4> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/globi.json> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<jar:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/1a9f4d35f2511e03acf12d4c841a73b9d37964a2ecdde940130338bb07954eaa> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:746792af-a53e-45f8-a804-848bb7cf10a4> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:29.669Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:f28b50fd-9e84-439b-a112-e6beb9706df0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:f28b50fd-9e84-439b-a112-e6beb9706df0> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:f28b50fd-9e84-439b-a112-e6beb9706df0> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:29.854Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:f28b50fd-9e84-439b-a112-e6beb9706df0> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:f28b50fd-9e84-439b-a112-e6beb9706df0> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:f28b50fd-9e84-439b-a112-e6beb9706df0> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:30.920Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:f28b50fd-9e84-439b-a112-e6beb9706df0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:f28b50fd-9e84-439b-a112-e6beb9706df0> <http://www.w3.org/ns/prov#used> <https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <http://purl.org/pav/hasVersion> <hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:f28b50fd-9e84-439b-a112-e6beb9706df0> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:30.920Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:da13a225-7d02-4b2d-8454-5c0937f268be> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:da13a225-7d02-4b2d-8454-5c0937f268be> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:da13a225-7d02-4b2d-8454-5c0937f268be> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:31.303Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:a78bed27-b096-4163-a20c-f61ac5622ec4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:a78bed27-b096-4163-a20c-f61ac5622ec4> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:a78bed27-b096-4163-a20c-f61ac5622ec4> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-06T18:00:31.306Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/7fafdc19aa1899121e85f68fe05e4ca917157045e683600f203e921c7b99a426> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:a78bed27-b096-4163-a20c-f61ac5622ec4> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<hash://sha256/7fafdc19aa1899121e85f68fe05e4ca917157045e683600f203e921c7b99a426> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:a78bed27-b096-4163-a20c-f61ac5622ec4> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:a78bed27-b096-4163-a20c-f61ac5622ec4> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-06T18:00:31.307Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:a78bed27-b096-4163-a20c-f61ac5622ec4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:a78bed27-b096-4163-a20c-f61ac5622ec4> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/interaction_types_mapping.csv> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<jar:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/interaction_types_mapping.csv> <http://purl.org/pav/hasVersion> <hash://sha256/7fafdc19aa1899121e85f68fe05e4ca917157045e683600f203e921c7b99a426> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:a78bed27-b096-4163-a20c-f61ac5622ec4> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:31.307Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
                "<urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:34.689Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n";

        String[] lines = prov.split("\n");

        List<URI> requested = new ArrayList<>();


        DatasetConfigReaderProv datasetConfigReaderProv = new DatasetConfigReaderProv(new ResourceService() {
            @Override
            public InputStream retrieve(URI uri) throws IOException {
                requested.add(uri);
                return null;
            }
        });
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

        dataset.retrieve(URI.create("https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip"));

        assertThat(requested.size(), Is.is(1));

        assertThat(requested.get(0).toString(), Is.is("hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423"));

        requested.clear();

        dataset.retrieve(URI.create("https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip"));

        assertThat(requested.size(), Is.is(1));

        assertThat(requested.get(0).toString(), Is.is("hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43"));

    }
    @Test
    public void globiDatasetLocalDirectory() throws IOException {
        String prov = "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<https://preston.guoda.bio> <http://purl.org/dc/terms/description> \"Preston is a software program that finds, archives and provides access to biodiversity datasets.\"@en <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> <http://purl.org/dc/terms/description> \"An event that (re-) processes existing biodiversity datasets graphs and their provenance.\"@en <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:16.013Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> <http://www.w3.org/ns/prov#wasStartedBy> <https://preston.guoda.bio> <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Icaro Alzuru, & Michael Elliott. 2018-2024. Preston: a biodiversity dataset tracker (Version 0.10.5) [Software]. Zenodo. https://doi.org/10.5281/zenodo.1410543\"@en <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:19dd8d60-0ccf-46ab-abc5-d03fdd6dec75> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://purl.org/dc/terms/description> \"Elton helps to access, review and index existing species interaction datasets.\"@en <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <http://purl.org/dc/terms/description> \"Update Local Datasets With Remote Sources\"@en <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:15.918Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <http://www.w3.org/ns/prov#wasStartedBy> <https://globalbioticinteractions.org/elton> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Tobias Kuhn & Katrin Leinweber. (2017/2024). globalbioticinteractions/elton: 0.15.0. Zenodo. https://zenodo.org/doi/10.5281/zenodo.998263\"@en <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:lsid:globalbioticinteractions.org:local> <http://www.w3.org/ns/prov#wasAssociatedWith> <file:///home/runner/work/ecoab-host-plant/ecoab-host-plant/./> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<file:///home/runner/work/ecoab-host-plant/ecoab-host-plant/./> <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:63dc56f2-df4d-4127-926f-6051a75379e1> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:63dc56f2-df4d-4127-926f-6051a75379e1> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:63dc56f2-df4d-4127-926f-6051a75379e1> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:16.069Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:f9a0fc23-42a5-49b9-91d4-85d2713cdf2f> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:f9a0fc23-42a5-49b9-91d4-85d2713cdf2f> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:f9a0fc23-42a5-49b9-91d4-85d2713cdf2f> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:16.081Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:021c91aa-ce6a-4e24-9fbe-d532ac62d18f> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:021c91aa-ce6a-4e24-9fbe-d532ac62d18f> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:021c91aa-ce6a-4e24-9fbe-d532ac62d18f> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:16.081Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/0ecb4e1f4a8d1b66b37fb379492d79813448edc1971236f62e0de104773b68db> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:021c91aa-ce6a-4e24-9fbe-d532ac62d18f> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/0ecb4e1f4a8d1b66b37fb379492d79813448edc1971236f62e0de104773b68db> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:021c91aa-ce6a-4e24-9fbe-d532ac62d18f> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:021c91aa-ce6a-4e24-9fbe-d532ac62d18f> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-25T20:17:16.112Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:021c91aa-ce6a-4e24-9fbe-d532ac62d18f> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:021c91aa-ce6a-4e24-9fbe-d532ac62d18f> <http://www.w3.org/ns/prov#used> <file:///home/runner/work/ecoab-host-plant/ecoab-host-plant/./globi.json> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<file:///home/runner/work/ecoab-host-plant/ecoab-host-plant/./globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/0ecb4e1f4a8d1b66b37fb379492d79813448edc1971236f62e0de104773b68db> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:021c91aa-ce6a-4e24-9fbe-d532ac62d18f> <http://www.w3.org/ns/prov#endedAtTime> \"2025-02-25T20:17:16.113Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:67c8d48e-4ea5-4e4c-a303-ca00850f4800> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:67c8d48e-4ea5-4e4c-a303-ca00850f4800> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:67c8d48e-4ea5-4e4c-a303-ca00850f4800> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:16.318Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/c96eaf6f0ebdfdcd9c8cec0968007f9149d476ca2d4423ba14c3e2c82318f8dc> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:67c8d48e-4ea5-4e4c-a303-ca00850f4800> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/c96eaf6f0ebdfdcd9c8cec0968007f9149d476ca2d4423ba14c3e2c82318f8dc> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:67c8d48e-4ea5-4e4c-a303-ca00850f4800> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:67c8d48e-4ea5-4e4c-a303-ca00850f4800> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-25T20:17:16.336Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:67c8d48e-4ea5-4e4c-a303-ca00850f4800> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:67c8d48e-4ea5-4e4c-a303-ca00850f4800> <http://www.w3.org/ns/prov#used> <file:///home/runner/work/ecoab-host-plant/ecoab-host-plant/./dwca-ecoab-host-plant-v1.10.zip> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<file:///home/runner/work/ecoab-host-plant/ecoab-host-plant/./dwca-ecoab-host-plant-v1.10.zip> <http://purl.org/pav/hasVersion> <hash://sha256/c96eaf6f0ebdfdcd9c8cec0968007f9149d476ca2d4423ba14c3e2c82318f8dc> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:67c8d48e-4ea5-4e4c-a303-ca00850f4800> <http://www.w3.org/ns/prov#endedAtTime> \"2025-02-25T20:17:16.336Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:b297c352-c307-4ffd-ba6e-6891153f098b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:b297c352-c307-4ffd-ba6e-6891153f098b> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:b297c352-c307-4ffd-ba6e-6891153f098b> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:18.951Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:35ec03e6-321c-4951-a867-c76f09e6b824> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:35ec03e6-321c-4951-a867-c76f09e6b824> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:35ec03e6-321c-4951-a867-c76f09e6b824> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:18.952Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:9935c8e9-0e34-48b4-99fe-ae778dda3c18> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:9935c8e9-0e34-48b4-99fe-ae778dda3c18> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:9935c8e9-0e34-48b4-99fe-ae778dda3c18> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:18.954Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/f49f665c540214e7d00466e359821de1bc03206f8373d4974220d608ed7b98f3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:9935c8e9-0e34-48b4-99fe-ae778dda3c18> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/f49f665c540214e7d00466e359821de1bc03206f8373d4974220d608ed7b98f3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:9935c8e9-0e34-48b4-99fe-ae778dda3c18> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:9935c8e9-0e34-48b4-99fe-ae778dda3c18> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-25T20:17:18.958Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:9935c8e9-0e34-48b4-99fe-ae778dda3c18> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:9935c8e9-0e34-48b4-99fe-ae778dda3c18> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_ignored.csv> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_ignored.csv> <http://purl.org/pav/hasVersion> <hash://sha256/f49f665c540214e7d00466e359821de1bc03206f8373d4974220d608ed7b98f3> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:9935c8e9-0e34-48b4-99fe-ae778dda3c18> <http://www.w3.org/ns/prov#endedAtTime> \"2025-02-25T20:17:18.959Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:d2e184bb-43ea-4b34-9f5f-472b64fd9e11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:d2e184bb-43ea-4b34-9f5f-472b64fd9e11> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:d2e184bb-43ea-4b34-9f5f-472b64fd9e11> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:18.966Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:d2e184bb-43ea-4b34-9f5f-472b64fd9e11> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:d2e184bb-43ea-4b34-9f5f-472b64fd9e11> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:d2e184bb-43ea-4b34-9f5f-472b64fd9e11> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-25T20:17:18.967Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:d2e184bb-43ea-4b34-9f5f-472b64fd9e11> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:d2e184bb-43ea-4b34-9f5f-472b64fd9e11> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_mapping.csv> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_mapping.csv> <http://purl.org/pav/hasVersion> <hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:d2e184bb-43ea-4b34-9f5f-472b64fd9e11> <http://www.w3.org/ns/prov#endedAtTime> \"2025-02-25T20:17:18.967Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:ff6ddce4-cc70-4b49-a8ae-5aed3e6a2c15> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:ff6ddce4-cc70-4b49-a8ae-5aed3e6a2c15> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:ff6ddce4-cc70-4b49-a8ae-5aed3e6a2c15> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:18.986Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/43aba7b90c686a4890aebd4a90a02d6f82259664524bdad1b22102a29fe9fa07> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:ff6ddce4-cc70-4b49-a8ae-5aed3e6a2c15> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/43aba7b90c686a4890aebd4a90a02d6f82259664524bdad1b22102a29fe9fa07> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:ff6ddce4-cc70-4b49-a8ae-5aed3e6a2c15> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:ff6ddce4-cc70-4b49-a8ae-5aed3e6a2c15> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-25T20:17:18.987Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:ff6ddce4-cc70-4b49-a8ae-5aed3e6a2c15> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:ff6ddce4-cc70-4b49-a8ae-5aed3e6a2c15> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_ro_unmapped.csv> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_ro_unmapped.csv> <http://purl.org/pav/hasVersion> <hash://sha256/43aba7b90c686a4890aebd4a90a02d6f82259664524bdad1b22102a29fe9fa07> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:ff6ddce4-cc70-4b49-a8ae-5aed3e6a2c15> <http://www.w3.org/ns/prov#endedAtTime> \"2025-02-25T20:17:18.987Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:10abb0b9-7b74-4aec-bb80-189471d91ede> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:10abb0b9-7b74-4aec-bb80-189471d91ede> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:10abb0b9-7b74-4aec-bb80-189471d91ede> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-25T20:17:18.988Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/7dc2797003a95ac7b97be06fda48b4dc25e7a555a2839a19ad7dc7a148427e43> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:10abb0b9-7b74-4aec-bb80-189471d91ede> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<hash://sha256/7dc2797003a95ac7b97be06fda48b4dc25e7a555a2839a19ad7dc7a148427e43> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:10abb0b9-7b74-4aec-bb80-189471d91ede> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:10abb0b9-7b74-4aec-bb80-189471d91ede> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-25T20:17:18.989Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:10abb0b9-7b74-4aec-bb80-189471d91ede> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:10abb0b9-7b74-4aec-bb80-189471d91ede> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_ro.csv> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_ro.csv> <http://purl.org/pav/hasVersion> <hash://sha256/7dc2797003a95ac7b97be06fda48b4dc25e7a555a2839a19ad7dc7a148427e43> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:10abb0b9-7b74-4aec-bb80-189471d91ede> <http://www.w3.org/ns/prov#endedAtTime> \"2025-02-25T20:17:18.989Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n" +
                "<urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> <http://www.w3.org/ns/prov#endedAtTime> \"2025-02-25T20:17:23.099Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:8d931445-503e-4f1f-b35d-e4a19b4c319a> .\n";

        String[] lines = prov.split("\n");

        List<URI> requested = new ArrayList<>();


        DatasetConfigReaderProv datasetConfigReaderProv = new DatasetConfigReaderProv(new ResourceService() {
            @Override
            public InputStream retrieve(URI uri) throws IOException {
                requested.add(uri);
                return null;
            }
        });
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

        dataset = dataset == null ? datasetConfigReaderProv.datasetForContextOrReset() : dataset;

        assertThat(dataset, Is.is(notNullValue()));

        assertThat(dataset.getNamespace(), Is.is("local"));

        assertThat(dataset.getArchiveURI().toString(), Is.is("file:///home/runner/work/ecoab-host-plant/ecoab-host-plant/./"));

        JsonNode resources = dataset.getConfig().at("/resources");
        assertThat(resources.get("file:///home/runner/work/ecoab-host-plant/ecoab-host-plant/./globi.json").asText(), Is.is("hash://sha256/0ecb4e1f4a8d1b66b37fb379492d79813448edc1971236f62e0de104773b68db"));
        assertThat(resources.get("/globi.json").asText(), Is.is("hash://sha256/0ecb4e1f4a8d1b66b37fb379492d79813448edc1971236f62e0de104773b68db"));

        dataset.retrieve(URI.create("file:///home/runner/work/ecoab-host-plant/ecoab-host-plant/./dwca-ecoab-host-plant-v1.10.zip"));

        assertThat(requested.size(), Is.is(1));

        assertThat(requested.get(0).toString(), Is.is("hash://sha256/c96eaf6f0ebdfdcd9c8cec0968007f9149d476ca2d4423ba14c3e2c82318f8dc"));

        requested.clear();

    }

    @Test
    public void parseAssociation() throws IOException {

        DatasetConfigReaderProv datasetConfigReaderProv = new DatasetConfigReaderProv(new ResourceService() {
            @Override
            public InputStream retrieve(URI uri) throws IOException {
                return null;
            }
        });

        assertNull(datasetConfigReaderProv.readConfig("<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/template-dataset> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> ."));
        assertNull(datasetConfigReaderProv.readConfig("<https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> ."));
        assertNull(datasetConfigReaderProv.readConfig("<https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <http://purl.org/pav/hasVersion> <hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> ."));
        assertNotNull(datasetConfigReaderProv.readConfig("<urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:11:00.241Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> ."));
    }

    @Test
    public void compositeHashDetector() {
        assertTrue(DatasetConfigReaderProv.isCompositeHashIRI(RefNodeFactory.toIRI("zip:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/globi.json")));
    }

    @Test
    public void plainContentHash() {
        assertFalse(DatasetConfigReaderProv.isCompositeHashIRI(RefNodeFactory.toIRI("hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/globi.json")));
    }


    @Test
    public void plainLocation() {
        assertFalse(DatasetConfigReaderProv.isCompositeHashIRI(RefNodeFactory.toIRI("https://example.org")));
    }


}