package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.eol.globi.data.LogUtil;
import org.eol.globi.domain.LogContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CmdCheckTest {

    private String testTmpDir = "target/test-cache";
    private CmdCheck cmdCheck;

    @Before
    public void init() {
        this.testTmpDir = "target/test-cache/" + UUID.randomUUID();
        this.cmdCheck = new CmdCheck();
        cmdCheck.setDateFactory(() -> new Date(0));
        cmdCheck.setReviewerName("elton-dev");
        cmdCheck.setReviewId("6a550a42-8951-416a-a187-34edbd3f87d0");
    }

    @After
    public void cleanCache() {
        FileUtils.deleteQuietly(new File(getTestTmpDir()));
    }

    private String getTestTmpDir() {
        return testTmpDir;
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
        runCheck(localTestPath, errOs, outOs, 10);

        assertThat(errOs.toString(), startsWith("Reviewing [local] at [file:///"));
        assertThat(errOs.toString(), endsWith("done.\n"));

        assertThat(outOs.toString(), startsWith("reviewId\treviewDate\treviewer\tnamespace\treviewComment\t"));
        assertThat(outOs.toString().split("\n")[1], startsWith("6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\tfile:///"));
        assertThat(outOs.toString().split("\n")[1], endsWith("\t\t\t\t\t\t\t\t\t"));
        assertThat(outOs.toString(), endsWith(
                "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t11 interaction(s)\t\t\t\t\t\t\t\t\t\n" +
                        "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t0 error(s)\t\t\t\t\t\t\t\t\t\n" +
                        "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t0 warning(s)\t\t\t\t\t\t\t\t\t"));
    }

    private void runCheck(String localTestPath, ByteArrayOutputStream errOs, ByteArrayOutputStream outOs, int maxLines) {
        PrintStream err = new PrintStream(errOs);
        cmdCheck.setStderr(err);
        PrintStream out = new PrintStream(outOs);
        cmdCheck.setStdout(out);
        cmdCheck.setWorkDir(Paths.get(localTestPath).toUri());
        cmdCheck.setTmpDir(getTestTmpDir());
        cmdCheck.setMaxLines(maxLines);
        cmdCheck.run();
    }

    @Test(expected = RuntimeException.class)
    public void runCheckLocalNoCitation() {
        assertOneWarning("src/test/resources/dataset-local-test-no-citation");
    }

    @Test
    public void runCheckLocalWithRemoteDeps() {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-local-with-remote-dependency-test", errOs, outOs, 10);
        } finally {
            assertThat(outOs.toString(), endsWith(
                    "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t2 interaction(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t0 error(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t0 warning(s)\t\t\t\t\t\t\t\t\t"));
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
                    "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t2 interaction(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t0 error(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t0 warning(s)\t\t\t\t\t\t\t\t\t"));
        }
    }

    @Test(expected = RuntimeException.class)
    public void runCheckLocalWithResourceRelation() {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-fmnh-rr-test", errOs, outOs, 10);
        } finally {
            String reviewReport = outOs.toString();
            String[] lines = StringUtils.splitPreserveAllTokens(reviewReport, '\n');
            int expectedNumberOfColumns = 14;
            for (String line : lines) {
                int numberOfColumns = StringUtils.splitPreserveAllTokens(line, '\t').length;
                assertThat("mismatching number of columns in line [" + line + "]", numberOfColumns, is(expectedNumberOfColumns));
            }
            assertThat(reviewReport, endsWith(
                    "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t6 interaction(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t0 error(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t1 warning(s)\t\t\t\t\t\t\t\t\t"));
        }
    }

    @Test(expected = RuntimeException.class)
    public void runCheckLocalBlankCitation() {
        assertOneWarning("src/test/resources/dataset-local-test-blank-citation");
    }

    private void assertOneWarning(String localTestPath) {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck(localTestPath, errOs, outOs, 10);
        } finally {
            assertThat(outOs.toString(), endsWith(
                    "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t11 interaction(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t0 error(s)\t\t\t\t\t\t\t\t\t\n" +
                            "6a550a42-8951-416a-a187-34edbd3f87d0\t1970-01-01T00:00:00Z\telton-dev\tlocal\t1 warning(s)\t\t\t\t\t\t\t\t\t"));
        }
    }


    @Test
    public void runCheckLocalNoRepo() {

        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errOs);
        cmdCheck.setStderr(err);
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outOs);
        cmdCheck.setStdout(out);
        cmdCheck.setWorkDir(Paths.get("src/test/resources/dataset-local-test-non-exist").toUri());
        try {
            cmdCheck.run();
            fail("should have thrown");
        } catch (Throwable ex) {

        }

        assertThat(errOs.toString(), is(""));
    }

    private void runOfflineWith(JCommander jc, String cacheDir) {
        jc.parse("check", "--cache-dir=" + cacheDir, "globalbioticinteractions/template-dataset");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object o = actual.getObjects().get(0);
        Assert.assertEquals(o.getClass(), CmdCheck.class);
        CmdCheck cmdCheck = (CmdCheck) o;

        assertThat(cmdCheck.getNamespaces(), is(Collections.singletonList("globalbioticinteractions/template-dataset")));
        assertThat(cmdCheck.getCacheDir(), is(cacheDir));

        CmdLine.run(actual);
    }

    @Test
    public void findTermValue() throws IOException {
        LogContext sourceOccurrenceId1 = LogUtil.contextFor(new HashMap<String, String>() {{
            put("sourceOccurrenceId", "a8c61ad5-4cda-47df-9cb6-6c64b0e71bfa");
        }});
        String sourceOccurrenceId = CmdCheck.getFindTermValue(new ObjectMapper().readTree(sourceOccurrenceId1.toString()), "sourceOccurrenceId");
        assertThat(sourceOccurrenceId, is("a8c61ad5-4cda-47df-9cb6-6c64b0e71bfa"));
    }

}