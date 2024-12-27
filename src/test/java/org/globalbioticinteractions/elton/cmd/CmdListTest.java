package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class CmdListTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void listOffline() throws URISyntaxException, IOException {
        String cacheDir = CmdTestUtil.cacheDirTest(tmpFolder);
        ByteArrayOutputStream out = runCmd(cacheDir, false);
        assertThat(out.toString(), startsWith("globalbioticinteractions/template-dataset"));
    }

    @Test
    public void listOfflineWithProv() throws URISyntaxException, IOException {
        String cacheDir = CmdTestUtil.cacheDirTest(tmpFolder);
        ByteArrayOutputStream out = runCmd(cacheDir, true);
        String actual = out.toString();
        assertThat(actual, not(startsWith("globalbioticinteractions/template-dataset")));

        String[] lines = StringUtils.split(actual, '\n');
        assertThat(lines.length, is(26));
        assertThat(lines[0], startsWith("<https://globalbioticinteractions.org/elton> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#SoftwareAgent>"));
        assertThat(lines[lines.length - 1], containsString("<http://www.w3.org/ns/prov#endedAtTime>"));

        Stream<String> associations = getLinesWith(lines, RefNodeConstants.WAS_ASSOCIATED_WITH);

        assertThat(associations.collect(Collectors.joining()), startsWith("<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/template-dataset> <http://www.w3.org/ns/prov#wasAssociatedWith> <https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <"));

        Stream<String> versions = getLinesWith(lines, RefNodeConstants.HAS_VERSION);

        assertThat(versions.collect(Collectors.joining()), containsString("hasVersion> <hash://sha256/2bcc437219f978f35db9cab36922628b3b87b7fea688efcf4f325c13681663c6> <"));

        Stream<String> wasDerivedFrom = getLinesWith(lines, RefNodeConstants.WAS_DERIVED_FROM);

        List<String> collect = wasDerivedFrom.collect(Collectors.toList());
        assertThat(collect.size(), is(2));
        assertThat(collect.get(0), startsWith("<hash://sha256/2bcc437219f978f35db9cab36922628b3b87b7fea688efcf4f325c13681663c6> <http://www.w3.org/ns/prov#wasDerivedFrom> <hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f>"));
        assertThat(collect.get(1), startsWith("<hash://sha256/2bcc437219f978f35db9cab36922628b3b87b7fea688efcf4f325c13681663c6> <http://www.w3.org/ns/prov#wasDerivedFrom> <jar:hash://sha256/631d3777cf83e1abea848b59a6589c470cf0c7d0fd99682c4c104481ad9a543f!/globalbioticinteractions-template-dataset-e68f448/globi.json>"));

        Stream<String> format = getLinesWith(lines, RefNodeConstants.HAS_FORMAT);
        List<String> formatList = format.collect(Collectors.toList());

        assertThat(formatList.size(), is(1));
        assertThat(formatList.get(0), startsWith("<https://zenodo.org/record/207958/files/globalbioticinteractions/template-dataset-0.0.2.zip> <http://purl.org/dc/elements/1.1/format> \"application/globi\""));

        File namespaceList = new File(cacheDir, "2bcc437219f978f35db9cab36922628b3b87b7fea688efcf4f325c13681663c6");

        assertThat(FileUtils.readFileToString(namespaceList, StandardCharsets.UTF_8), is("globalbioticinteractions/template-dataset\n"));
    }

    private Stream<String> getLinesWith(String[] lines, IRI hasVersion) {
        return Arrays.stream(lines).filter(line -> StringUtils.contains(line, hasVersion.getIRIString()));
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
        cmd.setEnableProvMode(enableProvMode);

        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(out1);
        cmd.setStdout(out);
        cmd.run();
        return out1;
    }


}