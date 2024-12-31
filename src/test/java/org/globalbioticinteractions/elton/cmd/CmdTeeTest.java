package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.awt.peer.ListPeer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class CmdTeeTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void gatherResourcesIRI() throws URISyntaxException, IOException {
        InputStream stdin = IOUtils.toInputStream("hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f", StandardCharsets.UTF_8);

        String dataDir = CmdTestUtil.cacheDirTest(tmpFolder);
        File destDir = tmpFolder.newFolder("destDir");

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);

        runTee(stdin, dataDir, destDir, out);

        assertThat(new File(destDir, "63/1d/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f").exists(), is(true));

        assertThat(out1.toString(), is("hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f"));
    }

    private void generateProvLog(ByteArrayOutputStream out) throws URISyntaxException, IOException {
        CmdInteractions cmd = new CmdInteractions();

        String dataDir = CmdTestUtil.cacheDirTest(tmpFolder);
        cmd.setDataDir(dataDir);

        String provDir = CmdTestUtil.cacheDirTest(tmpFolder);
        cmd.setProvDir(provDir);

        cmd.setEnableProvMode(true);

        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));

        cmd.setStdout(new PrintStream(out));

        assertThat(CmdTestUtil.numberOfDataFiles(dataDir), is(4));

        cmd.run();
    }


    @Test
    public void gatherResourcesIRIFromProvLog() throws URISyntaxException, IOException {

        ByteArrayOutputStream provBytes = new ByteArrayOutputStream();
        generateProvLog(provBytes);

        InputStream stdin = new ByteArrayInputStream(provBytes.toByteArray());

        String dataDir = CmdTestUtil.cacheDirTest(tmpFolder);
        File destDir = tmpFolder.newFolder("destDir");

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);

        runTee(stdin, dataDir, destDir, out);

        assertThat(new File(destDir, "63/1d/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f").exists(), is(true));

        assertThat(out1.toString(), is(new String(provBytes.toByteArray(), StandardCharsets.UTF_8)));
    }

    @Test
    public void gatherResourcesIRIFromProvLogMany() throws URISyntaxException, IOException {

        ByteArrayOutputStream provBytes = new ByteArrayOutputStream();
        generateProvLog(provBytes);

        InputStream stdin = new ByteArrayInputStream(provBytes.toByteArray());

        String dataDir = CmdTestUtil.cacheDirTest(tmpFolder);
        File destDir = tmpFolder.newFolder("destDir");

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);

        runTee(stdin, dataDir, destDir, out);

        assertThat(new File(destDir, "63/1d/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f").exists(), is(true));

        assertThat(out1.toString(), is(new String(provBytes.toByteArray(), StandardCharsets.UTF_8)));
    }

    @Test
    public void contentDetector() {
        final Set<String> contentIds = new TreeSet<>();
        CmdTee.contentDetector(
                new PrintStream(new ByteArrayOutputStream()),
                new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        contentIds.add(s);
                    }
                },
                getClass().getResourceAsStream("prov.nq")
        );

        assertThat(contentIds.size(), is(greaterThan(3)));
    }

    private void runTee(InputStream stdin, String dataDir, File destDir, PrintStream out) {
        CmdTee cmd = new CmdTee();
        cmd.setDataDir(dataDir);
        cmd.setProvDir(dataDir);
        cmd.setStdin(stdin);
        cmd.setStdout(out);
        cmd.setDestDir(destDir);
        cmd.run();
    }

}