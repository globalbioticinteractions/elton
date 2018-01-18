package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class CmdNamesTest {

    @Test
    public void names() throws URISyntaxException {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("names", "--cache-dir=" + CmdTestUtil.cacheDirTest(), "globalbioticinteractions/template-dataset");

        Assert.assertEquals(jc.getParsedCommand(), "names");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdNames.class);
        CmdNames cmdNames = (CmdNames) actual.getObjects().get(0);
        assertThat(cmdNames.getNamespaces().size(), is(1));
        assertThat(cmdNames.getNamespaces(), hasItem("globalbioticinteractions/template-dataset"));

        if (actual.getObjects().get(0) instanceof Runnable) {
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(out1);
            ((CmdNames) actual.getObjects().get(0)).run(out);
            assertThat(out1.toString(), startsWith("\tLeptoconchus incycloseris"));
            assertThat(out1.toString().split("\n").length, is(22));
        }
    }


}