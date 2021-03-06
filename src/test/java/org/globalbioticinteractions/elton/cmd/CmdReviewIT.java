package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CmdReviewIT {

    @Before
    public void emptyCache() {
        FileUtils.deleteQuietly(new File("target/tmp-dataset"));
    }

    @Test
    public void runCheck() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("check", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/template-dataset");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Assert.assertEquals(actual.getObjects().get(0).getClass(), CmdReview.class);

        CmdLine.run(actual);
    }

    @Test
    public void runCheckICES() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("check", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/ices");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Assert.assertEquals(actual.getObjects().get(0).getClass(), CmdReview.class);

        CmdLine.run(actual);

        jc = new CmdLine().buildCommander();
        jc.parse("check", "--offline", "--cache-dir=./target/tmp-dataset", "globalbioticinteractions/ices");

        JCommander actualOffline = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actualOffline.getObjects().size(), 1);
        Assert.assertEquals(actualOffline.getObjects().get(0).getClass(), CmdReview.class);

        CmdLine.run(actualOffline);

    }

    @Test
    public void runCheckJLewis() {
        JCommander jc = new CmdLine().buildCommander();
        jc.parse("check", "jhpoelen/JLewis_GoMexSi");

        JCommander actual = jc.getCommands().get(jc.getParsedCommand());
        Assert.assertEquals(actual.getObjects().size(), 1);
        Assert.assertEquals(actual.getObjects().get(0).getClass(), CmdReview.class);

        CmdLine.run(actual);
    }
}