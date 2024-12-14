package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.ActivityUtil;
import bio.guoda.preston.process.StatementListener;
import bio.guoda.preston.process.StatementsEmitterAdapter;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public class ProvLogger implements ActivityListener {

    private StatementListener listener;

    public ProvLogger(StatementListener listener) {
        this.listener = listener;
    }

    @Override
    public void onStarted(UUID activityId, IRI request) {

    }

    @Override
    public void onCompleted(UUID activityId, IRI request, IRI response, URI localPathOfResponseData) {
        if (listener != null) {
            ActivityUtil.emitDownloadActivity(
                    request,
                    response,
                    new StatementsEmitterAdapter() {
                        @Override
                        public void emit(Quad quad) {
                            listener.on(quad);
                        }
                    },
                    Optional.of(RefNodeFactory.toIRI(activityId))
            );
        }
    }
}
