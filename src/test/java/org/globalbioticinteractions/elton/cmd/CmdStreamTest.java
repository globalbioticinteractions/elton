package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.IOUtils;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class CmdStreamTest {

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