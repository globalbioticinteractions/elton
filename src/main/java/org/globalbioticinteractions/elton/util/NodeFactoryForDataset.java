package org.globalbioticinteractions.elton.util;

import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.domain.Environment;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.domain.Term;
import org.eol.globi.service.Dataset;
import org.globalbioticinteractions.elton.cmd.CmdUtil;
import org.globalbioticinteractions.elton.util.DatasetProcessor;
import org.globalbioticinteractions.elton.util.DatasetProcessorForTSV;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.SpecimenTaxonOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NodeFactoryForDataset extends NodeFactoryNull {
    private final InteractionWriter serializer;
    private Dataset dataset;
    private List<String> datasetInfo;
    private DatasetProcessor processor;

    public NodeFactoryForDataset(InteractionWriter serializer, DatasetProcessor processor) {
        this.serializer = serializer;
        this.processor = processor;
    }

    @Override
    public Dataset getOrCreateDataset(final Dataset dataset) {
        this.dataset = processor.process(dataset);
        this.datasetInfo = CmdUtil.datasetInfo(dataset);
        return super.getOrCreateDataset(dataset);
    }

    @Override
    public Specimen createSpecimen(Interaction interaction, Taxon taxon) throws NodeFactoryException {
        return new SpecimenTaxonOnly(dataset, datasetInfo, interaction.getStudy(), serializer, taxon);
    }

    @Override
    public Specimen createSpecimen(Study study, Taxon taxon) throws NodeFactoryException {
        return new SpecimenTaxonOnly(dataset, datasetInfo, study, serializer, taxon);
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
        ((SpecimenTaxonOnly)specimen).setEventDate(date);
    }

    public Date getUnixEpochProperty(Specimen specimen) throws NodeFactoryException {
        return ((SpecimenTaxonOnly)specimen).getEventDate();
    }

}
