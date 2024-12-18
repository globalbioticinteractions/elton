package org.globalbioticinteractions.elton.store;

import org.apache.commons.rdf.api.IRI;

import java.net.URI;

public interface ActivityListener {

    void onStarted(
            IRI parentActivityId,
            IRI activityId,
            IRI request
    );

    void onCompleted(
            IRI parentActivityId,
            IRI activityId,
            IRI request,
            IRI response,
            URI localPathOfResponseData
    );
}
