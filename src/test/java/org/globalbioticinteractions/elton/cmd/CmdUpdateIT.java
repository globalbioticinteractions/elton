package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CmdUpdateIT {

    @Before
    public void emptyCache() {
        FileUtils.deleteQuietly(new File("target/tmp-dataset"));
    }

    @Test
    public void update() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("update", "--cache-dir=./bla");
        assertUpdateCmd(jc);

        jc = new CmdLine().buildCommander();
        jc.parse("update", "-c", "./bla");
        assertUpdateCmd(jc);
    }

    @Test
    public void runUpdate() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("update", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/template-dataset");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Assert.assertEquals(actual.getObjects().get(0).getClass(), CmdUpdate.class);

        CmdLine.run(actual);
    }

    @Test
    public void runUpdateHafner() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("update", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/hafner");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Assert.assertEquals(actual.getObjects().get(0).getClass(), CmdUpdate.class);

        CmdLine.run(actual);
    }

    private void assertUpdateCmd(JCommander jc) {
        Assert.assertEquals(jc.getParsedCommand(), "update");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdUpdate.class);
        assertThat(((CmdUpdate) cmd).getCacheDir(), is("./bla"));
    }
}