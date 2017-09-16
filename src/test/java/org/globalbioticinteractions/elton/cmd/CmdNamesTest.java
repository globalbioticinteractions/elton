package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CmdNamesTest {

    @Test
    public void names() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("names", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/template-dataset");

        Assert.assertEquals(jc.getParsedCommand(), "names");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdNames.class);
        CmdNames cmdNames = (CmdNames) actual.getObjects().get(0);

        assertThat(cmdNames.getNamespaces().size(), is(1));
        assertThat(cmdNames.getNamespaces(), hasItem("globalbioticinteractions/template-dataset"));
    }


}