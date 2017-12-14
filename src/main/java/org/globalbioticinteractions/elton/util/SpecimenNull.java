package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Season;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Taxon;
import org.eol.globi.domain.Term;

import java.util.List;

public class SpecimenNull implements Specimen {
    public Location getSampleLocation() {
        return null;
    }

    public void ate(Specimen specimen) {
        this.interactsWith(specimen, InteractType.ATE);
    }

    public void caughtIn(Location sampleLocation) {
    }

    public Season getSeason() {
        return null;
    }

    public void caughtDuring(Season season) {
    }

    public Double getLengthInMm() {
        return null;
    }

    public void classifyAs(Taxon taxon) {
    }

    public void setLengthInMm(Double lengthInMm) {
    }

    public void setVolumeInMilliLiter(Double volumeInMm3) {
    }

    public void setStomachVolumeInMilliLiter(Double volumeInMilliLiter) {
    }

    public void interactsWith(Specimen target, InteractType type, Location centroid) {
    }

    public void interactsWith(Specimen recipientSpecimen, InteractType relType) {
        this.interactsWith(recipientSpecimen, relType, null);
    }

    public void setOriginalTaxonDescription(Taxon taxon) {
    }

    public void setLifeStage(List<Term> lifeStages) {
    }

    public void setLifeStage(Term lifeStage) {
    }

    public void setPhysiologicalState(Term physiologicalState) {
    }

    public void setBodyPart(List<Term> bodyParts) {
    }

    public void setBodyPart(Term bodyPart) {
    }

    public void setBasisOfRecord(Term basisOfRecord) {
    }

    public Term getBasisOfRecord() {
        return null;
    }

    public void setFrequencyOfOccurrence(Double frequencyOfOccurrence) {
    }

    public void setTotalCount(Integer totalCount) {
    }

    public void setTotalVolumeInMl(Double totalVolumeInMl) {
    }

    public Term getLifeStage() {
        return null;
    }

    public Term getBodyPart() {
        return null;
    }

    public void setProperty(String name, Object value) {
    }

    public void setExternalId(String externalId) {
    }

    public String getExternalId() {
        return null;
    }
}
