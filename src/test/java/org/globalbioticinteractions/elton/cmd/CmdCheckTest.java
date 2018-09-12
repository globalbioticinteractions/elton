package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.eol.globi.data.StudyImporterException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CmdCheckTest {

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

        CmdCheck cmdCheck = new CmdCheck();
        ByteArrayOutputStream errOs = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errOs);
        cmdCheck.setStderr(err);
        ByteArrayOutputStream outOs = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outOs);
        cmdCheck.setStdout(out);
        cmdCheck.setWorkDir(Paths.get("src/test/resources/dataset-local-test").toUri());
        cmdCheck.run();

        assertThat(errOs.toString(), startsWith("checking [local] at [file:///"));
        assertThat(errOs.toString(), endsWith("done.\n"));
        assertThat(outOs.toString(), startsWith("local\tfile:///"));
        assertThat(outOs.toString(), endsWith("local\t11 interaction(s)\nlocal\t0 error(s)\nlocal\t0 warning(s)\n"));
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
        } catch(Throwable ex) {

        }

        assertThat(errOs.toString(), is(""));
    }

    private void runOfflineWith(JCommander jc, String cacheDir) {
        jc.parse("check", "--offline", "--cache-dir=" + cacheDir, "globalbioticinteractions/template-dataset");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object o = actual.getObjects().get(0);
        Assert.assertEquals(o.getClass(), CmdCheck.class);
        CmdCheck cmdCheck = (CmdCheck) o;

        assertThat(cmdCheck.getNamespaces(), is(Collections.singletonList("globalbioticinteractions/template-dataset")));
        assertThat(cmdCheck.isOffline(), is(true));
        assertThat(cmdCheck.getCacheDir(), is(cacheDir));

        CmdLine.run(actual);
    }
}