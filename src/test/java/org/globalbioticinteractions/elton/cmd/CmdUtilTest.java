package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.RefNodeFactory;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.service.ResourceService;
import org.globalbioticinteractions.dataset.DatasetImpl;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;

public class CmdUtilTest {

    @Test
    public void reportDatasetAssociation() {
        DatasetImpl dataset = new DatasetImpl("foo/bar", new ResourceService() {
            @Override
            public InputStream retrieve(URI uri) throws IOException {
                return null;
            }
        }, URI.create("https://example.org/archive.zip"));
        IRI activitiyIRI = RefNodeFactory.toIRI(UUID.randomUUID());
        List<Quad> quads = CmdUtil.stateDatasetArchiveAssociations(dataset, activitiyIRI);
        assertThat(quads.size(), Is.is(2));
        assertThat(quads.get(0),
                Is.is(RefNodeFactory.toStatement(
                                activitiyIRI,
                                RefNodeFactory.toIRI("urn:lsid:globalbioticinteractions.org:foo/bar"),
                                RefNodeFactory.toIRI("http://www.w3.org/ns/prov#wasAssociatedWith"),
                                RefNodeFactory.toIRI("https://example.org/archive.zip")
                        )
                )
        );
        assertThat(quads.get(1),
                Is.is(RefNodeFactory.toStatement(
                                activitiyIRI,
                                RefNodeFactory.toIRI("https://example.org/archive.zip"),
                                RefNodeFactory.toIRI("http://purl.org/dc/elements/1.1/format"),
                                RefNodeFactory.toLiteral("application/globi")
                        )
                )
        );
    }

    @Test
    public void reportDatasetAssociationChecklistBank() {
        DatasetImpl dataset = new DatasetImpl(
                "urn:lsid:checklistbank.org:dataset:2017",
                uri -> null,
                URI.create("https://example.org/archive.zip")
        );
        IRI activitiyIRI = RefNodeFactory.toIRI(UUID.randomUUID());
        List<Quad> quads = CmdUtil.stateDatasetArchiveAssociations(dataset, activitiyIRI);
        assertThat(quads.size(), Is.is(2));
        assertThat(quads.get(0),
                Is.is(RefNodeFactory.toStatement(
                                activitiyIRI,
                                RefNodeFactory.toIRI("urn:lsid:checklistbank.org:dataset:2017"),
                                RefNodeFactory.toIRI("http://www.w3.org/ns/prov#wasAssociatedWith"),
                                RefNodeFactory.toIRI("https://example.org/archive.zip")
                        )
                )
        );
        assertThat(quads.get(1),
                Is.is(RefNodeFactory.toStatement(
                                activitiyIRI,
                                RefNodeFactory.toIRI("https://example.org/archive.zip"),
                                RefNodeFactory.toIRI("http://purl.org/dc/elements/1.1/format"),
                                RefNodeFactory.toLiteral("application/globi")
                        )
                )
        );
    }

}