package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.number.OrderingComparison.greaterThan;

public class CmdListIT {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void listOnline() throws URISyntaxException, IOException {
        String cacheDir = CmdTestUtil.cacheDirTest(tmpFolder);
        ByteArrayOutputStream out = runCmd(cacheDir, false);
        assertThat(out.toString(), startsWith("globalbioticinteractions/template-dataset"));
    }

    @Test
    public void listOnlineWithProv() throws URISyntaxException, IOException {
        String cacheDir = CmdTestUtil.cacheDirTest(tmpFolder);
        ByteArrayOutputStream out = runCmd(cacheDir, true);
        String actual = out.toString();
        assertThat(actual, not(startsWith("globalbioticinteractions/template-dataset")));

        String[] lines = StringUtils.split(actual, '\n');
        assertThat(lines.length, is(greaterThan(19)));
        assertThat(lines[0], startsWith("<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent>"));
        assertThat(lines[lines.length - 1], containsString("<http://www.w3.org/ns/prov#endedAtTime>"));

        long numberOfWasDerivedFromCount = Arrays.stream(lines).filter(line -> StringUtils.contains(line, RefNodeConstants.WAS_DERIVED_FROM.getIRIString())).count();

        // lists derived from offline resources log their dependencies
        assertThat(numberOfWasDerivedFromCount, is(not(0L)));
    }

    @Test
    public void listOfflineNonExistingCacheDir() throws URISyntaxException {
        String cacheDirNonExisting = "this/does/not/exist";
        assertThat(new File(cacheDirNonExisting).exists(), is(false));
        ByteArrayOutputStream out = runCmd(cacheDirNonExisting, false);
        assertThat(out.toString(), is("local\n"));
        assertThat(new File(cacheDirNonExisting).exists(), is(false));
    }

    private ByteArrayOutputStream runCmd(String cacheDir, boolean enableProvMode) {
        CmdList cmd = new CmdList();
        cmd.setDataDir(cacheDir);
        cmd.setProvDir(cacheDir);
        cmd.setOnline(true);
        cmd.setEnableProvMode(enableProvMode);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);
        cmd.run();
        return out1;
    }


}