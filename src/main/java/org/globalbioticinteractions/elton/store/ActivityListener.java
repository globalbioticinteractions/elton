package org.globalbioticinteractions.elton.store;

import org.apache.commons.rdf.api.IRI;

import java.net.URI;
import java.util.UUID;

public interface ActivityListener {
    void onStarted(UUID activityId, IRI request);

    void onCompleted(UUID activityId, IRI request, IRI response, URI localPathOfResponseData);
}
