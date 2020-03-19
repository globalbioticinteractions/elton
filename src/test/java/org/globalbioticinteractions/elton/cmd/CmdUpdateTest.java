package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CmdUpdateTest {

    @Test
    public void update() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("update", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/template-dataset");

        Assert.assertEquals(jc.getParsedCommand(), "sync");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdUpdate.class);
        CmdUpdate cmdUpdate = (CmdUpdate) actual.getObjects().get(0);

        assertThat(cmdUpdate.getNamespaces().size(), is(1));
        assertThat(cmdUpdate.getNamespaces(), hasItem("globalbioticinteractions/template-dataset"));
    }

    @Test
    public void supportedRegistries() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("update", "--registries", "zenodo,github", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/template-dataset");
        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Object cmd = actual.getObjects().get(0);
        Assert.assertEquals(cmd.getClass(), CmdUpdate.class);
        CmdUpdate cmdUpdate = (CmdUpdate) actual.getObjects().get(0);
        assertThat(cmdUpdate.getRegistryNames(), hasItem("zenodo"));
        assertThat(cmdUpdate.getRegistryNames(), hasItem("github"));
    }


}