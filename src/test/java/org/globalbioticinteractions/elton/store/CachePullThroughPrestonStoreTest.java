package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.HashType;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.KeyTo1LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.util.ResourceServiceLocal;
import org.globalbioticinteractions.cache.Cache;
import org.globalbioticinteractions.cache.ContentPathFactoryDepth0;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class CachePullThroughPrestonStoreTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testPrestonStore() throws IOException, URISyntaxException {
        ArrayList<Quad> quads = new ArrayList<>();

        Cache cache = new CachePullThroughPrestonStore(
                "some/namespace"
                , folder.getRoot().getAbsolutePath()
                , new ResourceServiceLocal(in -> in)
                , quads::add, new ContentPathFactoryDepth0()
        );

        assertThat(quads.size(), Is.is(0));

        File namespaceDir = new File(folder.getRoot(), "some/namespace");
        assertFalse(new File(namespaceDir, "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        InputStream is = cache.retrieve(getClass().getResource("hello.txt").toURI());
        assertTrue(new File(namespaceDir, "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        assertThat(IOUtils.toString(is, StandardCharsets.UTF_8.name()), Is.is("hello"));
        assertThat(quads.size(), Is.is(1));
        assertThat(quads.get(0).getSubject().toString(), endsWith("org/globalbioticinteractions/elton/store/hello.txt>"));
        assertThat(quads.get(0).getPredicate().toString(), Is.is("<http://purl.org/pav/hasVersion>"));
        assertThat(quads.get(0).getObject().toString(), Is.is("<hash://sha256/2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824>"));

        assertFalse(new File(folder.getRoot(), "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        BlobStoreAppendOnly blobStore = new BlobStoreAppendOnly(
                new KeyValueStoreLocalFileSystem(
                        folder.getRoot(),
                        new KeyTo1LevelPath(folder.getRoot().toURI()),
                        new ValidatingKeyValueStreamContentAddressedFactory()
                ),
                true,
                HashType.sha256);

        assertFalse(new File(folder.getRoot(), "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        IRI hash = blobStore.put(IOUtils.toInputStream("hello", StandardCharsets.UTF_8.name()));

        assertThat(hash.getIRIString(), Is.is("hash://sha256/2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));

        InputStream inputStream = blobStore.get(hash);

        assertThat(IOUtils.toString(inputStream, StandardCharsets.UTF_8.name()), Is.is("hello"));

        assertTrue(new File(folder.getRoot(), "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());
        assertThat(quads.size(), Is.is(1));
    }


    @Test(expected = IOException.class)
    public void testMissingFromPrestonStore() throws IOException, URISyntaxException {

        Cache cache = new CachePullThroughPrestonStore(
                "some/namespace"
                , folder.getRoot().getAbsolutePath()
                , new ResourceServiceLocal(in -> in), new ContentPathFactoryDepth0()
        );

        File namespaceDir = new File(folder.getRoot(), "some/namespace");
        assertFalse(new File(namespaceDir, "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        URI resourceName = getClass().getResource("hello.txt").toURI();
        cache.retrieve(new URI(resourceName.toString().replace("hello.txt", "this_file_does_not_exist.txt")));
    }

}