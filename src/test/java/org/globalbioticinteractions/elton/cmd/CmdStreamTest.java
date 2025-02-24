package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.KeyTo3LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;

public class CmdStreamTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void streamNothing() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream("{}", StandardCharsets.UTF_8));
        cmdStream.run();

        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), Is.is(""));
//        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("processing data stream from [local]..."));
//        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), endsWith("processing data stream from [local]..."));
    }

    @Test
    public void streamSomeInteractions() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();


        populateCacheWithResource(tmpDir, "/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip");

        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setWorkDir(tmpDir.getAbsolutePath());

        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream("{ \"url\": \"hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44\", \"citation\": \"some citation\" }", StandardCharsets.UTF_8));
        cmdStream.run();

        assertHeaderAndMore(outputStream, headerInteractions());
    }

    @Test
    public void streamSomeInteractionsUsingLocalRepository() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();

        CmdStream cmdStream = new CmdStream();


        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream("{ \"url\": \"hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44\", \"citation\": \"some citation\" }", StandardCharsets.UTF_8));
        cmdStream.run();

        String stdout = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        assertThat(stdout, Is.is(""));

        File repoDir = folder.newFolder("repoDir");
        repoDir.mkdirs();

        populateCacheWithResource(repoDir, "/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip");

        cmdStream.setStdin(IOUtils.toInputStream("{ \"url\": \"hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44\", \"citation\": \"some citation\" }", StandardCharsets.UTF_8));
        cmdStream.setRemotes(Arrays.asList(repoDir.toURI()));
        cmdStream.run();

        assertHeaderAndMore(outputStream, headerInteractions());
    }


    @Test
    public void streamSomeProvStatements() throws IOException {

        String provLogGeneratedByElton = "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/template-dataset> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:41389744-0f4d-47e2-8506-76999e1b5c34> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<https://github.com/globalbioticinteractions/template-dataset/archive/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip> <http://purl.org/pav/hasVersion> <hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:cce97773-a8e2-4af4-94f9-0ac2699cb28e> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/globi.json> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/94bc19a3b0f172f63138fdc9384bb347f110e6fae6d42613a6eba019df6268d2> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:c7b1a849-8230-4e34-a0d5-7b663bc87e01> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/interactions.tsv> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha256/d84999936296e4b85086f2851f4459605502f4eb80b9484049b81d34f43b2ff1> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-06T18:00:34.689Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();


        populateCacheWithResource(tmpDir, "/b92cd44dcba945c760229a14d3b9becb2dd0c147.zip");

        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream(provLogGeneratedByElton, StandardCharsets.UTF_8));

        Collection<File> filesBefore = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesBefore = filesBefore.stream().filter(File::isFile).count();
        assertThat(numberOfFilesBefore, Is.is(1L));


        cmdStream.run();

        Collection<File> filesAfter = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesAfter = filesAfter.stream().filter(File::isFile).count();

        assertThat(numberOfFilesAfter, Is.is(1L));

        List<String> filenames = filesAfter.stream().map(File::getName).collect(Collectors.toList());

        assertThat(filenames, hasItems("76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44"));

        assertHeaderAndMore(outputStream, headerInteractions());
    }

    @Test
    public void streamSomeProvStatementsTemplateDataset() throws IOException {

        String provLogGeneratedByEltonTrack = "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<https://preston.guoda.bio> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<https://preston.guoda.bio> <http://purl.org/dc/terms/description> \"Preston is a software program that finds, archives and provides access to biodiversity datasets.\"@en <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> <http://purl.org/dc/terms/description> \"An event that (re-) processes existing biodiversity datasets graphs and their provenance.\"@en <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:43.815Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> <http://www.w3.org/ns/prov#wasStartedBy> <https://preston.guoda.bio> <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<https://doi.org/10.5281/zenodo.1410543> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Icaro Alzuru, & Michael Elliott. 2018-2024. Preston: a biodiversity dataset tracker (Version 0.10.3-SNAPSHOT) [Software]. Zenodo. https://doi.org/10.5281/zenodo.1410543\"@en <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:9984d0f1-a8e9-4f49-9f9e-bbcaa48b7dea> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://purl.org/dc/terms/description> \"Elton helps to access, review and index existing species interaction datasets.\"@en <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <http://purl.org/dc/terms/description> \"Update Local Datasets With Remote Sources\"@en <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:43.617Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <http://www.w3.org/ns/prov#wasStartedBy> <https://globalbioticinteractions.org/elton> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Tobias Kuhn & Katrin Leinweber. (2017/2024). globalbioticinteractions/elton: 0.14.0-SNAPSHOT. Zenodo. https://zenodo.org/doi/10.5281/zenodo.998263\"@en <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:43.765Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:43.766Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/e1e5998113ff9554578c246f8ffa02e7e4da5774a0f7ca2e351c07b4c5c6d60b> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/e1e5998113ff9554578c246f8ffa02e7e4da5774a0f7ca2e351c07b4c5c6d60b> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:46.439Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <http://purl.org/pav/hasVersion> <hash://sha256/e1e5998113ff9554578c246f8ffa02e7e4da5774a0f7ca2e351c07b4c5c6d60b> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:46.439Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/e1e5998113ff9554578c246f8ffa02e7e4da5774a0f7ca2e351c07b4c5c6d60b> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/e1e5998113ff9554578c246f8ffa02e7e4da5774a0f7ca2e351c07b4c5c6d60b> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:46.440Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&set=user-globalbioticinteractions&metadataPrefix=oai_datacite> <http://purl.org/pav/hasVersion> <hash://sha256/e1e5998113ff9554578c246f8ffa02e7e4da5774a0f7ca2e351c07b4c5c6d60b> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:50df77e4-84e4-40a4-ae97-35238a9f8d26> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:46.440Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:46.722Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:46.723Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/5f27c879006e5fbe69412fa97ed6936a36e0befde9127bd97f751258094994b3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/5f27c879006e5fbe69412fa97ed6936a36e0befde9127bd97f751258094994b3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:48.325Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjU2PgjAARP9Lz-wGKn5A4kFjKCHBaE1b6MUUWqVLBQMlshr_-2qy583OcTLvzQP0SkkQup-z-QIufBj43hRCdzZ3wFWcFQihA-qb6M49CB-vsQUhGHrVfZxNWwhT6NbqUjdWdaK0um164ICLskIKK3adOunxBbRCH99Fqa0CTwf0Zdcac9SvYxAhM-Rw9DiipxwGg0TUl1HgSWbqTRN5PEumFFWmqLmRaNuuRm5SaPZpnBwKg7uc4YYQnokMT3LmrVfv6CvrvnMm6oQxdBvVV3Ugm8RVFJ_oPbWYbSdy5JgTKQpXXmi0dfGd74oMc5FdRxXLX0-eDUnOCs_glCWY3CtMY0MIi5CKqx5fgvUe_9dT_u3ZL5fg-QOwgYXb.Z32mZQ.tXG0ExRt_5dAivPRP9Tap_hGUXQ> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjU2PgjAARP9Lz-wGKn5A4kFjKCHBaE1b6MUUWqVLBQMlshr_-2qy583OcTLvzQP0SkkQup-z-QIufBj43hRCdzZ3wFWcFQihA-qb6M49CB-vsQUhGHrVfZxNWwhT6NbqUjdWdaK0um164ICLskIKK3adOunxBbRCH99Fqa0CTwf0Zdcac9SvYxAhM-Rw9DiipxwGg0TUl1HgSWbqTRN5PEumFFWmqLmRaNuuRm5SaPZpnBwKg7uc4YYQnokMT3LmrVfv6CvrvnMm6oQxdBvVV3Ugm8RVFJ_oPbWYbSdy5JgTKQpXXmi0dfGd74oMc5FdRxXLX0-eDUnOCs_glCWY3CtMY0MIi5CKqx5fgvUe_9dT_u3ZL5fg-QOwgYXb.Z32mZQ.tXG0ExRt_5dAivPRP9Tap_hGUXQ> <http://purl.org/pav/hasVersion> <hash://sha256/5f27c879006e5fbe69412fa97ed6936a36e0befde9127bd97f751258094994b3> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:48.326Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/5f27c879006e5fbe69412fa97ed6936a36e0befde9127bd97f751258094994b3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/5f27c879006e5fbe69412fa97ed6936a36e0befde9127bd97f751258094994b3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:48.327Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjU2PgjAARP9Lz-wGKn5A4kFjKCHBaE1b6MUUWqVLBQMlshr_-2qy583OcTLvzQP0SkkQup-z-QIufBj43hRCdzZ3wFWcFQihA-qb6M49CB-vsQUhGHrVfZxNWwhT6NbqUjdWdaK0um164ICLskIKK3adOunxBbRCH99Fqa0CTwf0Zdcac9SvYxAhM-Rw9DiipxwGg0TUl1HgSWbqTRN5PEumFFWmqLmRaNuuRm5SaPZpnBwKg7uc4YYQnokMT3LmrVfv6CvrvnMm6oQxdBvVV3Ugm8RVFJ_oPbWYbSdy5JgTKQpXXmi0dfGd74oMc5FdRxXLX0-eDUnOCs_glCWY3CtMY0MIi5CKqx5fgvUe_9dT_u3ZL5fg-QOwgYXb.Z32mZQ.tXG0ExRt_5dAivPRP9Tap_hGUXQ> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjU2PgjAARP9Lz-wGKn5A4kFjKCHBaE1b6MUUWqVLBQMlshr_-2qy583OcTLvzQP0SkkQup-z-QIufBj43hRCdzZ3wFWcFQihA-qb6M49CB-vsQUhGHrVfZxNWwhT6NbqUjdWdaK0um164ICLskIKK3adOunxBbRCH99Fqa0CTwf0Zdcac9SvYxAhM-Rw9DiipxwGg0TUl1HgSWbqTRN5PEumFFWmqLmRaNuuRm5SaPZpnBwKg7uc4YYQnokMT3LmrVfv6CvrvnMm6oQxdBvVV3Ugm8RVFJ_oPbWYbSdy5JgTKQpXXmi0dfGd74oMc5FdRxXLX0-eDUnOCs_glCWY3CtMY0MIi5CKqx5fgvUe_9dT_u3ZL5fg-QOwgYXb.Z32mZQ.tXG0ExRt_5dAivPRP9Tap_hGUXQ> <http://purl.org/pav/hasVersion> <hash://sha256/5f27c879006e5fbe69412fa97ed6936a36e0befde9127bd97f751258094994b3> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:bdc95eae-2ea0-460c-949f-0200095d528a> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:48.328Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:48.581Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:48.582Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/632e85c8b0b61769657d5a70ed8be7efc3d31a74f064566a29fef9278ee6ce96> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/632e85c8b0b61769657d5a70ed8be7efc3d31a74f064566a29fef9278ee6ce96> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:50.542Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUFvgjAAhf9Lz2yhoJuQeNAYSkgwWlMKvZhCq3RUMKVEpvG_D5Odl73jy_u-9wC9lAKE7rsH53PfXQQwWPgfnv85c8CVnyUIfQc0N27OPQgf09qCEAy9NG9n3ZVcl6qzqlKtlYZXVnVtDxxwkZYLbvnOyJMaJ6Dj6vgqKmUleDqgr0yn9VFNzyBCeii8ETKUnQovGATKZiIKoKC62bQRZHkyz1Cty4ZpgbbdamQ69fQ-jZNDqbEpKG4JYTnPsV9QuF69oq7UfBeUNwml6DbKr_pANokrM3zK7qnFdOuLkWFGBC9dccmirYvvbFfmmPH8OspY_HqKfEgKWkKNU5pgcq9xFmtCaIRkXPf4Eqz3-L-e6m_PfrkEzx8Cc4YP.Z32maA.dwceLTkbJvq3V4JBa1YhKfc-eGk> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUFvgjAAhf9Lz2yhoJuQeNAYSkgwWlMKvZhCq3RUMKVEpvG_D5Odl73jy_u-9wC9lAKE7rsH53PfXQQwWPgfnv85c8CVnyUIfQc0N27OPQgf09qCEAy9NG9n3ZVcl6qzqlKtlYZXVnVtDxxwkZYLbvnOyJMaJ6Dj6vgqKmUleDqgr0yn9VFNzyBCeii8ETKUnQovGATKZiIKoKC62bQRZHkyz1Cty4ZpgbbdamQ69fQ-jZNDqbEpKG4JYTnPsV9QuF69oq7UfBeUNwml6DbKr_pANokrM3zK7qnFdOuLkWFGBC9dccmirYvvbFfmmPH8OspY_HqKfEgKWkKNU5pgcq9xFmtCaIRkXPf4Eqz3-L-e6m_PfrkEzx8Cc4YP.Z32maA.dwceLTkbJvq3V4JBa1YhKfc-eGk> <http://purl.org/pav/hasVersion> <hash://sha256/632e85c8b0b61769657d5a70ed8be7efc3d31a74f064566a29fef9278ee6ce96> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:50.543Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/632e85c8b0b61769657d5a70ed8be7efc3d31a74f064566a29fef9278ee6ce96> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/632e85c8b0b61769657d5a70ed8be7efc3d31a74f064566a29fef9278ee6ce96> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:50.545Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUFvgjAAhf9Lz2yhoJuQeNAYSkgwWlMKvZhCq3RUMKVEpvG_D5Odl73jy_u-9wC9lAKE7rsH53PfXQQwWPgfnv85c8CVnyUIfQc0N27OPQgf09qCEAy9NG9n3ZVcl6qzqlKtlYZXVnVtDxxwkZYLbvnOyJMaJ6Dj6vgqKmUleDqgr0yn9VFNzyBCeii8ETKUnQovGATKZiIKoKC62bQRZHkyz1Cty4ZpgbbdamQ69fQ-jZNDqbEpKG4JYTnPsV9QuF69oq7UfBeUNwml6DbKr_pANokrM3zK7qnFdOuLkWFGBC9dccmirYvvbFfmmPH8OspY_HqKfEgKWkKNU5pgcq9xFmtCaIRkXPf4Eqz3-L-e6m_PfrkEzx8Cc4YP.Z32maA.dwceLTkbJvq3V4JBa1YhKfc-eGk> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUFvgjAAhf9Lz2yhoJuQeNAYSkgwWlMKvZhCq3RUMKVEpvG_D5Odl73jy_u-9wC9lAKE7rsH53PfXQQwWPgfnv85c8CVnyUIfQc0N27OPQgf09qCEAy9NG9n3ZVcl6qzqlKtlYZXVnVtDxxwkZYLbvnOyJMaJ6Dj6vgqKmUleDqgr0yn9VFNzyBCeii8ETKUnQovGATKZiIKoKC62bQRZHkyz1Cty4ZpgbbdamQ69fQ-jZNDqbEpKG4JYTnPsV9QuF69oq7UfBeUNwml6DbKr_pANokrM3zK7qnFdOuLkWFGBC9dccmirYvvbFfmmPH8OspY_HqKfEgKWkKNU5pgcq9xFmtCaIRkXPf4Eqz3-L-e6m_PfrkEzx8Cc4YP.Z32maA.dwceLTkbJvq3V4JBa1YhKfc-eGk> <http://purl.org/pav/hasVersion> <hash://sha256/632e85c8b0b61769657d5a70ed8be7efc3d31a74f064566a29fef9278ee6ce96> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:9186fd1f-c8ae-4416-a52e-ebfc8e422d95> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:50.546Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:50.709Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:50.710Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/3018fd2829614f41d16c14d520d9524b9ecd92ad9d25791d4626556658d7f26c> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/3018fd2829614f41d16c14d520d9524b9ecd92ad9d25791d4626556658d7f26c> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:52.555Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjVFvgjAYRf_L98wWiuiUxAeNAUKC0Rpa2hdTaJWOCgZKZJr992my52X38eaecx_QKyUhcN9992PmT2bzKZrOked6DlzFWUHgO1DfRHfuIXg8xxYCGHrVvZ1NWwhT6NbqUjdWdaK0um16cOCirJDCil2nTnp8Aq3Qx1dRaqvg24G-7Fpjjvp5DGFkBuaNiEfkxLzFICPiy3CBJDX1pgkRz5MpiSpT1NzIaNuuRm5Sz-zTODkUBneM4ibLeC5yPGEUrVev6CvtvhgVdUJpdBvVZ3XINomrCD6Re2ox3U7kyDHPpChceSHh1sV3vityzEV-HVUsfz0sHxJGC2RwShOc3StMYpNlNIxUXPX4sljv8X895d-e_XIJ3z-i44XS.Z32mag.MwIhlfXEeygZU8rmPPA-NH-Xmvo> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjVFvgjAYRf_L98wWiuiUxAeNAUKC0Rpa2hdTaJWOCgZKZJr992my52X38eaecx_QKyUhcN9992PmT2bzKZrOked6DlzFWUHgO1DfRHfuIXg8xxYCGHrVvZ1NWwhT6NbqUjdWdaK0um16cOCirJDCil2nTnp8Aq3Qx1dRaqvg24G-7Fpjjvp5DGFkBuaNiEfkxLzFICPiy3CBJDX1pgkRz5MpiSpT1NzIaNuuRm5Sz-zTODkUBneM4ibLeC5yPGEUrVev6CvtvhgVdUJpdBvVZ3XINomrCD6Re2ox3U7kyDHPpChceSHh1sV3vityzEV-HVUsfz0sHxJGC2RwShOc3StMYpNlNIxUXPX4sljv8X895d-e_XIJ3z-i44XS.Z32mag.MwIhlfXEeygZU8rmPPA-NH-Xmvo> <http://purl.org/pav/hasVersion> <hash://sha256/3018fd2829614f41d16c14d520d9524b9ecd92ad9d25791d4626556658d7f26c> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:52.556Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/3018fd2829614f41d16c14d520d9524b9ecd92ad9d25791d4626556658d7f26c> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/3018fd2829614f41d16c14d520d9524b9ecd92ad9d25791d4626556658d7f26c> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:52.557Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjVFvgjAYRf_L98wWiuiUxAeNAUKC0Rpa2hdTaJWOCgZKZJr992my52X38eaecx_QKyUhcN9992PmT2bzKZrOked6DlzFWUHgO1DfRHfuIXg8xxYCGHrVvZ1NWwhT6NbqUjdWdaK0um16cOCirJDCil2nTnp8Aq3Qx1dRaqvg24G-7Fpjjvp5DGFkBuaNiEfkxLzFICPiy3CBJDX1pgkRz5MpiSpT1NzIaNuuRm5Sz-zTODkUBneM4ibLeC5yPGEUrVev6CvtvhgVdUJpdBvVZ3XINomrCD6Re2ox3U7kyDHPpChceSHh1sV3vityzEV-HVUsfz0sHxJGC2RwShOc3StMYpNlNIxUXPX4sljv8X895d-e_XIJ3z-i44XS.Z32mag.MwIhlfXEeygZU8rmPPA-NH-Xmvo> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjVFvgjAYRf_L98wWiuiUxAeNAUKC0Rpa2hdTaJWOCgZKZJr992my52X38eaecx_QKyUhcN9992PmT2bzKZrOked6DlzFWUHgO1DfRHfuIXg8xxYCGHrVvZ1NWwhT6NbqUjdWdaK0um16cOCirJDCil2nTnp8Aq3Qx1dRaqvg24G-7Fpjjvp5DGFkBuaNiEfkxLzFICPiy3CBJDX1pgkRz5MpiSpT1NzIaNuuRm5Sz-zTODkUBneM4ibLeC5yPGEUrVev6CvtvhgVdUJpdBvVZ3XINomrCD6Re2ox3U7kyDHPpChceSHh1sV3vityzEV-HVUsfz0sHxJGC2RwShOc3StMYpNlNIxUXPX4sljv8X895d-e_XIJ3z-i44XS.Z32mag.MwIhlfXEeygZU8rmPPA-NH-Xmvo> <http://purl.org/pav/hasVersion> <hash://sha256/3018fd2829614f41d16c14d520d9524b9ecd92ad9d25791d4626556658d7f26c> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:04381220-ab39-4d0e-8aa7-016a07cbdea2> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:52.557Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:52.645Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:52.646Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/527cf04523796ab950036ded906056410cb2035558e92f5db18b3b8743fee06c> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/527cf04523796ab950036ded906056410cb2035558e92f5db18b3b8743fee06c> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:54.519Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjc2OgjAYRd_lWzMTfo2SuNAYICQYrSml3ZhCq3SoYKBERuO7jyaznsxd3txz7gMGKQWE9qcz89154AX-bO7NHc-CKz9LCAMLmhvvzwOEj9fWQAjjIPuPs-5KrkvVGVWp1sieV0Z17QAWXKThghu-6-VJTS-g4-r4LiplJDwtGKq-0_qoXr8QxXqk7uSwOD9RdzGKOPdFtHAE0c2mjRxWpEEe17psmBbxtltNTGeu3mdJeig16ilBLcas4AXyKHHWq3fUlfTflPAmJSS-TfKrPuBNasscnfJ7ZhDZemJiiGHBS1tc8mhrozvblQVivLhOMhG_HlqMKSWlo1FGUoTvNcoTjTGJYpnUA7os1nv0X0_1t2e_XMLzB19Nhag.Z32maw.Bs98v0yRC164eOh8TjNIciqQFn4> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjc2OgjAYRd_lWzMTfo2SuNAYICQYrSml3ZhCq3SoYKBERuO7jyaznsxd3txz7gMGKQWE9qcz89154AX-bO7NHc-CKz9LCAMLmhvvzwOEj9fWQAjjIPuPs-5KrkvVGVWp1sieV0Z17QAWXKThghu-6-VJTS-g4-r4LiplJDwtGKq-0_qoXr8QxXqk7uSwOD9RdzGKOPdFtHAE0c2mjRxWpEEe17psmBbxtltNTGeu3mdJeig16ilBLcas4AXyKHHWq3fUlfTflPAmJSS-TfKrPuBNasscnfJ7ZhDZemJiiGHBS1tc8mhrozvblQVivLhOMhG_HlqMKSWlo1FGUoTvNcoTjTGJYpnUA7os1nv0X0_1t2e_XMLzB19Nhag.Z32maw.Bs98v0yRC164eOh8TjNIciqQFn4> <http://purl.org/pav/hasVersion> <hash://sha256/527cf04523796ab950036ded906056410cb2035558e92f5db18b3b8743fee06c> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:54.520Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/527cf04523796ab950036ded906056410cb2035558e92f5db18b3b8743fee06c> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/527cf04523796ab950036ded906056410cb2035558e92f5db18b3b8743fee06c> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:54.521Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjc2OgjAYRd_lWzMTfo2SuNAYICQYrSml3ZhCq3SoYKBERuO7jyaznsxd3txz7gMGKQWE9qcz89154AX-bO7NHc-CKz9LCAMLmhvvzwOEj9fWQAjjIPuPs-5KrkvVGVWp1sieV0Z17QAWXKThghu-6-VJTS-g4-r4LiplJDwtGKq-0_qoXr8QxXqk7uSwOD9RdzGKOPdFtHAE0c2mjRxWpEEe17psmBbxtltNTGeu3mdJeig16ilBLcas4AXyKHHWq3fUlfTflPAmJSS-TfKrPuBNasscnfJ7ZhDZemJiiGHBS1tc8mhrozvblQVivLhOMhG_HlqMKSWlo1FGUoTvNcoTjTGJYpnUA7os1nv0X0_1t2e_XMLzB19Nhag.Z32maw.Bs98v0yRC164eOh8TjNIciqQFn4> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjc2OgjAYRd_lWzMTfo2SuNAYICQYrSml3ZhCq3SoYKBERuO7jyaznsxd3txz7gMGKQWE9qcz89154AX-bO7NHc-CKz9LCAMLmhvvzwOEj9fWQAjjIPuPs-5KrkvVGVWp1sieV0Z17QAWXKThghu-6-VJTS-g4-r4LiplJDwtGKq-0_qoXr8QxXqk7uSwOD9RdzGKOPdFtHAE0c2mjRxWpEEe17psmBbxtltNTGeu3mdJeig16ilBLcas4AXyKHHWq3fUlfTflPAmJSS-TfKrPuBNasscnfJ7ZhDZemJiiGHBS1tc8mhrozvblQVivLhOMhG_HlqMKSWlo1FGUoTvNcoTjTGJYpnUA7os1nv0X0_1t2e_XMLzB19Nhag.Z32maw.Bs98v0yRC164eOh8TjNIciqQFn4> <http://purl.org/pav/hasVersion> <hash://sha256/527cf04523796ab950036ded906056410cb2035558e92f5db18b3b8743fee06c> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:78b31ffa-16e3-4733-b00d-c9d8f91511c8> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:54.522Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:54.604Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:54.605Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/444004dfa2f179dc31b7a2ccd6bada59b5d0284a5184edde2202466498c72fdf> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/444004dfa2f179dc31b7a2ccd6bada59b5d0284a5184edde2202466498c72fdf> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:56.660Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUGPgjAYRP_Ld2Y3FHUjJB40BggJRmtKoRfT0ipdKhgokdX431cTz5ud42Temzv0SkkI3M-ph9w5Qt4EebO5O_cduPCTguDLgfrKu1MPwf05thDA0Kvu42RawY3QrdWlbqzqeGl12_TgwFlZLrnl204d9fgEWq4Pr6LUVsHDgb7sWmMO-nkMYWSGwhsRi7Jj4fmDjLKpDH0kqanXTYhYnsyyqDKiZkZGm3Y5MpN6ZpfGyV4Y3BUUN4SwnOd4UlC0Wr6iL7T7KSivE0qj66i-qz1ZJ67K8DG7pRbTzUSODDMiuXDlOQs3Lr6xrcgx4_llVLF8e4p8SAoqkMEpTTC5VTiLDSE0jFRc9fjsr3b4v57yb89usYDHL5qvhc0.Z32mbg.cQtJvsgtjPje_fW9obiByNWMglA> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUGPgjAYRP_Ld2Y3FHUjJB40BggJRmtKoRfT0ipdKhgokdX431cTz5ud42Temzv0SkkI3M-ph9w5Qt4EebO5O_cduPCTguDLgfrKu1MPwf05thDA0Kvu42RawY3QrdWlbqzqeGl12_TgwFlZLrnl204d9fgEWq4Pr6LUVsHDgb7sWmMO-nkMYWSGwhsRi7Jj4fmDjLKpDH0kqanXTYhYnsyyqDKiZkZGm3Y5MpN6ZpfGyV4Y3BUUN4SwnOd4UlC0Wr6iL7T7KSivE0qj66i-qz1ZJ67K8DG7pRbTzUSODDMiuXDlOQs3Lr6xrcgx4_llVLF8e4p8SAoqkMEpTTC5VTiLDSE0jFRc9fjsr3b4v57yb89usYDHL5qvhc0.Z32mbg.cQtJvsgtjPje_fW9obiByNWMglA> <http://purl.org/pav/hasVersion> <hash://sha256/444004dfa2f179dc31b7a2ccd6bada59b5d0284a5184edde2202466498c72fdf> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:56.662Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/444004dfa2f179dc31b7a2ccd6bada59b5d0284a5184edde2202466498c72fdf> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/444004dfa2f179dc31b7a2ccd6bada59b5d0284a5184edde2202466498c72fdf> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:56.662Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/ns/prov#used> <https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUGPgjAYRP_Ld2Y3FHUjJB40BggJRmtKoRfT0ipdKhgokdX431cTz5ud42Temzv0SkkI3M-ph9w5Qt4EebO5O_cduPCTguDLgfrKu1MPwf05thDA0Kvu42RawY3QrdWlbqzqeGl12_TgwFlZLrnl204d9fgEWq4Pr6LUVsHDgb7sWmMO-nkMYWSGwhsRi7Jj4fmDjLKpDH0kqanXTYhYnsyyqDKiZkZGm3Y5MpN6ZpfGyV4Y3BUUN4SwnOd4UlC0Wr6iL7T7KSivE0qj66i-qz1ZJ67K8DG7pRbTzUSODDMiuXDlOQs3Lr6xrcgx4_llVLF8e4p8SAoqkMEpTTC5VTiLDSE0jFRc9fjsr3b4v57yb89usYDHL5qvhc0.Z32mbg.cQtJvsgtjPje_fW9obiByNWMglA> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/oai2d?verb=ListRecords&resumptionToken=.eJyNjUGPgjAYRP_Ld2Y3FHUjJB40BggJRmtKoRfT0ipdKhgokdX431cTz5ud42Temzv0SkkI3M-ph9w5Qt4EebO5O_cduPCTguDLgfrKu1MPwf05thDA0Kvu42RawY3QrdWlbqzqeGl12_TgwFlZLrnl204d9fgEWq4Pr6LUVsHDgb7sWmMO-nkMYWSGwhsRi7Jj4fmDjLKpDH0kqanXTYhYnsyyqDKiZkZGm3Y5MpN6ZpfGyV4Y3BUUN4SwnOd4UlC0Wr6iL7T7KSivE0qj66i-qz1ZJ67K8DG7pRbTzUSODDMiuXDlOQs3Lr6xrcgx4_llVLF8e4p8SAoqkMEpTTC5VTiLDSE0jFRc9fjsr3b4v57yb89usYDHL5qvhc0.Z32mbg.cQtJvsgtjPje_fW9obiByNWMglA> <http://purl.org/pav/hasVersion> <hash://sha256/444004dfa2f179dc31b7a2ccd6bada59b5d0284a5184edde2202466498c72fdf> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:7e4afd79-b6ea-4812-8a47-c1b017779348> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:56.664Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:59.307Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:59.308Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/6ff154a9e7478a86a8c1307b4468465fea23bf4974255189b25e9c299cecb908> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/6ff154a9e7478a86a8c1307b4468465fea23bf4974255189b25e9c299cecb908> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:59.528Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/template-dataset/info/refs?service=git-upload-pack> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://github.com/globalbioticinteractions/template-dataset/info/refs?service=git-upload-pack> <http://purl.org/pav/hasVersion> <hash://sha256/6ff154a9e7478a86a8c1307b4468465fea23bf4974255189b25e9c299cecb908> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:59.529Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/6ff154a9e7478a86a8c1307b4468465fea23bf4974255189b25e9c299cecb908> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/6ff154a9e7478a86a8c1307b4468465fea23bf4974255189b25e9c299cecb908> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:59.529Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/template-dataset/info/refs?service=git-upload-pack> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://github.com/globalbioticinteractions/template-dataset/info/refs?service=git-upload-pack> <http://purl.org/pav/hasVersion> <hash://sha256/6ff154a9e7478a86a8c1307b4468465fea23bf4974255189b25e9c299cecb908> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:2e533870-1a7b-4ece-a16a-6a6a197e570d> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:59.529Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/template-dataset> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:4cfe6aad-8582-4c69-8ab6-e5ae81e053ce> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:4cfe6aad-8582-4c69-8ab6-e5ae81e053ce> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:4cfe6aad-8582-4c69-8ab6-e5ae81e053ce> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:59.565Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:4cfe6aad-8582-4c69-8ab6-e5ae81e053ce> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:4cfe6aad-8582-4c69-8ab6-e5ae81e053ce> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:4cfe6aad-8582-4c69-8ab6-e5ae81e053ce> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:59.787Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:4cfe6aad-8582-4c69-8ab6-e5ae81e053ce> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:4cfe6aad-8582-4c69-8ab6-e5ae81e053ce> <http://www.w3.org/ns/prov#used> <https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <http://purl.org/pav/hasVersion> <hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:4cfe6aad-8582-4c69-8ab6-e5ae81e053ce> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:59.788Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:a7605e36-98a9-4396-97a9-b34993ddce53> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:a7605e36-98a9-4396-97a9-b34993ddce53> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:a7605e36-98a9-4396-97a9-b34993ddce53> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:59.819Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:fdea0e41-993e-4865-95ca-ad1923566aa0> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:fdea0e41-993e-4865-95ca-ad1923566aa0> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:fdea0e41-993e-4865-95ca-ad1923566aa0> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:59.829Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:fd80fedc-8dfb-482d-9fce-34654731c9f5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:fd80fedc-8dfb-482d-9fce-34654731c9f5> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:fd80fedc-8dfb-482d-9fce-34654731c9f5> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:10:59.836Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:fd80fedc-8dfb-482d-9fce-34654731c9f5> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:fd80fedc-8dfb-482d-9fce-34654731c9f5> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:fd80fedc-8dfb-482d-9fce-34654731c9f5> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:10:59.844Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:fd80fedc-8dfb-482d-9fce-34654731c9f5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:fd80fedc-8dfb-482d-9fce-34654731c9f5> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060!/globalbioticinteractions-template-dataset-0851959/globi.json> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<jar:hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060!/globalbioticinteractions-template-dataset-0851959/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:fd80fedc-8dfb-482d-9fce-34654731c9f5> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:10:59.845Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:6ec1ef6a-f48e-466e-8556-ff9e5645c22d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:6ec1ef6a-f48e-466e-8556-ff9e5645c22d> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:6ec1ef6a-f48e-466e-8556-ff9e5645c22d> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:11:00.121Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/1450b1fb1bd0bef14669804377b5824f81c7717a4e416db1da09449ef7d02cf9> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:6ec1ef6a-f48e-466e-8556-ff9e5645c22d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<hash://sha256/1450b1fb1bd0bef14669804377b5824f81c7717a4e416db1da09449ef7d02cf9> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:6ec1ef6a-f48e-466e-8556-ff9e5645c22d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:6ec1ef6a-f48e-466e-8556-ff9e5645c22d> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-01-07T22:11:00.123Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:6ec1ef6a-f48e-466e-8556-ff9e5645c22d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:6ec1ef6a-f48e-466e-8556-ff9e5645c22d> <http://www.w3.org/ns/prov#used> <jar:hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060!/globalbioticinteractions-template-dataset-0851959/interactions.tsv> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<jar:hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060!/globalbioticinteractions-template-dataset-0851959/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha256/1450b1fb1bd0bef14669804377b5824f81c7717a4e416db1da09449ef7d02cf9> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:6ec1ef6a-f48e-466e-8556-ff9e5645c22d> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:11:00.123Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:0aa00fc6-34a6-4e45-9e40-94f9876d493b> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:0aa00fc6-34a6-4e45-9e40-94f9876d493b> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:0aa00fc6-34a6-4e45-9e40-94f9876d493b> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:11:00.152Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:b742a94b-58f6-4760-b978-193fbb4a6dc3> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:b742a94b-58f6-4760-b978-193fbb4a6dc3> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:b742a94b-58f6-4760-b978-193fbb4a6dc3> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:11:00.155Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:5d311c87-3071-436e-8739-531d05a19416> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:5d311c87-3071-436e-8739-531d05a19416> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:5d311c87-3071-436e-8739-531d05a19416> <http://www.w3.org/ns/prov#startedAtTime> \"2025-01-07T22:11:00.239Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n" +
                "<urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> <http://www.w3.org/ns/prov#endedAtTime> \"2025-01-07T22:11:00.241Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:b382e573-2df0-45ee-acb3-731fe226207d> .\n";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();


        populateCacheWithResource(tmpDir, "/template-dataset-0.0.3.zip");

        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream(provLogGeneratedByEltonTrack, StandardCharsets.UTF_8));

        Collection<File> filesBefore = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesBefore = filesBefore.stream().filter(File::isFile).count();
        assertThat(numberOfFilesBefore, Is.is(1L));


        cmdStream.run();

        Collection<File> filesAfter = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesAfter = filesAfter.stream().filter(File::isFile).count();

        assertThat(numberOfFilesAfter, Is.is(1L));

        List<String> filenames = filesAfter.stream().map(File::getName).collect(Collectors.toList());

        assertThat(filenames, hasItems("5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060"));

        assertHeaderAndMore(outputStream, headerInteractions());
    }

    @Test
    public void streamProvStatementsGeneratedByEltonProv() throws IOException {

        String provLogGeneratedByEltonTrack = "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://purl.org/dc/terms/description> \"Elton helps to access, review and index existing species interaction datasets.\"@en <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <http://purl.org/dc/terms/description> \"Tracking the origins of species interaction dataset\"@en <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-24T19:34:09.636Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <http://www.w3.org/ns/prov#wasStartedBy> <https://globalbioticinteractions.org/elton> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Tobias Kuhn & Katrin Leinweber. (2017/2024). globalbioticinteractions/elton: 0.14.4-SNAPSHOT. Zenodo. https://zenodo.org/doi/10.5281/zenodo.998263\"@en <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> .\n" +
                "<hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> .\n" +
                "<hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> .\n" +
                "<urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T19:34:09.812Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> .\n" +
                "<urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> .\n" +
                "<urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> .\n" +
                "<urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> <http://www.w3.org/ns/prov#used> <https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> .\n" +
                "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/template-dataset> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> .\n" +
                "<https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> .\n" +
                "<https://zenodo.org/records/1436853/files/globalbioticinteractions/template-dataset-0.0.3.zip> <http://purl.org/pav/hasVersion> <hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060> <urn:uuid:cd8f671f-fa08-483b-a531-125641ad9c48> .\n" +
                "<hash://sha256/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> <urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> .\n" +
                "<hash://sha256/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> <urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> .\n" +
                "<urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T19:34:09.818Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> .\n" +
                "<urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> .\n" +
                "<urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> .\n" +
                "<urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> <http://www.w3.org/ns/prov#used> <zip:hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060!/globalbioticinteractions-template-dataset-0851959/globi.json> <urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> .\n" +
                "<zip:hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060!/globalbioticinteractions-template-dataset-0851959/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/1cc8eff62af0e6bb3e7771666e2e4109f351b7dfc6fc1dc8314e5671a8eecb80> <urn:uuid:4573c311-66a4-42e9-8d16-0c0ba596570c> .\n" +
                "<hash://sha256/f49f665c540214e7d00466e359821de1bc03206f8373d4974220d608ed7b98f3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> <urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> .\n" +
                "<hash://sha256/f49f665c540214e7d00466e359821de1bc03206f8373d4974220d608ed7b98f3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> <urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> .\n" +
                "<urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T19:34:09.896Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> .\n" +
                "<urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> .\n" +
                "<urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> .\n" +
                "<urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_ignored.csv> <urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_ignored.csv> <http://purl.org/pav/hasVersion> <hash://sha256/f49f665c540214e7d00466e359821de1bc03206f8373d4974220d608ed7b98f3> <urn:uuid:2aa07e09-7bda-40d9-8a13-7255bf4ee1df> .\n" +
                "<hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> <urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> .\n" +
                "<hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> <urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> .\n" +
                "<urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T19:34:09.920Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> .\n" +
                "<urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> .\n" +
                "<urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> .\n" +
                "<urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_mapping.csv> <urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_mapping.csv> <http://purl.org/pav/hasVersion> <hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7> <urn:uuid:577a32ea-364c-448a-8215-3982bfa42853> .\n" +
                "<hash://sha256/43aba7b90c686a4890aebd4a90a02d6f82259664524bdad1b22102a29fe9fa07> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> <urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> .\n" +
                "<hash://sha256/43aba7b90c686a4890aebd4a90a02d6f82259664524bdad1b22102a29fe9fa07> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> <urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> .\n" +
                "<urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T19:34:09.923Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> .\n" +
                "<urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> .\n" +
                "<urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> .\n" +
                "<urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_ro_unmapped.csv> <urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_ro_unmapped.csv> <http://purl.org/pav/hasVersion> <hash://sha256/43aba7b90c686a4890aebd4a90a02d6f82259664524bdad1b22102a29fe9fa07> <urn:uuid:607cf322-efde-451b-b1dc-be74c106b902> .\n" +
                "<hash://sha256/7dc2797003a95ac7b97be06fda48b4dc25e7a555a2839a19ad7dc7a148427e43> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> <urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> .\n" +
                "<hash://sha256/7dc2797003a95ac7b97be06fda48b4dc25e7a555a2839a19ad7dc7a148427e43> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> <urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> .\n" +
                "<urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T19:34:09.928Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> .\n" +
                "<urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> .\n" +
                "<urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> .\n" +
                "<urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_ro.csv> <urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_ro.csv> <http://purl.org/pav/hasVersion> <hash://sha256/7dc2797003a95ac7b97be06fda48b4dc25e7a555a2839a19ad7dc7a148427e43> <urn:uuid:d5d803f3-1984-4b54-94e8-dbc9e82c31c7> .\n" +
                "<hash://sha256/1450b1fb1bd0bef14669804377b5824f81c7717a4e416db1da09449ef7d02cf9> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> <urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> .\n" +
                "<hash://sha256/1450b1fb1bd0bef14669804377b5824f81c7717a4e416db1da09449ef7d02cf9> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> <urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> .\n" +
                "<urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T19:34:09.969Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> .\n" +
                "<urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> .\n" +
                "<urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:60073b73-643c-41f6-8aa3-4f49d139e770> <urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> .\n" +
                "<urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> <http://www.w3.org/ns/prov#used> <zip:hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060!/globalbioticinteractions-template-dataset-0851959/interactions.tsv> <urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> .\n" +
                "<zip:hash://sha256/5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060!/globalbioticinteractions-template-dataset-0851959/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha256/1450b1fb1bd0bef14669804377b5824f81c7717a4e416db1da09449ef7d02cf9> <urn:uuid:86973557-442f-495a-81c3-6e461fb1eda5> .\n";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();


        populateCacheWithResource(tmpDir, "/template-dataset-0.0.3.zip");

        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream(provLogGeneratedByEltonTrack, StandardCharsets.UTF_8));

        Collection<File> filesBefore = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesBefore = filesBefore.stream().filter(File::isFile).count();
        assertThat(numberOfFilesBefore, Is.is(1L));


        cmdStream.run();

        Collection<File> filesAfter = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesAfter = filesAfter.stream().filter(File::isFile).count();

        assertThat(numberOfFilesAfter, Is.is(1L));

        List<String> filenames = filesAfter.stream().map(File::getName).collect(Collectors.toList());

        assertThat(filenames, hasItems("5b4ee64e7384bdf3d75b1d6617edd5d82124567b4ec52b47920ea332837ff060"));

        assertHeaderAndMore(outputStream, headerInteractions());
    }

    @Test
    public void streamProvStatementsGeneratedByEltonProvForUCSBIZC() throws IOException {

        String provLogGeneratedByEltonTrack = "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Agent> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<https://globalbioticinteractions.org/elton> <http://purl.org/dc/terms/description> \"Elton helps to access, review and index existing species interaction datasets.\"@en <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Activity> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <http://purl.org/dc/terms/description> \"Tracking the origins of species interaction dataset\"@en <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <http://www.w3.org/ns/prov#startedAtTime> \"2025-02-24T20:22:51.096Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <http://www.w3.org/ns/prov#wasStartedBy> <https://globalbioticinteractions.org/elton> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/ns/prov#usedBy> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://purl.org/dc/dcmitype/Software> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<https://zenodo.org/doi/10.5281/zenodo.998263> <http://purl.org/dc/terms/bibliographicCitation> \"Jorrit Poelen, Tobias Kuhn & Katrin Leinweber. (2017/2024). globalbioticinteractions/elton: 0.14.4-SNAPSHOT. Zenodo. https://zenodo.org/doi/10.5281/zenodo.998263\"@en <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Entity> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<urn:uuid:0659a54f-b713-4f86-a917-5be166a14110> <http://purl.org/dc/terms/description> \"A biodiversity dataset graph archive.\"@en <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> .\n" +
                "<hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> .\n" +
                "<hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> .\n" +
                "<urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T20:22:51.267Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> .\n" +
                "<urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> .\n" +
                "<urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> .\n" +
                "<urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> <http://www.w3.org/ns/prov#used> <https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> .\n" +
                "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/ucsb-izc> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> .\n" +
                "<https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <http://purl.org/pav/hasVersion> <hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43> <urn:uuid:037d27ec-ae9e-440e-8c79-ffa851763e52> .\n" +
                "<hash://sha256/14289a70968588a29f8e566053c4509e0784bded38b6ab7172569ac7ceb7cae7> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> <urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> .\n" +
                "<hash://sha256/14289a70968588a29f8e566053c4509e0784bded38b6ab7172569ac7ceb7cae7> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> <urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> .\n" +
                "<urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T20:22:51.271Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> .\n" +
                "<urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> .\n" +
                "<urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> .\n" +
                "<urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> <http://www.w3.org/ns/prov#used> <zip:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/globi.json> <urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> .\n" +
                "<zip:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/globi.json> <http://purl.org/pav/hasVersion> <hash://sha256/1a9f4d35f2511e03acf12d4c841a73b9d37964a2ecdde940130338bb07954eaa> <urn:uuid:69a8b326-ff2c-4ed3-a2d5-1d8be1a48fb4> .\n" +
                "<hash://sha256/14289a70968588a29f8e566053c4509e0784bded38b6ab7172569ac7ceb7cae7> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> <urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> .\n" +
                "<hash://sha256/14289a70968588a29f8e566053c4509e0784bded38b6ab7172569ac7ceb7cae7> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> <urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> .\n" +
                "<urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T20:22:51.419Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> .\n" +
                "<urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> .\n" +
                "<urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> .\n" +
                "<urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> <http://www.w3.org/ns/prov#used> <https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> .\n" +
                "<https://ecdysis.org/content/dwca/UCSB-IZC_DwC-A.zip> <http://purl.org/pav/hasVersion> <hash://sha256/14289a70968588a29f8e566053c4509e0784bded38b6ab7172569ac7ceb7cae7> <urn:uuid:87e4259b-8a2b-4cd0-a944-e940f740c59d> .\n" +
                "<hash://sha256/7fafdc19aa1899121e85f68fe05e4ca917157045e683600f203e921c7b99a426> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> <urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> .\n" +
                "<hash://sha256/7fafdc19aa1899121e85f68fe05e4ca917157045e683600f203e921c7b99a426> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> <urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> .\n" +
                "<urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T20:22:51.848Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> .\n" +
                "<urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> .\n" +
                "<urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> .\n" +
                "<urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> <http://www.w3.org/ns/prov#used> <zip:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/interaction_types_mapping.csv> <urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> .\n" +
                "<zip:hash://sha256/1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43!/ucsb-izc-65ef047765bf2eb5ef8108371b429489bf9c2a27/interaction_types_mapping.csv> <http://purl.org/pav/hasVersion> <hash://sha256/7fafdc19aa1899121e85f68fe05e4ca917157045e683600f203e921c7b99a426> <urn:uuid:c5612b40-04e5-4656-95ee-a05510bd1a58> .\n" +
                "<hash://sha256/f49f665c540214e7d00466e359821de1bc03206f8373d4974220d608ed7b98f3> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> <urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> .\n" +
                "<hash://sha256/f49f665c540214e7d00466e359821de1bc03206f8373d4974220d608ed7b98f3> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> <urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> .\n" +
                "<urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T20:22:51.858Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> .\n" +
                "<urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> .\n" +
                "<urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> .\n" +
                "<urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_ignored.csv> <urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_ignored.csv> <http://purl.org/pav/hasVersion> <hash://sha256/f49f665c540214e7d00466e359821de1bc03206f8373d4974220d608ed7b98f3> <urn:uuid:dad92001-542f-43b9-9c26-10a8ee9a4f85> .\n" +
                "<hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> <urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> .\n" +
                "<hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> <urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> .\n" +
                "<urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T20:22:51.869Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> .\n" +
                "<urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> .\n" +
                "<urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> .\n" +
                "<urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_mapping.csv> <urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_mapping.csv> <http://purl.org/pav/hasVersion> <hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7> <urn:uuid:c3e1b845-f989-448b-bd97-4770ebd5987a> .\n" +
                "<hash://sha256/43aba7b90c686a4890aebd4a90a02d6f82259664524bdad1b22102a29fe9fa07> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> <urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> .\n" +
                "<hash://sha256/43aba7b90c686a4890aebd4a90a02d6f82259664524bdad1b22102a29fe9fa07> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> <urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> .\n" +
                "<urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T20:22:51.872Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> .\n" +
                "<urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> .\n" +
                "<urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> .\n" +
                "<urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_ro_unmapped.csv> <urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_ro_unmapped.csv> <http://purl.org/pav/hasVersion> <hash://sha256/43aba7b90c686a4890aebd4a90a02d6f82259664524bdad1b22102a29fe9fa07> <urn:uuid:0731b190-f6f9-4a36-8729-5ef929dce2ad> .\n" +
                "<hash://sha256/7dc2797003a95ac7b97be06fda48b4dc25e7a555a2839a19ad7dc7a148427e43> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> <urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> .\n" +
                "<hash://sha256/7dc2797003a95ac7b97be06fda48b4dc25e7a555a2839a19ad7dc7a148427e43> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> <urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> .\n" +
                "<urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> <http://www.w3.org/ns/prov#generatedAtTime> \"2025-02-24T20:22:51.877Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> .\n" +
                "<urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> .\n" +
                "<urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:6f3350e3-7f3a-4dd9-b3c3-1c160790f428> <urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> .\n" +
                "<urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> <http://www.w3.org/ns/prov#used> <classpath:/org/globalbioticinteractions/interaction_types_ro.csv> <urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_ro.csv> <http://purl.org/pav/hasVersion> <hash://sha256/7dc2797003a95ac7b97be06fda48b4dc25e7a555a2839a19ad7dc7a148427e43> <urn:uuid:d8e45a4e-4665-4335-89c5-06db987aed83> .\n";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();


        populateCacheWithResource(tmpDir, "/ucsb-izc-fat-dwca.zip");
        populateCacheWithResource(tmpDir, "/ucsb-izc-in-globi-config.zip");

        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream(provLogGeneratedByEltonTrack, StandardCharsets.UTF_8));

        Collection<File> filesBefore = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesBefore = filesBefore.stream().filter(File::isFile).count();
        assertThat(numberOfFilesBefore, Is.is(2L));


        cmdStream.run();

        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), Is.is(""));

        Collection<File> filesAfter = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesAfter = filesAfter.stream().filter(File::isFile).count();

        assertThat(numberOfFilesAfter, Is.is(2L));

        List<String> filenames = filesAfter.stream().map(File::getName).collect(Collectors.toList());

        String patchedDwcaContentId = "14289a70968588a29f8e566053c4509e0784bded38b6ab7172569ac7ceb7cae7";
        String globiContentContentId = "1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43";
        assertThat(filenames, hasItems(patchedDwcaContentId));
        assertThat(filenames, hasItems(globiContentContentId));

        assertHeaderAndMore(outputStream, headerInteractions());
    }

    static void assertHeaderAndMore(ByteArrayOutputStream outputStream, String prefix) {
        String stdout = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        assertThat(stdout, startsWith(prefix));
        assertThat(stdout.split("\n").length, Is.is(greaterThan(1)));
    }

    private void populateCacheWithResource(File tmpDir, String resource) throws IOException {
        BlobStoreAppendOnly blobStore = getBlobStore(tmpDir);
        blobStore.put(getClass().getResourceAsStream(resource));
    }

    private BlobStoreAppendOnly getBlobStore(File tmpDir) {
        KeyTo3LevelPath keyToPath = new KeyTo3LevelPath(tmpDir.toURI());
        return new BlobStoreAppendOnly(
                new KeyValueStoreLocalFileSystem(
                        tmpDir,
                        keyToPath,
                        new ValidatingKeyValueStreamContentAddressedFactory()
                ),
                true,
                HashType.sha256
        );
    }

    @Test
    public void streamSomeProvStatementsDwCAWithoutGloBIJSON() throws IOException, URISyntaxException {

        URL resource = getClass().getResource("/ucsb-izc-slim-dwca.zip");
        assertNotNull(resource);

        IRI iri = RefNodeFactory.toIRI(resource.toURI());

        String provLogGeneratedByElton = "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/ucsb-izc> <http://www.w3.org/ns/prov#wasAssociatedWith> " + iri + " <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                iri + " <http://purl.org/dc/elements/1.1/format> \"application/globi\" <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                iri + " <http://purl.org/pav/hasVersion> <hash://sha256/aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_mapping.csv> <http://purl.org/pav/hasVersion> <hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<classpath:/org/globalbioticinteractions/interaction_types_ro.csv> <http://purl.org/pav/hasVersion> <hash://sha256/7dc2797003a95ac7b97be06fda48b4dc25e7a555a2839a19ad7dc7a148427e43> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                "<urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> <http://www.w3.org/ns/prov#endedAtTime> \"2025-02-04T00:18:02.671Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();
        CmdStream cmdStream = new CmdStream();

        populateCache(tmpDir);


        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream(provLogGeneratedByElton, StandardCharsets.UTF_8));

        Collection<File> filesBefore = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesBefore = filesBefore.stream().filter(File::isFile).count();
        assertThat(numberOfFilesBefore, Is.is(3L));


        cmdStream.run();

        Collection<File> filesAfter = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesAfter = filesAfter.stream().filter(File::isFile).count();

        assertThat(numberOfFilesAfter, Is.is(3L));

        List<String> filenames = filesAfter.stream().map(File::getName).collect(Collectors.toList());

        assertThat(filenames, hasItems("aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d"));
        assertThat(filenames, hasItems("ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7"));

        assertHeaderAndMore(outputStream, headerInteractions());
    }

    @Test
    public void streamSomeProvStatementsEmbeddedDwCA() throws IOException, URISyntaxException {

        URL resource = getClass().getResource("/ucsb-izc-in-globi-config.zip");
        assertNotNull(resource);

        String dwcArchive = "/ucsb-izc-slim-dwca.zip";
        IRI dwcaReferenceReplacement = Hasher.calcHashIRI(getClass().getResourceAsStream(dwcArchive), NullOutputStream.INSTANCE, HashType.sha256);

        String dwcaReference = "<hash://sha256/14289a70968588a29f8e566053c4509e0784bded38b6ab7172569ac7ceb7cae7>";

        String provLogGeneratedByElton = IOUtils.toString(getClass().getResourceAsStream("/ucsb-izc.nq"), StandardCharsets.UTF_8);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();
        CmdStream cmdStream = new CmdStream();


        populateCacheWithResource(tmpDir, "/ucsb-izc-in-globi-config.zip");
        populateCacheWithResource(tmpDir, dwcArchive);
        populateCacheWithResource(tmpDir, "/ucsb-izc-interaction_type_mapping_default.csv");


        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream(StringUtils.replace(provLogGeneratedByElton, dwcaReference, dwcaReferenceReplacement.toString()), StandardCharsets.UTF_8));

        Collection<File> filesBefore = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesBefore = filesBefore.stream().filter(File::isFile).count();
        assertThat(numberOfFilesBefore, Is.is(3L));


        cmdStream.run();

        Collection<File> filesAfter = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesAfter = filesAfter.stream().filter(File::isFile).count();

        assertThat(numberOfFilesAfter, Is.is(3L));

        List<String> filenames = filesAfter.stream().map(File::getName).collect(Collectors.toList());

        assertThat(filenames, hasItems("aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d"));
        assertThat(filenames, hasItems("aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d"));

        assertHeaderAndMore(outputStream, headerInteractions());
    }

    private void populateCache(File tmpDir) throws IOException {
        populateCacheWithResource(tmpDir, "/ucsb-izc-slim-dwca.zip");
        populateCacheWithResource(tmpDir, "/ucsb-izc-interaction_type_mapping_default.csv");
        populateCacheWithResource(tmpDir, "/ucsb-izc-interaction_type_mapping_ro.csv");
    }


    @Test
    public void streamSomeInteractionsCustomNamespace() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();


        populateCache(tmpDir);

        cmdStream.setRecordType("interaction");
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("namespace", "name/space");
        objectNode.put("format", "dwca");
        objectNode.put("url", "hash://sha256/aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d");
        objectNode.put("citation", "some citation");
        ObjectNode resources = new ObjectMapper().createObjectNode();
        resources.put("classpath:/org/globalbioticinteractions/interaction_types_mapping.csv", "hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7");
        objectNode.set("resources", resources);


        cmdStream.setStdin(IOUtils.toInputStream(objectNode.toString(), StandardCharsets.UTF_8));
        cmdStream.run();

        assertHeaderAndMore(outputStream, headerInteractions());
    }

    @Test
    public void streamSomeInteractionsCustomNamespaceGlobalOverrideInteractionTypeMapping() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();


        populateCache(tmpDir);
        populateCacheWithResource(tmpDir, "/global-globi-config.json");
        populateCacheWithResource(tmpDir, "/global-interaction_type_mapping_default.csv");


        cmdStream.setRecordType("interaction");
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setConfigOverrideReesource(URI.create("hash://sha256/b1a25958aa62f50ffb231fed929d053a4fbd99a9d854ffc9284b338501716685"));

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("namespace", "name/space");
        objectNode.put("format", "dwca");
        objectNode.put("url", "hash://sha256/aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d");
        objectNode.put("citation", "some citation");
        ObjectNode resources = new ObjectMapper().createObjectNode();
        resources.put("classpath:/org/globalbioticinteractions/interaction_types_mapping.csv", "hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7");
        objectNode.set("resources", resources);


        cmdStream.setStdin(IOUtils.toInputStream(objectNode.toString(), StandardCharsets.UTF_8));
        cmdStream.run();

        String stdout = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        assertThat(stdout, startsWith(headerInteractions()));
        String[] lines = stdout.split("\n");
        assertThat(lines.length, Is.is(greaterThan(1)));
        assertThat(lines[1], containsString("http://purl.obolibrary.org/obo/RO_0002321\tecologicallyRelatedTo"));
    }

    @Test
    public void streamSomeNames() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        runForRecordType(outputStream, errorStream, "name");

        assertHeaderAndMore(outputStream, headerNames());

        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), startsWith(headerNames()));
    }

    private void runForRecordType(ByteArrayOutputStream outputStream, ByteArrayOutputStream errorStream, String recordType) throws IOException {
        CmdStream cmdStream = new CmdStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();


        populateCache(tmpDir);

        cmdStream.setRecordType(recordType);
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));

        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        objectNode.put("format", "dwca");
        objectNode.put("url", "hash://sha256/aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d");
        objectNode.put("citation", "some citation");
        ObjectNode resources = new ObjectMapper().createObjectNode();
        resources.put("classpath:/org/globalbioticinteractions/interaction_types_mapping.csv", "hash://sha256/ef045408607c6fb19d6bdf8145e7ce16a0e16bc8be45acbe31da33e1db0c9ea7");
        objectNode.set("resources", resources);

        String input = objectNode.toString();
        cmdStream.setStdin(IOUtils.toInputStream(input, StandardCharsets.UTF_8));
        cmdStream.run();
    }

    @Test
    public void streamSomeReviewNotesNoData() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        runForRecordType(outputStream, errorStream, "review");

        assertHeaderAndMore(outputStream, headerReviewNotes());
    }

    private String headerNames() {
        return "taxonId\ttaxonName\ttaxonRank\ttaxonPathIds\ttaxonPath\ttaxonPathNames\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion";
    }

    static String headerInteractions() {
        return "argumentTypeId\tsourceOccurrenceId\tsourceCatalogNumber\tsourceCollectionCode\tsourceCollectionId\tsourceInstitutionCode\tsourceTaxonId\tsourceTaxonName\tsourceTaxonRank\tsourceTaxonPathIds\tsourceTaxonPath\tsourceTaxonPathNames\tsourceBodyPartId\tsourceBodyPartName\tsourceLifeStageId\tsourceLifeStageName\tsourceSexId\tsourceSexName\tinteractionTypeId\tinteractionTypeName\ttargetOccurrenceId\ttargetCatalogNumber\ttargetCollectionCode\ttargetCollectionId\ttargetInstitutionCode\ttargetTaxonId\ttargetTaxonName\ttargetTaxonRank\ttargetTaxonPathIds\ttargetTaxonPath\ttargetTaxonPathNames\ttargetBodyPartId\ttargetBodyPartName\ttargetLifeStageId\ttargetLifeStageName\ttargetSexId\ttargetSexName\tbasisOfRecordId\tbasisOfRecordName\thttp://rs.tdwg.org/dwc/terms/eventDate\tdecimalLatitude\tdecimalLongitude\tlocalityId\tlocalityName\treferenceDoi\treferenceUrl\treferenceCitation\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion";
    }

    private String headerReviewNotes() {
        return "reviewId\treviewDate\treviewer\tnamespace\treviewCommentType\treviewComment\tarchiveURI\treferenceUrl\tinstitutionCode\tcollectionCode\tcollectionId\tcatalogNumber\toccurrenceId\tsourceCitation\tdataContext";
    }

}