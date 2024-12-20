package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class LocalPathToHashIRITest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void notInDataFolder() throws IOException {
        File dataFolder = folder.newFolder();
        LocalPathToHashIRI localPathToHashIRI = new LocalPathToHashIRI(dataFolder);

        IRI iri = localPathToHashIRI.get(RefNodeFactory.toIRI("https://example.org"));

        assertThat(iri.getIRIString(), Is.is("https://example.org"));
    }


    @Test
    public void hashInDataFolder() throws IOException {
        File dataFolder = folder.newFolder();

        String hashId = Hasher.calcHashIRI("bla", HashType.sha256).getIRIString();

        String hex = StringUtils.substring(hashId, HashType.sha256.getPrefix().length());
        File file = new File(dataFolder, hex);

        assertThat(file.getAbsolutePath(), endsWith("4df3c3f68fcc83b27e9d42c90431a72499f17875c81a599b566c9889b9696703"));

        LocalPathToHashIRI localPathToHashIRI = new LocalPathToHashIRI(dataFolder);

        IRI iri = localPathToHashIRI.get(RefNodeFactory.toIRI(file.toURI()));

        assertThat(iri.getIRIString(), Is.is("hash://sha256/4df3c3f68fcc83b27e9d42c90431a72499f17875c81a599b566c9889b9696703"));
    }

    @Test
    public void hashEmbeddedJarInDataFolder() throws IOException {
        File dataFolder = folder.newFolder();

        String hashId = Hasher.calcHashIRI("bla", HashType.sha256).getIRIString();

        String hex = StringUtils.substring(hashId, HashType.sha256.getPrefix().length());
        File file = new File(dataFolder, hex);

        assertThat(file.getAbsolutePath(), endsWith("4df3c3f68fcc83b27e9d42c90431a72499f17875c81a599b566c9889b9696703"));

        LocalPathToHashIRI localPathToHashIRI = new LocalPathToHashIRI(dataFolder);

        IRI iri = localPathToHashIRI.get(
                RefNodeFactory.toIRI(
                        "jar:" +
                                RefNodeFactory.toIRI(file.toURI()).getIRIString()
                                + "!/foo")
        );

        assertThat(iri.getIRIString(), Is.is("jar:hash://sha256/4df3c3f68fcc83b27e9d42c90431a72499f17875c81a599b566c9889b9696703!/foo"));
    }

    @Test
    public void hashEmbeddedLineInJarInDataFolder() throws IOException {
        File dataFolder = folder.newFolder();

        String hashId = Hasher.calcHashIRI("bla", HashType.sha256).getIRIString();

        String hex = StringUtils.substring(hashId, HashType.sha256.getPrefix().length());
        File file = new File(dataFolder, hex);

        assertThat(file.getAbsolutePath(), endsWith("4df3c3f68fcc83b27e9d42c90431a72499f17875c81a599b566c9889b9696703"));

        LocalPathToHashIRI localPathToHashIRI = new LocalPathToHashIRI(dataFolder);

        IRI iri = localPathToHashIRI.get(
                RefNodeFactory.toIRI(
                        "line:jar:" +
                                RefNodeFactory.toIRI(file.toURI()).getIRIString()
                                + "!/foo!/L23")
        );

        assertThat(iri.getIRIString(), Is.is("line:jar:hash://sha256/4df3c3f68fcc83b27e9d42c90431a72499f17875c81a599b566c9889b9696703!/foo!/L23"));
    }

    @Test
    public void hashNotInDataFolder() throws IOException {
        File dataFolder = folder.newFolder();
        File notDataFolder = folder.newFolder();

        String hashId = Hasher.calcHashIRI("bla", HashType.sha256).getIRIString();

        String hex = StringUtils.substring(hashId, HashType.sha256.getPrefix().length());
        File file = new File(notDataFolder, hex);

        assertThat(file.getAbsolutePath(), endsWith("4df3c3f68fcc83b27e9d42c90431a72499f17875c81a599b566c9889b9696703"));

        LocalPathToHashIRI localPathToHashIRI = new LocalPathToHashIRI(dataFolder);

        IRI provided = RefNodeFactory.toIRI(file.toURI());
        IRI iri = localPathToHashIRI.get(provided);

        assertThat(iri.getIRIString(), Is.is(provided.getIRIString()));
    }

}