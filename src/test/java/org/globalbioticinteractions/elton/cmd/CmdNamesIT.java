package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CmdNamesIT {

    @Before
    public void emptyCache() {
        //FileUtils.deleteQuietly(new File("target/tmp-dataset"));
    }

    @Test
    public void listNames() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("names", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/template-dataset");

        Assert.assertEquals(jc.getParsedCommand(), "names");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Assert.assertEquals(actual.getObjects().get(0).getClass(), CmdNames.class);

        CmdLine.run(actual);
    }
}