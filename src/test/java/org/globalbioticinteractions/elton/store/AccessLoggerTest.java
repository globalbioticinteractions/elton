package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.RefNodeFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class AccessLoggerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testCreatProvDirIfNotExists() throws IOException {
        String provDir = folder.newFolder("prov").getAbsolutePath();

        File file = new File(provDir);
        file.delete();

        assertFalse(file.exists());

        String namespace = "some/namespace";
        new AccessLogger(namespace, provDir).onCompleted(
                UUID.randomUUID(),
                RefNodeFactory.toIRI("some:request"),
                RefNodeFactory.toIRI("some:response"),
                URI.create("foo/bar")
        );

        assertTrue(file.exists());
        assertTrue(file.isDirectory());
    }

}