package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.domain.Term;
import org.globalbioticinteractions.dataset.Dataset;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecimenImpl extends SpecimenNull {
    public final Taxon taxon;
    final InteractionWriter serializer;
    private final Study study;
    private final Dataset dataset;
    private Location location;
    private Date eventDate;
    private Term bodyPart;
    private Term lifeStage;
    private String externalId;
    private Term basisOfRecord;
    private Term sex;
    private Map<String, String> properties;
    private boolean supportingClaim;

    public SpecimenImpl(Dataset dataset, Study study, InteractionWriter serializer, Taxon taxon) {
        this(dataset, study, serializer, taxon, true);
    }

    public SpecimenImpl(Dataset dataset, Study study, InteractionWriter serializer, Taxon taxon, boolean supportingClaim) {
        this.study = study;
        this.serializer = serializer;
        this.taxon = taxon;
        this.dataset = dataset;
        this.supportingClaim = supportingClaim;
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
            ((SpecimenImpl) target).location = providedLocation;
        }
        if (eventDate != null) {
            ((SpecimenImpl) target).setEventDate(eventDate);
        }
        serializer.write(this, type, (SpecimenImpl) target, study, dataset);
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Date getEventDate() {
        return this.eventDate;
    }

    @Override
    public void setLifeStage(List<Term> lifeStages) {
        if (lifeStages != null && lifeStages.size() > 0) {
            setLifeStage(lifeStages.get(0));
        }
    }

    @Override
    public void setLifeStage(Term lifeStage) {
        this.lifeStage = lifeStage;
    }

    @Override
    public void setBodyPart(List<Term> bodyParts) {
        if (bodyParts != null && bodyParts.size() > 0) {
            setBodyPart(bodyParts.get(0));
        }
    }

    @Override
    public void setBodyPart(Term bodyPart) {
        this.bodyPart = bodyPart;
    }

    @Override
    public void setBasisOfRecord(Term basisOfRecord) {
        this.basisOfRecord = basisOfRecord;
    }

    @Override
    public Term getBasisOfRecord() {
        return basisOfRecord;
    }

    @Override
    public Term getLifeStage() {
        return lifeStage;
    }

    @Override
    public Term getBodyPart() {
        return bodyPart;
    }

    @Override
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    public String getExternalId() {
        return externalId;
    }

    @Override
    public Term getSex() {
        return sex;
    }

    @Override
    public void setSex(Term sex) {
        this.sex = sex;
    }

    @Override
    public void setProperty(String name, String value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }

    @Override
    public String getProperty(String name) {
        return properties == null
                ? null
                : properties.get(name);
    }



    public boolean isSupportingClaim() {
        return supportingClaim;
    }
}
