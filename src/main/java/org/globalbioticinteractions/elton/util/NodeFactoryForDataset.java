package org.globalbioticinteractions.elton.util;

import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.domain.Environment;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.RelTypes;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.domain.Term;
import org.globalbioticinteractions.dataset.Dataset;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class NodeFactoryForDataset extends NodeFactoryNull {
    private final InteractionWriter serializer;
    private Dataset dataset;
    private DatasetProcessor processor;

    public NodeFactoryForDataset(InteractionWriter serializer, DatasetProcessor processor) {
        this.serializer = serializer;
        this.processor = processor;
    }

    @Override
    public Dataset getOrCreateDataset(final Dataset dataset) {
        this.dataset = processor.process(dataset);
        return super.getOrCreateDataset(dataset);
    }

    @Override
    public Specimen createSpecimen(Interaction interaction, Taxon taxon) throws NodeFactoryException {
        return new SpecimenImpl(dataset, interaction.getStudy(), serializer, taxon, true);
    }

    @Override
    public Specimen createSpecimen(Study study, Taxon taxon) throws NodeFactoryException {
        return new SpecimenImpl(dataset, study, serializer, taxon, true);
    }

    @Override
    public Specimen createSpecimen(Study study, Taxon taxon, RelTypes... relTypes) throws NodeFactoryException {
        boolean containsSupportingClaim = Arrays.asList(relTypes).contains(RelTypes.SUPPORTS);
        return new SpecimenImpl(dataset, study, serializer, taxon, containsSupportingClaim);
    }

    @Override
    public List<Environment> addEnvironmentToLocation(Location location, List<Term> terms) {
        for (Term term : terms) {
            final String name = term.getName();
            final String id = term.getId();
            Environment environment = new Environment() {
                @Override
                public String getName() {
                    return name;
                }

                @Override
                public void setExternalId(String externalId) {

                }

                @Override
                public String getExternalId() {
                    return id;
                }
            };
            location.addEnvironment(environment);
        }
        return location.getEnvironments();
    }

    public void setUnixEpochProperty(Specimen specimen, Date date) throws NodeFactoryException {
        ((SpecimenImpl)specimen).setEventDate(date);
    }

    public Date getUnixEpochProperty(Specimen specimen) throws NodeFactoryException {
        return ((SpecimenImpl)specimen).getEventDate();
    }

}
