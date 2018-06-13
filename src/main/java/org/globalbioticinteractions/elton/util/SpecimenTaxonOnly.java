package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.globalbioticinteractions.dataset.CitationUtil;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class SpecimenTaxonOnly extends SpecimenNull {
    public final Taxon taxon;
    final InteractionWriter serializer;
    private final Study study;
    private final Dataset dataset;
    private List<String> datasetInfo;
    private Location location;
    private Date eventDate;

    public SpecimenTaxonOnly(Dataset dataset, List<String> datasetInfo, Study study, InteractionWriter serializer, Taxon taxon) {
        this.study = study;
        this.serializer = serializer;
        this.taxon = taxon;
        this.datasetInfo = datasetInfo;
        this.dataset = dataset;
    }

    @Override
    public Location getSampleLocation() {
        return location;
    }

    @Override
    public void caughtIn(Location sampleLocation) {
        this.location = sampleLocation;
    }

    @Override
    public void interactsWith(Specimen recipientSpecimen, InteractType relType) {
        interactsWith(recipientSpecimen, relType, location);
    }

    @Override
    public void interactsWith(Specimen target, InteractType type, Location providedLocation) {
        if (providedLocation != null) {
            this.location = providedLocation;
            ((SpecimenTaxonOnly) target).location = providedLocation;
        }
        serializer.write(this, type, (SpecimenTaxonOnly) target, study, dataset, datasetInfo);
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Date getEventDate() {
        return this.eventDate;
    }

}
