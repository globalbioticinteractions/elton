package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class CmdLineTest {

    @Before
    public void deleteTmpCache() {
        FileUtils.deleteQuietly(new File("target/tmp-datasets"));
    }

    @Test
    public void check() throws Throwable {
        CmdLine.run(new String[]{"update", "--cache-dir=target/tmp-datasets", "globalbioticinteractions/template-dataset"});
        CmdLine.run(new String[]{"check", "--cache-dir=target/tmp-datasets", "globalbioticinteractions/template-dataset"});
    }

    @Test
    public void invalidCommand() throws Throwable {
        CmdLine.run(new String[]{"bla", "globalbioticinteractions/template-dataset"});
    }

    @Test
    public void noCommand() throws Throwable {
        CmdLine.run(new String[]{});
    }
}