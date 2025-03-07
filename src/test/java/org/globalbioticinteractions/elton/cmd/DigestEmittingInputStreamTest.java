package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.StatementsEmitter;
import bio.guoda.preston.process.StatementsEmitterAdapter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

public class DigestEmittingInputStreamTest {

    @Test
    public void incompleteJSON() throws NoSuchAlgorithmException, IOException {
        final MessageDigest md = MessageDigest.getInstance(HashType.sha256.getAlgorithm());

        String jsonWithTrailingWhitespace = "{\"foo\": \"bar\"}  ";
        List<Quad> statements = new ArrayList<>();
        DigestEmittingInputStream is = new DigestEmittingInputStream(
                IOUtils.toInputStream(jsonWithTrailingWhitespace, StandardCharsets.UTF_8),
                md,
                URI.create("http://example.org"),
                new ActivityContext() {
                    @Override
                    public IRI getActivity() {
                        return RefNodeFactory.toIRI("https://example.org/activity");
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }
                },
                new StatementsEmitterAdapter() {
                    @Override
                    public void emit(Quad quad) {
                        statements.add(quad);
                    }
                }, HashType.sha256);

        assertNotNull(new ObjectMapper().readTree(is));

        assertThat(statements.size(), Is.is(7));

        IRI expectedHash = Hasher.calcHashIRI(jsonWithTrailingWhitespace, HashType.sha256);

        assertThat(statements.get(6).getObject().ntriplesString(), Is.is(expectedHash.ntriplesString()));


    }


}