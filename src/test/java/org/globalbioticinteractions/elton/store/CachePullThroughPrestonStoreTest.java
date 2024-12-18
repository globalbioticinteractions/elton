package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.HashType;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.StatementListener;
import bio.guoda.preston.store.BlobStoreAppendOnly;
import bio.guoda.preston.store.KeyTo1LevelPath;
import bio.guoda.preston.store.KeyValueStoreLocalFileSystem;
import bio.guoda.preston.store.ValidatingKeyValueStreamContentAddressedFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
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
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class CachePullThroughPrestonStoreTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Literal startTime = RefNodeFactory.toDateTime("2024-12-18T20:54:27.951Z");
    private Literal endTime = RefNodeFactory.toDateTime("2024-12-18T20:54:28.029Z");

    @Test
    public void testPrestonStore() throws IOException, URISyntaxException {
        ArrayList<Quad> quads = new ArrayList<>();

        String dataDir = folder.getRoot().getAbsolutePath();
        String provDir = folder.getRoot().getAbsolutePath();

        pullResource(
                quads,
                dataDir,
                provDir,
                new ActivityListenerImpl(startTime, endTime, new StatementListener() {

                    @Override
                    public void on(Quad quad) {
                        quads.add(quad);
                    }
                }),
                "some/namespace"
        );

        assertFalse(new File(folder.getRoot(), "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        BlobStoreAppendOnly blobStore = new BlobStoreAppendOnly(
                new KeyValueStoreLocalFileSystem(
                        folder.getRoot(),
                        new KeyTo1LevelPath(folder.getRoot().toURI()),
                        new ValidatingKeyValueStreamContentAddressedFactory()
                ),
                true,
                HashType.sha256
        );

        assertFalse(new File(folder.getRoot(), "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        IRI hash = blobStore.put(IOUtils.toInputStream("hello", StandardCharsets.UTF_8.name()));

        assertThat(hash.getIRIString(), Is.is("hash://sha256/2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));

        InputStream inputStream = blobStore.get(hash);

        assertThat(IOUtils.toString(inputStream, StandardCharsets.UTF_8.name()), Is.is("hello"));

        assertTrue(new File(folder.getRoot(), "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        assertThat(quads.size(), Is.is(10));
    }


    @Test(expected = IOException.class)
    public void testMissingFromPrestonStore() throws IOException, URISyntaxException {

        String dataDir = folder.getRoot().getAbsolutePath();
        String provDir = folder.getRoot().getAbsolutePath();
        final StatementListener listener = new StatementListener() {
            @Override
            public void on(Quad quad) {
                // do nothing
            }
        };
        Cache cache = new CachePullThroughPrestonStore(
                "some/namespace"
                , new ResourceServiceLocal(in -> in),
                new ContentPathFactoryDepth0(),
                dataDir,
                provDir,
                new ActivityProxy(
                        Arrays.asList(
                                new ProvLogger(listener),
                                new AccessLogger("some/namespace", provDir)
                        )
                ), new ActivityContext() {
            @Override
            public IRI getActivity() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }
        }, new Supplier<IRI>() {
            @Override
            public IRI get() {
                return RefNodeFactory.toIRI(UUID.randomUUID());
            }
        }
        );

        File namespaceDir = new File(folder.getRoot(), "some/namespace");
        assertFalse(new File(namespaceDir, "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        URI resourceName = getClass().getResource("hello.txt").toURI();
        cache.retrieve(new URI(resourceName.toString().replace("hello.txt", "this_file_does_not_exist.txt")));
    }


    @Test
    public void testCreateDataDirIfNotExists() throws IOException, URISyntaxException {
        ArrayList<Quad> quads = new ArrayList<>();

        String dataDir = folder.newFolder().getAbsolutePath();
        String provDir = folder.newFolder().getAbsolutePath();

        File file = new File(dataDir);
        file.delete();

        assertFalse(file.exists());

        pullResource(
                quads,
                dataDir,
                provDir,
                new ActivityListenerImpl(startTime, endTime, new StatementListener() {

                    @Override
                    public void on(Quad quad) {
                        quads.add(quad);
                    }
                }),
                "some/namespace"
        );

        assertTrue(file.exists());

    }

    private void pullResource(ArrayList<Quad> quads,
                              String dataDir,
                              String provDir,
                              ActivityListener dereferenceListener,
                              String namespace) throws IOException, URISyntaxException {

        ActivityContext ctx = new ActivityContext() {
            @Override
            public IRI getActivity() {
                return RefNodeFactory.toIRI(UUID.fromString("e4fe8c5c-1455-46c6-bcfe-f11a065978aa"));
            }

            @Override
            public String getDescription() {
                return "this is a description";
            }
        };

        Supplier<IRI> uuidFactory = new Supplier<IRI>() {

            @Override
            public IRI get() {
                return RefNodeFactory.toIRI(UUID.fromString("e4fe8c5c-1455-46c6-bcfe-f11a065978aa"));
            }
        };

        Cache cache = new CachePullThroughPrestonStore(
                namespace,
                new ResourceServiceLocal(in -> in),
                new ContentPathFactoryDepth0(),
                dataDir,
                provDir,
                dereferenceListener,
                ctx,
                uuidFactory
        );

        assertThat(quads.size(), Is.is(0));

        File namespaceDir = new File(dataDir, namespace);
        assertFalse(new File(namespaceDir, "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        URI sourceURI = getClass().getResource("hello.txt").toURI();
        InputStream is = cache.retrieve(sourceURI);
        assertTrue(new File(namespaceDir, "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824").exists());

        assertThat(IOUtils.toString(is, StandardCharsets.UTF_8.name()), Is.is("hello"));

        StringBuilder builder = new StringBuilder();
        quads.forEach(q -> {
            builder.append(q.toString());
            builder.append("\n");
        });

        String expected = "<urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> .\n" +
                "<urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> <http://www.w3.org/ns/prov#wasInformedBy> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> .\n" +
                "<urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> <http://www.w3.org/ns/prov#startedAtTime> \"2024-12-18T20:54:27.951Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> .\n" +
                "<hash://sha256/2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824> <http://www.w3.org/ns/prov#wasGeneratedBy> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> .\n" +
                "<hash://sha256/2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824> <http://www.w3.org/ns/prov#qualifiedGeneration> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> .\n" +
                "<urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> <http://www.w3.org/ns/prov#generatedAtTime> \"2024-12-18T20:54:28.029Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> .\n" +
                "<urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/ns/prov#Generation> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> .\n" +
                "<urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> <http://www.w3.org/ns/prov#used> <file:/home/jorrit/proj/globi/elton/target/test-classes/org/globalbioticinteractions/elton/store/hello.txt> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> .\n" +
                "<" + sourceURI.toString() + "> <http://purl.org/pav/hasVersion> <hash://sha256/2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> .\n" +
                "<urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> <http://www.w3.org/ns/prov#endedAtTime> \"2024-12-18T20:54:28.029Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> <urn:uuid:e4fe8c5c-1455-46c6-bcfe-f11a065978aa> .\n";

        assertThat(builder.toString(), Is.is(expected));
    }


}