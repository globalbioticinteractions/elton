package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

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
    public void streamSomeInteractions() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream("{ \"url\": \"bla.tsv\", \"citation\": \"some citation\" }", StandardCharsets.UTF_8));
        cmdStream.run();

        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), startsWith(headerInteractions()));
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
                "<jar:hash://sha256/76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44!/template-dataset-b92cd44dcba945c760229a14d3b9becb2dd0c147/interactions.tsv> <http://purl.org/pav/hasVersion> <hash://sha256/d84999936296e4b85086f2851f4459605502f4eb80b9484049b81d34f43b2ff1> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();
        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();
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
        assertThat(numberOfFilesBefore, Is.is(0L));


        cmdStream.run();

        Collection<File> filesAfter = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesAfter = filesAfter.stream().filter(File::isFile).count();

        assertThat(numberOfFilesAfter, Is.is(2L));

        List<String> filenames = filesAfter.stream().map(File::getName).collect(Collectors.toList());

        assertThat(filenames, hasItems("d84999936296e4b85086f2851f4459605502f4eb80b9484049b81d34f43b2ff1"));
        assertThat(filenames, hasItems("76c00c8b64e422800b85d29db93bcfa9ebee999f52f21e16cbd00ba750e98b44"));

        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), startsWith(headerInteractions()));
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), Is.is("tracking [globalbioticinteractions/template-dataset]...done.\nwrote [globalbioticinteractions/template-dataset]\n"));
    }


    @Test
    public void streamSomeInteractionsCustomNamespace() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream("{ \"namespace\": \"name/space\", \"url\": \"bla.tsv\", \"citation\": \"some citation\" }", StandardCharsets.UTF_8));
        cmdStream.run();

        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), startsWith(headerInteractions()));
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("tracking [name/space]..."));
    }

    @Test
    public void streamSomeNames() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();
        cmdStream.setRecordType("name");
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream("{ \"format\": \"dwca\", \"url\": \"bla.tsv\", \"citation\": \"some citation\" }", StandardCharsets.UTF_8));
        cmdStream.run();

        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), startsWith(headerNames()));
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("tracking [local]..."));
    }

    @Test
    public void streamSomeReviewNotesNoData() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        CmdStream cmdStream = new CmdStream();
        cmdStream.setRecordType("review");
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream("{ \"format\": \"dwca\", \"url\": \"bla.tsv\", \"citation\": \"some citation\" }", StandardCharsets.UTF_8));
        cmdStream.run();

        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), startsWith(headerReviewNotes()));
        assertThat(new String(outputStream.toByteArray(), StandardCharsets.UTF_8), containsString("failed to add dataset associated with namespace [local]"));
        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), startsWith("tracking [local]..."));
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