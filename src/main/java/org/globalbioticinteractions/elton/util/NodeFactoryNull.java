package org.globalbioticinteractions.elton.util;

import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.domain.Environment;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.RelTypes;
import org.eol.globi.domain.Season;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.domain.Term;
import org.eol.globi.domain.TermImpl;
import org.globalbioticinteractions.dataset.Dataset;
import org.eol.globi.service.TermLookupService;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NodeFactoryNull implements NodeFactory {

    public Location findLocation(Location location) {
        return location;
    }

    public Season createSeason(final String seasonName) {
        return () -> seasonName;
    }

    @Override
    public Specimen createSpecimen(Interaction interaction, Taxon taxon) throws NodeFactoryException {
        return new SpecimenNull();
    }

    @Override
    public Specimen createSpecimen(Study study, Taxon taxon) throws NodeFactoryException {
        return createSpecimen(study, taxon, RelTypes.COLLECTED, RelTypes.SUPPORTS);
    }

    @Override
    public Specimen createSpecimen(Study study, Taxon taxon, RelTypes... types) throws NodeFactoryException {
        return createSpecimen(createInteraction(study), taxon);
    }

    public Study createStudy(Study study) {
        return study;
    }

    public Study getOrCreateStudy(Study study) throws NodeFactoryException {
        return study;
    }

    @Override
    public Study findStudy(Study title) {
        return null;
    }

    public Season findSeason(String seasonName) {
        return null;
    }

    public Location getOrCreateLocation(Location location) throws NodeFactoryException {
        return location;
    }

    public void setUnixEpochProperty(Specimen specimen, Date date) throws NodeFactoryException {
    }

    public Date getUnixEpochProperty(Specimen specimen) throws NodeFactoryException {
        return null;
    }

    public List<Environment> getOrCreateEnvironments(Location location, String externalId, String name) throws NodeFactoryException {
        return Collections.emptyList();
    }

    public List<Environment> addEnvironmentToLocation(Location location, List<Term> terms) {
        return Collections.emptyList();
    }

    public Term getOrCreateBodyPart(String externalId, String name) throws NodeFactoryException {
        return new TermImpl(externalId, name);
    }

    public Term getOrCreatePhysiologicalState(String externalId, String name) throws NodeFactoryException {
        return new TermImpl(externalId, name);
    }

    public Term getOrCreateLifeStage(String externalId, String name) throws NodeFactoryException {
        return new TermImpl(externalId, name);
    }

    public TermLookupService getTermLookupService() {
        return name -> Collections.singletonList(new TermImpl(name, (String) null));
    }

    public Term getOrCreateBasisOfRecord(String externalId, String name) throws NodeFactoryException {
        return new TermImpl(externalId, name);
    }

    public Dataset getOrCreateDataset(Dataset dataset) {
        return dataset;
    }

    public Interaction createInteraction(final Study study) {
        return new Interaction() {
            @Override
            public Collection<Specimen> getParticipants() {
                return null;
            }

            @Override
            public Study getStudy() {
                return study;
            }

            @Override
            public void setExternalId(String externalId) {

            }

            @Override
            public String getExternalId() {
                return null;
            }
        };
    }

    @Override
    public void close() throws Exception {

    }
}
