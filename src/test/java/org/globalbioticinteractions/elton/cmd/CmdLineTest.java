package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.MissingCommandException;
import org.junit.Test;

public class CmdLineTest {

    @Test
    public void check() throws Throwable {
        CmdLine.run(new String[] {"check", "globalbioticinteractions/template-dataset"});
    }

    @Test(expected = MissingCommandException.class)
    public void invalidCommand() throws Throwable {
        CmdLine.run(new String[] {"bla", "globalbioticinteractions/template-dataset"});
    }
}