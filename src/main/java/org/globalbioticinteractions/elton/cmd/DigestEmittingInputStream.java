package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.ActivityUtil;
import bio.guoda.preston.process.StatementsEmitter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.rdf.api.IRI;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class DigestEmittingInputStream extends DigestInputStream {
    private final AtomicBoolean hasLogged;
    private final URI resourceLocation;
    private final MessageDigest md;
    private final ActivityContext ctx;
    private final StatementsEmitter activityEmitter;
    private final HashType hashType;

    public DigestEmittingInputStream(InputStream retrieve,
                                     MessageDigest md,
                                     URI resource,
                                     ActivityContext ctx,
                                     StatementsEmitter activityEmitter,
                                     HashType hashType) {
        super(retrieve, md);
        this.md = md;
        hasLogged = new AtomicBoolean(false);
        resourceLocation = resource;
        this.ctx = ctx;
        this.activityEmitter = activityEmitter;
        this.hashType = hashType;
    }

    public void close() throws IOException {
        this.in.close();

        if (!hasLogged.get()) {
            IRI object = Hasher.toHashIRI(this.md, this.hashType);
            ActivityUtil.emitDownloadActivity(
                    RefNodeFactory.toIRI(this.resourceLocation),
                    object,
                    this.activityEmitter,
                    Optional.of(this.ctx.getActivity()));
            hasLogged.set(true);
        }
    }

}
