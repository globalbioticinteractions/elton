package org.globalbioticinteractions.elton.cmd;

import com.Ostermiller.util.LabeledCSVParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.util.CSVTSVUtil;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class CmdInitIT {

    @Test
    public void gatherInput2() throws IOException {
        File tempFile = File.createTempFile("init", "test", new File("target"));
        FileUtils.forceDelete(tempFile);
        FileUtils.forceMkdir(tempFile);

        CmdInit cmdInit = new CmdInit();
        cmdInit.setDataUrl("https://idata.idiv.de/mmm/ShowMultimediaData/getFile/?path=Datasets%255c283%255c283_2_FoodWebDataBase_2018_12_10.csv");
        cmdInit.setDataCitation("some citation");
        cmdInit.setWorkDir(tempFile.getAbsolutePath());
        cmdInit.getNamespaces().add("some/namespace");

        assertFalse(new File(tempFile, "README.md").exists());
        assertFalse(new File(tempFile, "globi.json").exists());
        assertFalse(new File(tempFile, ".travis.yaml").exists());
        assertFalse(new File(tempFile, ".gitignore").exists());
        cmdInit.run();

        assertTrue(new File(tempFile, "README.md").exists());
        assertTrue(new File(tempFile, "globi.json").exists());
        assertTrue(new File(tempFile, ".travis.yml").exists());
        assertTrue(new File(tempFile, ".gitignore").exists());
    }

}