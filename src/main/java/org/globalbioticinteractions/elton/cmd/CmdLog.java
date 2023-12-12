package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import org.apache.commons.io.output.NullAppendable;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.service.ResourceService;
import org.eol.globi.tool.NullImportLogger;
import org.eol.globi.util.ResourceUtil;
import org.globalbioticinteractions.cache.CacheUtil;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetFinderUtil;
import org.globalbioticinteractions.dataset.DatasetProxy;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistryProxy;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@CommandLine.Command(
        name = "log",
        aliases = {"prov"},
        description = "lists provenance of original resources"
)
public class CmdLog extends CmdDefaultParams {

    @CommandLine.Option(
            names = {"--hash-algorithm", "--algo", "-a"},
            description = "Hash algorithm used to generate primary content identifiers. Supported values: ${COMPLETION-CANDIDATES}."
    )
    private HashType hashType = HashType.sha256;


    @Override
    public void run() {
        run(System.out);
    }

    void run(PrintStream out) {
        DatasetRegistry registry = DatasetRegistryUtil.forCacheDirOrLocalDir(
                getCacheDir(),
                getWorkDir(),
                createInputStreamFactory()
        );

        DatasetRegistry proxy = new DatasetRegistryProxy(Collections.singletonList(registry)) {
            public Dataset datasetFor(String namespace) throws DatasetRegistryException {
                Dataset dataset = super.datasetFor(namespace);
                return new DatasetProxy(dataset) {
                    ResourceService service = new LoggingResourceService(out, dataset, hashType);

                    public InputStream retrieve(URI resourcePath) throws IOException {
                        return service.retrieve(resourcePath);
                    }
                };
            }
        };

        NodeFactory nodeFactory = new NodeFactoryNull();
        CmdUtil.handleNamespaces(
                proxy,
                nodeFactory,
                getNamespaces(),
                "logging provenance",
                NullAppendable.INSTANCE,
                new NullImportLogger());
    }

    private static class LoggingResourceService implements ResourceService {
        private final PrintStream out;
        private HashType hashType;
        private final ResourceService local;
        private final AtomicReference<IRI> archiveContentId = new AtomicReference<>(null);

        public LoggingResourceService(PrintStream out, ResourceService resourceService, HashType hashType) {
            this.out = out;
            this.local = resourceService;
            this.hashType = hashType;
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
                throw new RuntimeException("cannot calculate sha256 hashes", e);
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
                        Quad quad = RefNodeFactory.toStatement(
                                RefNodeFactory.toIRI(archiveURI),
                                RefNodeConstants.HAS_VERSION,
                                archiveContentId
                        );
                        out.println(quad.toString());
                    }

                    String localDatasetRoot = DatasetFinderUtil.getLocalDatasetURIRoot(local.retrieve(archiveURI));

                    URI localArchiveRoot = URI.create("zip:" + archiveContentId.get().getIRIString() + "!/" + localDatasetRoot);
                    resourceLocation = ResourceUtil.getAbsoluteResourceURI(localArchiveRoot, uri);
                }
            }
            return resourceLocation;
        }

        private class DigestLoggingInputStream extends DigestInputStream {
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
                    Quad quad = RefNodeFactory.toStatement(
                            RefNodeFactory.toIRI(resourceLocation),
                            RefNodeConstants.HAS_VERSION,
                            Hasher.toHashIRI(md, hashType)
                    );
                    out.println(quad.toString());
                    hasLogged.set(true);
                }
            }
        }
    }

    public void setHashType(HashType hashType) {
        this.hashType = hashType;
    }

}


