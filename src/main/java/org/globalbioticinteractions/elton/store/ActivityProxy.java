package org.globalbioticinteractions.elton.store;

import org.apache.commons.rdf.api.IRI;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public class ActivityProxy implements ActivityListener {
    private final List<ActivityListener> listeners;

    public ActivityProxy(List<ActivityListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onStarted(IRI parentActivityId, IRI activityId, IRI request) {
        listeners.forEach(l -> l.onStarted(parentActivityId, activityId, request));

    }

    @Override
    public void onCompleted(IRI parentActivityId, IRI activityId, IRI request, IRI response, URI localPathOfResponseData) {
        listeners.forEach(l -> l.onCompleted(parentActivityId, activityId, request, response, localPathOfResponseData));
    }

}
