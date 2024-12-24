package org.globalbioticinteractions.elton.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.data.LogUtil;
import org.eol.globi.domain.LogContext;
import org.eol.globi.util.CSVTSVUtil;
import org.globalbioticinteractions.elton.Elton;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotNull;

public class CmdReviewTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    private CmdReview cmdReview;

    @Before
    public void init() throws IOException {
        this.cmdReview = new CmdReview();
        cmdReview.setDateFactory(() -> new Date(0));
        cmdReview.setDataDir(tmpDir.newFolder().getAbsolutePath());
        cmdReview.setReviewerName("elton-dev");
        cmdReview.setReviewId("6a550a42-8951-416a-a187-34edbd3f87d0");
    }

    @Test
    public void runCheck() throws URISyntaxException, IOException {
        String cacheDir = CmdTestUtil.cacheDirTest(tmpDir);
        runOfflineWith(cacheDir);
    }

    @Test
    public void runCheckNonExisting() {
        assertThat(new File("this/should/not/exist").exists(), is(false));
        runOfflineWith("this/should/not/exist");
        assertThat(new File("this/should/not/exist").exists(), is(false));
    }


    @Test
    public void runCheckLocal() throws IOException {
        String localTestPath = "src/test/resources/dataset-local-test";
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        runCheck(localTestPath, errOs, outOs, 100L, false, 3);

        assertThat(errOs.toString(), containsString("creating review [local]..."));
        assertThat(errOs.toString(), endsWith("done.\n"));

        assertThat(outOs.toString(), startsWith("reviewId\treviewDate\treviewer\tnamespace\treviewCommentType\treviewComment\t"));
        String[] lines = outOs.toString().split("\n");
        String thirdToLast = "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t11 interaction(s)\t\t\t\t\t\t\t\t\t";
        String secondToLast = "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t0 note(s)\t\t\t\t\t\t\t\t\t";
        String last = "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t11 info(s)\t\t\t\t\t\t\t\t\t";
        assertThat(lines[lines.length - 1], is(last));
        assertThat(lines[lines.length - 2], is(secondToLast));
        assertThat(lines[lines.length - 3], is(thirdToLast));
    }

    @Test
    public void runCheckLocalWithProv() throws IOException {
        String localTestPath = "src/test/resources/dataset-local-test";
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        runCheck(
                localTestPath,
                errOs,
                outOs,
                100L,
                true,
                3
        );

        assertThat(errOs.toString(), containsString("creating review [local]..."));
        assertThat(errOs.toString(), endsWith("done.\n"));

        assertThat(outOs.toString(), not(startsWith("reviewId\treviewDate\treviewer\tnamespace\treviewCommentType\treviewComment\t")));
        String[] lines = outOs.toString().split("\n");

        long numberOfDerivedFromStatements = Arrays
                .stream(lines)
                .filter(line -> StringUtils.contains(line, "wasDerivedFrom"))
                .count();

        assertThat(numberOfDerivedFromStatements, is(not(0L)));
        assertThat(lines[lines.length - 1], containsString("endedAtTime"));
    }

    @Test(expected = RuntimeException.class)
    public void throwOnEmpty() throws IOException {
        String localTestPath = "src/test/resources/dataset-local-test-no-records";
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        runCheck(localTestPath, errOs, outOs, 100, false, 3);
    }

    @Test
    public void runCheckLocalSummaryOnly() throws IOException {
        String localTestPath = "src/test/resources/dataset-local-test";
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        runCheck(localTestPath, errOs, outOs,
                100,
                Collections.singletonList(ReviewCommentType.summary),
                false,
                3);

        assertThat(errOs.toString(), containsString("creating review [local]..."));
        assertThat(errOs.toString(), endsWith("done.\n"));

        assertThat(outOs.toString(), startsWith("reviewId\treviewDate\treviewer\tnamespace\treviewCommentType\treviewComment\t"));
        String[] lines = outOs.toString().split("\n");
        assertThat(lines.length, is(5));
        String thirdToLast = "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t11 interaction(s)\t\t\t\t\t\t\t\t\t";
        String secondToLast = "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t0 note(s)\t\t\t\t\t\t\t\t\t";
        String last = "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t11 info(s)\t\t\t\t\t\t\t\t\t";
        assertThat(lines[lines.length - 1], is(last));
        assertThat(lines[lines.length - 2], is(secondToLast));
        assertThat(lines[lines.length - 3], is(thirdToLast));
    }

    private void runCheck(String localTestPath, ByteArrayOutputStream errOs, ByteArrayOutputStream outOs, long maxLines, boolean enableProvMode, int expectedNumberOfDataFiles) throws IOException {
        runCheck(localTestPath, errOs, outOs, maxLines, Arrays.asList(ReviewCommentType.values()), enableProvMode, expectedNumberOfDataFiles);
    }

    private void runCheck(String localTestPath,
                          ByteArrayOutputStream errOs,
                          ByteArrayOutputStream outOs,
                          long maxLines,
                          List<ReviewCommentType> commentTypes,
                          boolean enableProvMode,
                          int expectedNumberOfDataFiles) throws IOException {
        PrintStream err = new PrintStream(errOs);
        cmdReview.setStderr(err);
        PrintStream out = new PrintStream(outOs);
        cmdReview.setStdout(out);
        cmdReview.setWorkDir(Paths.get(localTestPath).toAbsolutePath().toString());
        String dataDir = tmpDir.newFolder().getAbsolutePath();
        cmdReview.setDataDir(dataDir);
        cmdReview.setProvDir(dataDir);
        cmdReview.setMaxLines(maxLines);
        cmdReview.setDesiredReviewCommentTypes(commentTypes);
        cmdReview.setEnableProvMode(enableProvMode);

        cmdReview.run();
        assertThat(CmdTestUtil.numberOfDataFiles(dataDir), is(expectedNumberOfDataFiles));
        Collection<File> files = FileUtils.listFiles(new File(dataDir), null, true);
        long numberOfAccessTsvFiles = files.stream().filter(file -> StringUtils.endsWith(file.getName(), "access.tsv")).count();
        assertThat(numberOfAccessTsvFiles, is(enableProvMode ? 0L : 1L));
    }

    @Test
    public void runCheckLocalNoCitation() throws IOException {
        assertOneWarning("src/test/resources/dataset-local-test-no-citation");
    }

    @Test
    public void runCheckLocalWithRemoteDeps() throws IOException {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-local-with-remote-dependency-test", errOs, outOs, 100, false, 4);
        } finally {
            assertThat(outOs.toString(), endsWith(
                    "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t2 interaction(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t0 note(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t2 info(s)\t\t\t\t\t\t\t\t\t"));
        }
    }

    @Test
    public void runCheckLocalWithPopulatedDynamicPropertiesWithoutInteractionTerms() throws IOException {
        // related to https://github.com/globalbioticinteractions/elton/issues/34
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck(
                    "src/test/resources/ucsb-izc-default-interaction",
                    errOs,
                    outOs,
                    100,
                    false,
                    2
            );
        } finally {

            String reviewReport = outOs.toString();
            String[] lines = StringUtils.split(reviewReport, "\n");
            List<String> reviewMessages = new ArrayList<>();
            for (String line : lines) {
                String[] values = CSVTSVUtil.splitTSV(line);
                reviewMessages.add(values[5]);

            }
            assertThat(reviewMessages, not(hasItem("no interaction type defined")));
        }
    }

    @Test
    public void runCheckLocalWithRemoteDepsMax1Line() throws IOException {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-local-with-remote-dependency-test",
                    errOs,
                    outOs,
                    1,
                    false,
                    4
            );
        } finally {
            assertThat(outOs.toString(), endsWith(
                    "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t2 interaction(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t0 note(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t2 info(s)\t\t\t\t\t\t\t\t\t"));
        }
    }

    @Test
    public void runCheckLocalWithResourceRelation() throws IOException {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-fmnh-rr-test", errOs, outOs, 10, false, 2);
        } finally {
            String reviewReport = outOs.toString();
            String[] lines = StringUtils.splitPreserveAllTokens(reviewReport, '\n');
            int expectedNumberOfColumns = 15;
            for (String line : lines) {
                int numberOfColumns = StringUtils.splitPreserveAllTokens(line, '\t').length;
                assertThat("mismatching number of columns in line [" + line + "]", numberOfColumns, is(expectedNumberOfColumns));
            }
            assertThat(reviewReport, endsWith(
                    "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t7 interaction(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t3 note(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t9 info(s)\t\t\t\t\t\t\t\t\t"));
        }
    }

    @Test
    public void runCheckLocalBlankCitation() throws IOException {
        assertOneWarning("src/test/resources/dataset-local-test-blank-citation");
    }

    private void assertOneWarning(String localTestPath) throws IOException {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck(localTestPath, errOs, outOs, 100, false, 3);
        } finally {
            assertThat(outOs.toString(), endsWith(
                    "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t11 interaction(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t1 note(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t11 info(s)\t\t\t\t\t\t\t\t\t"));
        }
    }


    @Test(expected = RuntimeException.class)
    public void runCheckLocalNoRepo() {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errOs);
        cmdReview.setStderr(err);
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outOs);
        cmdReview.setStdout(out);
        cmdReview.setWorkDir(Paths.get("src/test/resources/dataset-local-test-non-exist").toAbsolutePath().toString());
        try {
            cmdReview.run();
        } finally {
            assertThat(errOs.toString(), is("creating review [local]... failed.\n"));
        }

    }

    private void runOfflineWith(String cacheDir) {
        Elton.run(new String[]{"check", "--cache-dir=" + cacheDir, "globalbioticinteractions/template-dataset"});
    }

    @Test
    public void findTermValue() throws IOException {
        LogContext sourceOccurrenceId1 = LogUtil.contextFor(new TreeMap<String, String>() {{
            put("sourceOccurrenceId", "a8c61ad5-4cda-47df-9cb6-6c64b0e71bfa");
        }});
        String sourceOccurrenceId = CmdReview.getFindTermValueOrEmptyString(new ObjectMapper().readTree(sourceOccurrenceId1.toString()), "sourceOccurrenceId");
        assertThat(sourceOccurrenceId, is("a8c61ad5-4cda-47df-9cb6-6c64b0e71bfa"));
    }

    @Test
    public void findTermValueNull() throws IOException {
        LogContext sourceOccurrenceId1 = LogUtil.contextFor(new TreeMap<String, String>() {{
            put("sourceOccurrenceId", null);
        }});
        JsonNode message = new ObjectMapper().readTree(sourceOccurrenceId1.toString());
        assertNotNull(message);
        String sourceOccurrenceId = CmdReview.getFindTermValueOrEmptyString(
                message, "sourceOccurrenceId");
        assertThat(sourceOccurrenceId, is(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void logTooShort() {
        CmdReview.logReviewComment(new PrintStream(new ByteArrayOutputStream()), "one", "two");
    }

    @Test
    public void logSuffientNumberOfFields() {
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            fields.add(Integer.toString(i));
        }
        CmdReview.logReviewComment(new PrintStream(new ByteArrayOutputStream()), fields.toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void logTooManyFields() {
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            fields.add(Integer.toString(i));
        }
        CmdReview.logReviewComment(new PrintStream(new ByteArrayOutputStream()), fields.toArray());
    }

    @Test
    public void escapeNewlinesAndTabs() throws IOException {
        List<String> fields = new ArrayList<>();
        for (int i = 0; i < CmdReview.LOG_NUMBER_OF_FIELDS; i++) {
            fields.add(i + "bla\n\t");
        }


        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(out1)) {
            CmdReview.logReviewComment(out, fields.toArray());
        }
        String loggedString = IOUtils.toString(out1.toByteArray(), StandardCharsets.UTF_8.name());
        assertThat(loggedString.split("\t").length, is((int) CmdReview.LOG_NUMBER_OF_FIELDS));
        assertThat(loggedString.split("\n").length, is(1));

    }

    @Test
    public void parseAndSortContext() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode review = mapper.createObjectNode();
        review.put("reviewId", "some id");
        review.put("namespace", "some namespace");
        final String content = "{ \"zfoo\": \"bar\", \"foo\": \"bar\"}";
        final JsonNode dataContextSorted = CmdReview.parseAndSortContext(content);

        review.set("context", dataContextSorted);
        String reviewJsonString = mapper.writeValueAsString(review.get("context"));

        assertThat(reviewJsonString, is("{\"foo\":\"bar\",\"zfoo\":\"bar\"}"));


    }

}