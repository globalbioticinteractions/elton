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
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
    public void hashEmbeddedJarInClasspath() throws IOException {
        File dataFolder = folder.newFolder();

        String hashId = Hasher.calcHashIRI("hello", HashType.sha256).getIRIString();

        String hex = StringUtils.substring(hashId, HashType.sha256.getPrefix().length());
        File file = new File(dataFolder, hex);

        assertThat(file.getAbsolutePath(), endsWith("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));

        LocalPathToHashIRI localPathToHashIRI = new LocalPathToHashIRI(dataFolder);

        String stringClassPath = "classpath:/java/lang/String.class";
        IRI iri = localPathToHashIRI.get(
                RefNodeFactory.toIRI(
                        stringClassPath)
        );

        assertThat(iri.getIRIString(), startsWith("jar:file:"));
        assertThat(iri.getIRIString(), endsWith("!/java/lang/String.class"));
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