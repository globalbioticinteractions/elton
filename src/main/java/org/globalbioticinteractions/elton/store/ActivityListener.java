package org.globalbioticinteractions.elton.store;

import org.apache.commons.rdf.api.IRI;

import java.net.URI;
import java.util.UUID;

public interface ActivityListener {

    void onStarted(
            IRI parentActivityId,
            UUID activityId,
            IRI request
    );

    void onCompleted(
            IRI parentActivityId,
            UUID activityId,
            IRI request,
            IRI response,
            URI localPathOfResponseData
    );
}
