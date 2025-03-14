package org.globalbioticinteractions.elton.store;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.util.DateUtil;
import org.globalbioticinteractions.cache.ContentProvenance;
import org.globalbioticinteractions.cache.ProvenanceLog;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public class AccessLogger implements ActivityListener {

    private String namespace;
    private String provDir;

    public AccessLogger(String namespace, String provDir) {
        this.namespace = namespace;
        this.provDir = provDir;
    }

    @Override
    public void onStarted(IRI parentActivityId, IRI activityId, IRI request) {

    }

    @Override
    public void onCompleted(IRI parentActivityId, IRI activityId, IRI request, IRI response, URI localPathOfResponseData) {
        if (request != null && response != null && !request.equals(response)) {
            try {
                ContentProvenance contentProvenanceWithNamespace
                        = new ContentProvenance(this.namespace,
                        URI.create(request.getIRIString()),
                        localPathOfResponseData,
                        HashCalculatorImpl.getHexPartIfAvailable(response.getIRIString()),
                        DateUtil.nowDateString()
                );

                ProvenanceLog.appendProvenanceLog(
                        new File(provDir),
                        contentProvenanceWithNamespace
                );
            } catch (IOException e) {
                throw new RuntimeException("failed to record provenance", e);
            }
        }
    }
}
