package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.KeyTo3LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
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
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("tracking [local]..."));
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), endsWith("tracking [local]..."));
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

        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("tracking [local]..."));
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
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), Is.is("tracking [globalbioticinteractions/template-dataset]...done.\nwrote [globalbioticinteractions/template-dataset]\n"));
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
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), Is.is("tracking [globalbioticinteractions/ucsb-izc]...done.\nwrote [globalbioticinteractions/ucsb-izc]\n"));
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

        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("tracking [name/space]..."));
    }

    @Test
    public void streamSomeNames() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        runForRecordType(outputStream, errorStream, "name");

        assertHeaderAndMore(outputStream, headerNames());

        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), startsWith(headerNames()));
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("tracking [local]..."));
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

        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), is("tracking [local]...done.\nwrote [local]\n"));
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