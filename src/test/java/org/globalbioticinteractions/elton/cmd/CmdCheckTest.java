package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CmdCheckTest {

    private String testTmpDir = "target/test-cache";

    @Before
    public void generateTestUUID() {
        this.testTmpDir = "target/test-cache/" + UUID.randomUUID();
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
    public void runCheckNonExisting() throws URISyntaxException {
        assertThat(new File("this/should/not/exist").exists(), is(false));
        JCommander jc = new CmdLine().buildCommander();
        runOfflineWith(jc, "this/should/not/exist");
        assertThat(new File("this/should/not/exist").exists(), is(false));
    }


    @Test
    public void runCheckLocal() throws URISyntaxException {

        String localTestPath = "src/test/resources/dataset-local-test";
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        runCheck(localTestPath, errOs, outOs, 10);

        assertThat(errOs.toString(), startsWith("checking [local] at [file:///"));
        assertThat(errOs.toString(), endsWith("done.\n"));
        assertThat(outOs.toString(), startsWith("local\tfile:///"));
        assertThat(outOs.toString(), endsWith("local\t11 interaction(s)\nlocal\t0 error(s)\nlocal\t0 warning(s)\n"));
    }

    private void runCheck(String localTestPath, ByteArrayOutputStream errOs, ByteArrayOutputStream outOs, int maxLines) {
        CmdCheck cmdCheck = new CmdCheck();
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
    public void runCheckLocalNoCitation() throws URISyntaxException {
        assertOneWarning("src/test/resources/dataset-local-test-no-citation");
    }

    @Test(expected = RuntimeException.class)
    public void runCheckLocalWithRemoteDeps() throws URISyntaxException {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-local-with-remote-dependency-test", errOs, outOs, 10);
        } finally {
            assertThat(outOs.toString(), endsWith("local\t1 interaction(s)\nlocal\t0 error(s)\nlocal\t3 warning(s)\n"));
        }
    }

    @Test(expected = RuntimeException.class)
    public void runCheckLocalWithRemoteDepsMax1Line() throws URISyntaxException {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-local-with-remote-dependency-test", errOs, outOs, 1);
        } finally {
            assertThat(outOs.toString(), endsWith("local\t1 interaction(s)\nlocal\t0 error(s)\nlocal\t3 warning(s)\n"));
        }
    }

    @Test(expected = RuntimeException.class)
    public void runCheckLocalWithResourceRelation() throws URISyntaxException {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck("src/test/resources/dataset-fmnh-rr-test", errOs, outOs, 10);
        } finally {
            assertThat(outOs.toString(), endsWith("local\t6 interaction(s)\nlocal\t0 error(s)\nlocal\t1 warning(s)\n"));
        }
    }

    @Test(expected = RuntimeException.class)
    public void runCheckLocalBlankCitation() throws URISyntaxException {
        assertOneWarning("src/test/resources/dataset-local-test-blank-citation");
    }

    private void assertOneWarning(String localTestPath) {
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        try {
            runCheck(localTestPath, errOs, outOs, 10);
        } finally {
            assertThat(outOs.toString(), endsWith("local\t11 interaction(s)\nlocal\t0 error(s)\nlocal\t1 warning(s)\n"));
        }
    }


    @Test
    public void runCheckLocalNoRepo() throws URISyntaxException {

        CmdCheck cmdCheck = new CmdCheck();
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
}