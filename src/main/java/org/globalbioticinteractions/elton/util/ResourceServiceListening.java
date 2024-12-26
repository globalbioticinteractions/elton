package org.globalbioticinteractions.elton.util;

import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.store.LocalPathToHashIRI;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Supplier;

public class ResourceServiceListening implements ResourceService {
    private final Supplier<IRI> activityIdFactory;
    private final ActivityListener activityListener;
    private final ActivityContext ctx;
    private final ResourceService service;
    private final LocalPathToHashIRI localPathToHashIRI;

    public ResourceServiceListening(
            Supplier<IRI> activityIdFactory,
            ActivityListener activityListener,
            ActivityContext ctx,
            ResourceService service,
            LocalPathToHashIRI localPathToHashIRI
    ) {
        this.activityIdFactory = activityIdFactory;
        this.activityListener = activityListener;
        this.ctx = ctx;
        this.service = service;
        this.localPathToHashIRI = localPathToHashIRI;
    }

    @Override
    public InputStream retrieve(URI uri) throws IOException {

        IRI accessId = activityIdFactory.get();
        activityListener.onStarted(ctx.getActivity(), accessId, RefNodeFactory.toIRI(uri));
        InputStream is = service.retrieve(uri);

        return new ProxyInputStream(is) {
            @Override
            public void close() throws IOException {
                IRI request = RefNodeFactory.toIRI(uri);
                IRI response = RefNodeFactory.toIRI(uri);
                activityListener.onCompleted(
                        ctx.getActivity(),
                        accessId,
                        localPathToHashIRI.get(request),
                        localPathToHashIRI.get(response),
                        null
                );
                IOUtils.close(in, this::handleIOException);
            }
        };
    }
}
