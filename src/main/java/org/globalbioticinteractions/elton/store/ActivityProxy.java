package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.ActivityUtil;
import bio.guoda.preston.process.StatementListener;
import bio.guoda.preston.process.StatementsEmitterAdapter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.util.DateUtil;
import org.globalbioticinteractions.cache.ContentProvenance;
import org.globalbioticinteractions.cache.ProvenanceLog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ActivityProxy implements ActivityListener {
    private final List<ActivityListener> listeners;

    public ActivityProxy(List<ActivityListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onStarted(UUID activityId, IRI request) {

    }

    @Override
    public void onCompleted(UUID activityId, IRI request, IRI response, URI localPathOfResponseData) {
        listeners.forEach(l -> l.onCompleted(activityId, request, response, localPathOfResponseData));
    }

}
