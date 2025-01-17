package org.globalbioticinteractions.elton.cmd;

import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;

public class CmdVersionTest {

    @Test
    public void showVersionsVerbose() {
        CmdVersion cmdVersion = new CmdVersion();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        cmdVersion.setVerbose(true);
        cmdVersion.run(new PrintStream(stdout));
        assertThat(new String(stdout.toByteArray(), StandardCharsets.UTF_8), Is.is("elton@dev preston@0.10.3\n"));
    }

    @Test
    public void showVersions() {
        CmdVersion cmdVersion = new CmdVersion();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        cmdVersion.run(new PrintStream(stdout));
        assertThat(new String(stdout.toByteArray(), StandardCharsets.UTF_8), Is.is("dev\n"));
    }

}