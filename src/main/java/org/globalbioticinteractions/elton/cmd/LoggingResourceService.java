package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.ActivityUtil;
import bio.guoda.preston.process.StatementsEmitter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.service.ResourceService;
import org.eol.globi.util.ResourceUtil;
import org.globalbioticinteractions.cache.CacheUtil;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetFinderUtil;
import org.globalbioticinteractions.elton.Elton;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class LoggingResourceService implements ResourceService {
    private final ActivityContext ctx;
    private final StatementsEmitter activityEmitter;
    private HashType hashType;
    private final ResourceService local;
    private final AtomicReference<IRI> archiveContentId = new AtomicReference<>(null);

    public LoggingResourceService(ResourceService resourceService,
                                  HashType hashType,
                                  ActivityContext ctx,
                                  StatementsEmitter emitter) {
        this.local = resourceService;
        this.hashType = hashType;
        this.ctx = ctx;
        this.activityEmitter = new StatementsEmitter() {
            @Override
            public void emit(List<Quad> statement) {
                emitter.emit(statement);
            }

            @Override
            public void emit(Quad statement) {
                emitter.emit(statement);
            }
        };
    }

    @Override
    public InputStream retrieve(URI uri) throws IOException {
        return logVersion(uri, this.local.retrieve(uri));
    }

    private InputStream logVersion(URI uri, InputStream retrieve) throws IOException {
        try {
            final MessageDigest md = MessageDigest.getInstance(hashType.getAlgorithm());
            final URI resource = local instanceof Dataset
                    ? getLocationInDataset(uri, (Dataset) local) : uri;

            return new DigestLoggingInputStream(retrieve, md, resource);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("algorithm [" + hashType.getAlgorithm() + "] not supported", e);
        }
    }

    private URI getLocationInDataset(URI uri, Dataset dataset) throws IOException {
        URI archiveURI = dataset.getArchiveURI();
        URI resourceLocation = uri;
        if (!uri.isAbsolute()) {
            if (CacheUtil.isLocalDir(archiveURI)) {
                resourceLocation = ResourceUtil.getAbsoluteResourceURI(archiveURI, uri);
            } else {
                if (this.archiveContentId.get() == null) {
                    IRI archiveContentId = Hasher.calcHashIRI(
                            local.retrieve(archiveURI),
                            NullOutputStream.NULL_OUTPUT_STREAM,
                            hashType
                    );
                    this.archiveContentId.set(archiveContentId);
                    ActivityUtil.emitDownloadActivity(
                            RefNodeFactory.toIRI(archiveURI),
                            archiveContentId,
                            activityEmitter,
                            Optional.of(ctx.getActivity()));
                }

                String localDatasetRoot = DatasetFinderUtil.getLocalDatasetURIRoot(local.retrieve(archiveURI));

                URI localArchiveRoot = URI.create("zip:" + archiveContentId.get().getIRIString() + "!/" + localDatasetRoot);
                resourceLocation = ResourceUtil.getAbsoluteResourceURI(localArchiveRoot, uri);
            }
        }
        return resourceLocation;
    }

    public class DigestLoggingInputStream extends DigestInputStream {
        final AtomicBoolean isEOF;
        final AtomicBoolean hasLogged;
        final URI resourceLocation;
        private final MessageDigest md;
        private final URI resource;

        public DigestLoggingInputStream(InputStream retrieve, MessageDigest md, URI resource) {
            super(retrieve, md);
            this.md = md;
            this.resource = resource;
            isEOF = new AtomicBoolean(false);
            hasLogged = new AtomicBoolean(false);
            resourceLocation = resource;
        }

        public int read() throws IOException {
            return setEOFIfEncountered(super.read());
        }

        public int read(byte[] var1, int var2, int var3) throws IOException {
            return setEOFIfEncountered(super.read(var1, var2, var3));
        }

        private int setEOFIfEncountered(int read) {
            if (read == -1) {
                isEOF.set(true);
            }
            return read;
        }

        public void close() throws IOException {
            this.in.close();

            if (isEOF.get() && !hasLogged.get()) {
                IRI object = Hasher.toHashIRI(md, hashType);
                ActivityUtil.emitDownloadActivity(
                        RefNodeFactory.toIRI(resourceLocation),
                        object,
                        activityEmitter,
                        Optional.of(ctx.getActivity()));
                hasLogged.set(true);
            }
        }

    }

}
