package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.io.FileUtils;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

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