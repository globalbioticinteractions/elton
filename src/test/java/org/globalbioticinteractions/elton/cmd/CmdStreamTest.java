package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.KeyTo3LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
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
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("processing data stream from [local]..."));
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), endsWith("processing data stream from [local]..."));
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

        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("processing data stream from [local]..."));
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
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), Is.is("processing data stream from [globalbioticinteractions/template-dataset]...done.\ndone processing [globalbioticinteractions/template-dataset].\n"));
    }

    private void assertHeaderAndMore(ByteArrayOutputStream outputStream, String prefix) {
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
    public void streamSomeProvStatementsDwCA() throws IOException, URISyntaxException {

        URL resource = getClass().getResource("/ucsb-izc-slim-dwca.zip");
        assertNotNull(resource);

        IRI iri = RefNodeFactory.toIRI(resource.toURI());

        String provLogGeneratedByElton = "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/ucsb-izc> <http://www.w3.org/ns/prov#wasAssociatedWith> " + iri + " <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                iri + " <http://purl.org/dc/elements/1.1/format> \"application/dwca\" <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                iri + " <http://purl.org/pav/hasVersion> <hash://sha256/aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n";

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

        assertThat(filenames, hasItems("aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d"));

        assertHeaderAndMore(outputStream, headerInteractions());
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), Is.is("processing data stream from [globalbioticinteractions/ucsb-izc]...done.\ndone processing [globalbioticinteractions/ucsb-izc].\n"));
    }

    @Test
    public void streamSomeProvStatementsEmbeddedDwCA() throws IOException, URISyntaxException {

        URL resource = getClass().getResource("/ucsb-izc-in-globi-config.zip");
        assertNotNull(resource);

        String dwcArchive = "/ucsb-izc-slim-dwca.zip";
        IRI dwcaReferenceReplacement = Hasher.calcHashIRI(getClass().getResourceAsStream(dwcArchive), NullOutputStream.INSTANCE, HashType.sha256);

        String dwcaReference = "<hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423>";

        String provLogGeneratedByElton = "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/ucsb-izc> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://github.com/globalbioticinteractions/ucsb-izc/archive/65ef047765bf2eb5ef8108371b429489bf9c2a27.zip> <urn:uuid:7be4be8f-c170-41d1-a46a-6f3415dd6cea> .\n" +
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

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();
        CmdStream cmdStream = new CmdStream();


        populateCacheWithResource(tmpDir, "/ucsb-izc-in-globi-config.zip");
        populateCacheWithResource(tmpDir, dwcArchive);


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
        assertThat(numberOfFilesBefore, Is.is(2L));


        cmdStream.run();

        Collection<File> filesAfter = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesAfter = filesAfter.stream().filter(File::isFile).count();

        assertThat(numberOfFilesAfter, Is.is(2L));

        List<String> filenames = filesAfter.stream().map(File::getName).collect(Collectors.toList());

        assertThat(filenames, hasItems("1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43"));
        assertThat(filenames, hasItems("1c7c3f5e0ef87ebbf1b7905042dfe7665087df3489d555647fb0c8527935fc43"));

        assertHeaderAndMore(outputStream, headerInteractions());
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), Is.is("processing data stream from [globalbioticinteractions/ucsb-izc]...done.\ndone processing [globalbioticinteractions/ucsb-izc].\n"));
    }

    private void populateCache(File tmpDir) throws IOException {
        populateCacheWithResource(tmpDir, "/ucsb-izc-slim-dwca.zip");
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
        cmdStream.setStdin(IOUtils.toInputStream("{ \"namespace\": \"name/space\", \"format\": \"dwca\", \"url\": \"hash://sha256/aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d\", \"citation\": \"some citation\" }", StandardCharsets.UTF_8));
        cmdStream.run();

        assertHeaderAndMore(outputStream, headerInteractions());

        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("processing data stream from [name/space]..."));
    }

    @Test
    public void streamSomeNames() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        runForRecordType(outputStream, errorStream, "name");

        assertHeaderAndMore(outputStream, headerNames());

        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), startsWith(headerNames()));
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("processing data stream from [local]..."));
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
        cmdStream.setStdin(IOUtils.toInputStream("{ \"format\": \"dwca\", \"url\": \"hash://sha256/aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d\", \"citation\": \"some citation\" }", StandardCharsets.UTF_8));
        cmdStream.run();
    }

    @Test
    public void streamSomeReviewNotesNoData() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        runForRecordType(outputStream, errorStream, "review");

        assertHeaderAndMore(outputStream, headerReviewNotes());

        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), is("processing data stream from [local]...done.\ndone processing [local].\n"));
    }

    private String headerNames() {
        return "taxonId\ttaxonName\ttaxonRank\ttaxonPathIds\ttaxonPath\ttaxonPathNames\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion";
    }

    private String headerInteractions() {
        return "argumentTypeId\tsourceOccurrenceId\tsourceCatalogNumber\tsourceCollectionCode\tsourceCollectionId\tsourceInstitutionCode\tsourceTaxonId\tsourceTaxonName\tsourceTaxonRank\tsourceTaxonPathIds\tsourceTaxonPath\tsourceTaxonPathNames\tsourceBodyPartId\tsourceBodyPartName\tsourceLifeStageId\tsourceLifeStageName\tsourceSexId\tsourceSexName\tinteractionTypeId\tinteractionTypeName\ttargetOccurrenceId\ttargetCatalogNumber\ttargetCollectionCode\ttargetCollectionId\ttargetInstitutionCode\ttargetTaxonId\ttargetTaxonName\ttargetTaxonRank\ttargetTaxonPathIds\ttargetTaxonPath\ttargetTaxonPathNames\ttargetBodyPartId\ttargetBodyPartName\ttargetLifeStageId\ttargetLifeStageName\ttargetSexId\ttargetSexName\tbasisOfRecordId\tbasisOfRecordName\thttp://rs.tdwg.org/dwc/terms/eventDate\tdecimalLatitude\tdecimalLongitude\tlocalityId\tlocalityName\treferenceDoi\treferenceUrl\treferenceCitation\tnamespace\tcitation\tarchiveURI\tlastSeenAt\tcontentHash\teltonVersion";
    }

    private String headerReviewNotes() {
        return "reviewId\treviewDate\treviewer\tnamespace\treviewCommentType\treviewComment\tarchiveURI\treferenceUrl\tinstitutionCode\tcollectionCode\tcollectionId\tcatalogNumber\toccurrenceId\tsourceCitation\tdataContext";
    }

}