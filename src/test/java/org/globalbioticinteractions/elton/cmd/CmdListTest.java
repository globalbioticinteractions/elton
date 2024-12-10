package org.globalbioticinteractions.elton.cmd;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CmdListTest {

    @Test
    public void listOffline() throws URISyntaxException {
        String cacheDir = CmdTestUtil.cacheDirTest();
        ByteArrayOutputStream out = runCmd(cacheDir);
        assertThat(out.toString(), startsWith("globalbioticinteractions/template-dataset"));
    }

    @Test
    public void listOfflineNonExistingCacheDir() throws URISyntaxException {
        String cacheDirNonExisting = "this/does/not/exist";
        assertThat(new File(cacheDirNonExisting).exists(), is(false));
        ByteArrayOutputStream out = runCmd(cacheDirNonExisting);
        assertThat(out.toString(), is("local\n"));
        assertThat(new File(cacheDirNonExisting).exists(), is(false));
    }

    private ByteArrayOutputStream runCmd(String cacheDir) {
        CmdList cmd = new CmdList();
        cmd.setDataDir(cacheDir);
        cmd.setProvDir(cacheDir);

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.run(out);
        return out1;
    }


}