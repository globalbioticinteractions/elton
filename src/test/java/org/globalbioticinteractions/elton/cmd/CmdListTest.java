package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CmdListTest {

    @Test
    public void listOffline() throws URISyntaxException {
        JCommander jc = new CmdLine().buildCommander();
        String cacheDir = CmdTestUtil.cacheDirTest();
        ByteArrayOutputStream out = runCmd(jc, cacheDir);
        assertThat(out.toString(), startsWith("globalbioticinteractions/template-dataset"));
    }

    @Test
    public void listOfflineNonExistingCacheDir() throws URISyntaxException {
        String cacheDirNonExisting = "this/does/not/exist";
        assertThat(new File(cacheDirNonExisting).exists(), is(false));
        JCommander jc = new CmdLine().buildCommander();
        ByteArrayOutputStream out = runCmd(jc, cacheDirNonExisting);
        assertThat(StringUtils.isBlank(out.toString()), is(true));
        assertThat(new File(cacheDirNonExisting).exists(), is(false));
    }

    private ByteArrayOutputStream runCmd(JCommander jc, String cacheDir) {
        jc.parse("list", "--offline", "--cache-dir=" + cacheDir);

        Assert.assertEquals(jc.getParsedCommand(), "list");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdList.class);
        CmdList cmdList = (CmdList) actual.getObjects().get(0);

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmdList.run(out);
        return out1;
    }


}