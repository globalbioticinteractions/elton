package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.eol.globi.data.LogUtil;
import org.eol.globi.domain.LogContext;
import org.eol.globi.util.CSVTSVUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

public class CmdReviewTest {

    private String testCacheDir = "target/test-cache";
    private CmdReview cmdReview;

    @Before
    public void init() {
        this.testCacheDir = "target/test-cache/" + UUID.randomUUID();
        this.cmdReview = new CmdReview();
        cmdReview.setDateFactory(() -> new Date(0));
        cmdReview.setReviewerName("elton-dev");
        cmdReview.setReviewId("6a550a42-8951-416a-a187-34edbd3f87d0");
    }

    @After
    public void cleanCache() {
        FileUtils.deleteQuietly(new File(getTestCacheDir()));
    }

    private String getTestCacheDir() {
        return testCacheDir;
    }

    @Test
    public void runCheck() throws URISyntaxException {
        JCommander jc = new CmdLine().buildCommander();
        String cacheDir = CmdTestUtil.cacheDirTest();
        runOfflineWith(jc, cacheDir);
    }

    @Test(expected = Exception.class)
    public void runCheckNonExisting() {
        assertThat(new File("this/should/not/exist").exists(), is(false));
        JCommander jc = new CmdLine().buildCommander();
        runOfflineWith(jc, "this/should/not/exist");
        assertThat(new File("this/should/not/exist").exists(), is(false));
    }


    @Test
    public void runCheckLocal() {
        String localTestPath = "src/test/resources/dataset-local-test";
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        runCheck(localTestPath, errOs, outOs, 100);

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

    @Test(expected = RuntimeException.class)
    public void throwOnEmpty() {
        String localTestPath = "src/test/resources/dataset-local-test-no-records";
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        runCheck(localTestPath, errOs, outOs, 100);
    }

    @Test
    public void runCheckLocalSummaryOnly() {
        String localTestPath = "src/test/resources/dataset-local-test";
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        runCheck(localTestPath, errOs, outOs,
                100,
                Collections.singletonList(ReviewCommentType.summary));

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

    private void runCheck(String localTestPath, ByteArrayOutputStream errOs, ByteArrayOutputStream outOs, int maxLines) {
        runCheck(localTestPath, errOs, outOs, maxLines, Arrays.asList(ReviewCommentType.values()));
    }

    private void runCheck(String localTestPath, ByteArrayOutputStream errOs, ByteArrayOutputStream outOs, int maxLines, List<ReviewCommentType> commentTypes) {
        PrintStream err = new PrintStream(errOs);
        cmdReview.setStderr(err);
        PrintStream out = new PrintStream(outOs);
        cmdReview.setStdout(out);
        cmdReview.setWorkDir(Paths.get(localTestPath).toAbsolutePath().toString());
        cmdReview.setCacheDir(getTestCacheDir());
        cmdReview.setMaxLines(maxLines);
        cmdReview.setDesiredReviewCommentTypes(commentTypes);
        cmdReview.run();
    }

    @Test
    public void runCheckLocalNoCitation() {
        assertOneWarning("src/test/resources/dataset-local-test-no-citation");
    }

    @Test
    public void runCheckLocalWithRemoteDeps() {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-local-with-remote-dependency-test", errOs, outOs, 100);
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
            runCheck("src/test/resources/ucsb-izc-default-interaction", errOs, outOs, 100);
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
    public void runCheckLocalWithRemoteDepsMax1Line() {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-local-with-remote-dependency-test", errOs, outOs, 1);
        } finally {
            assertThat(outOs.toString(), endsWith(
                    "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t2 interaction(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t0 note(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tsummary\t2 info(s)\t\t\t\t\t\t\t\t\t"));
        }
    }

    @Test
    public void runCheckLocalWithResourceRelation() {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-fmnh-rr-test", errOs, outOs, 10);
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
    public void runCheckLocalBlankCitation() {
        assertOneWarning("src/test/resources/dataset-local-test-blank-citation");
    }

    private void assertOneWarning(String localTestPath) {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck(localTestPath, errOs, outOs, 100);
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
            assertThat(errOs.toString(), is("failed.\n"));
        }

    }

    private void runOfflineWith(JCommander jc, String cacheDir) {
        jc.parse("check", "--cache-dir=" + cacheDir, "globalbioticinteractions/template-dataset");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object o = actual.getObjects().get(0);
        Assert.assertEquals(o.getClass(), CmdReview.class);
        CmdReview cmdReview = (CmdReview) o;

        assertThat(cmdReview.getNamespaces(), is(Collections.singletonList("globalbioticinteractions/template-dataset")));
        assertThat(cmdReview.getCacheDir(), is(cacheDir));

        CmdLine.run(actual);
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
        String sourceOccurrenceId = CmdReview.getFindTermValueOrEmptyString(new ObjectMapper().readTree(sourceOccurrenceId1.toString()), "sourceOccurrenceId");
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

        review.put("context", dataContextSorted);
        String reviewJsonString = mapper.writeValueAsString(review.get("context"));

        assertThat(reviewJsonString, is("{\"foo\":\"bar\",\"zfoo\":\"bar\"}"));


    }

}