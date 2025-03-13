package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertNotNull;

public class CmdDwCDataPackageTest {

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @Test
    public void streamArchive() throws IOException, URISyntaxException {
        CmdDefaultParams cmd = new CmdDwCDataPackage();
        String dataDir = CmdTestUtil.cacheDirTest(tmpDir);
        cmd.setDataDir(dataDir);
        cmd.setProvDir(dataDir);
        cmd.setNamespaces(Collections.singletonList("globalbioticinteractions/template-dataset"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        cmd.setStdout(new PrintStream(outputStream));

        cmd.run();

        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        List<String> filenames = new ArrayList<>();
        ZipEntry entry;
        while((entry = zis.getNextEntry()) != null) {
            filenames.add(entry.getName());
            zis.closeEntry();
        }

        assertThat(filenames, hasItems("datapackage.json", "occurrence.csv", "event.csv", "organism-interaction.csv"));

    }

}