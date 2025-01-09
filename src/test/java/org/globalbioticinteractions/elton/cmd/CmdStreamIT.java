package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.KeyTo3LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertNotNull;
import static org.globalbioticinteractions.elton.cmd.CmdStreamTest.assertHeaderAndMore;
import static org.globalbioticinteractions.elton.cmd.CmdStreamTest.headerInteractions;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.OrderingComparison.greaterThan;

public class CmdStreamIT {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Test
    public void streamSomeProvStatementsDwCAUsingRemoteRepository() throws IOException, URISyntaxException {

        URL resource = getClass().getResource("/ucsb-izc-slim-dwca.zip");
        assertNotNull(resource);

        IRI iri = RefNodeFactory.toIRI("https://example.org/dwca.zip");

        String provLogGeneratedByElton = "<urn:lsid:globalbioticinteractions.org:globalbioticinteractions/ucsb-izc> <http://www.w3.org/ns/prov#wasAssociatedWith> " + iri + " <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                iri + " <http://purl.org/dc/elements/1.1/format> \"application/dwca\" <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n" +
                iri + " <http://purl.org/pav/hasVersion> <hash://sha256/fba3d1a15752667412d59e984729a847bf5dc2fb995ac12eb22490933f828423> <urn:uuid:16b63a6d-153b-4f16-afed-a67fa09383a7> .\n";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        File tmpDir = folder.newFolder("tmpDir");
        tmpDir.mkdirs();
        CmdStream cmdStream = new CmdStream();


        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setDataDir(tmpDir.getAbsolutePath());
        cmdStream.setWorkDir(tmpDir.getAbsolutePath());
        cmdStream.setStdout(new PrintStream(outputStream));
        cmdStream.setStderr(new PrintStream(errorStream));
        cmdStream.setStdin(IOUtils.toInputStream(provLogGeneratedByElton, StandardCharsets.UTF_8));
        cmdStream.setRemotes(Arrays.asList(URI.create("https://linker.bio")));

        cmdStream.run();

        Collection<File> filesAfter = FileUtils.listFilesAndDirs(
                tmpDir,
                TrueFileFilter.INSTANCE,
                TrueFileFilter.INSTANCE
        );

        long numberOfFilesAfter = filesAfter.stream().filter(File::isFile).count();

        assertThat(numberOfFilesAfter, Is.is(1L));

        List<String> filenames = filesAfter.stream().map(File::getName).collect(Collectors.toList());

        assertThat(filenames, hasItems("aa12991df4efe1e392b2316c50d7cf17117cab7509dcc1918cd42c726bb4e36d"));

        assertThat(new String(errorStream.toByteArray(), StandardCharsets.UTF_8), Is.is("processing data stream from [globalbioticinteractions/ucsb-izc]...done.\ndone processing [globalbioticinteractions/ucsb-izc].\n"));

        assertHeaderAndMore(outputStream, headerInteractions());
    }
}