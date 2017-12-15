package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;

public class SpecimenTaxonOnly extends SpecimenNull {
    public final Dataset dataset;
    public final Taxon taxon;
    final InteractionWriter serializer;
    private final Study study;


    public SpecimenTaxonOnly(Dataset dataset, InteractionWriter serializer, Taxon taxon) {
        this(dataset, null, serializer, taxon);
    }

    public SpecimenTaxonOnly(Dataset dataset, Study study, InteractionWriter serializer, Taxon taxon) {
        this.dataset = dataset;
        this.study = study;
        this.serializer = serializer;
        this.taxon = taxon;
    }

    @Override
    public void interactsWith(Specimen target, InteractType type, Location centroid) {
        serializer.write(this, type, (SpecimenTaxonOnly) target, dataset, study);
    }

}
