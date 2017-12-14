package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;

public class SpecimenTaxonOnly extends SpecimenNull {
    final Dataset dataset;
    final Taxon taxon;
    final InteractionSerializerInterface serializer;


    public SpecimenTaxonOnly(Dataset dataset, InteractionSerializerInterface serializer, Taxon taxon) {
        this.dataset = dataset;
        this.serializer = serializer;
        this.taxon = taxon;
    }

    @Override
    public void interactsWith(Specimen target, InteractType type, Location centroid) {
        serializer.serialize(this, type, (SpecimenTaxonOnly) target, dataset);
    }

}
