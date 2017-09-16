package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.domain.Environment;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Season;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.domain.Term;
import org.eol.globi.geo.Ecoregion;
import org.eol.globi.geo.EcoregionFinder;
import org.eol.globi.geo.EcoregionFinderException;
import org.eol.globi.service.AuthorIdResolver;
import org.eol.globi.service.Dataset;
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

    public Specimen createSpecimen(Interaction interaction, Taxon taxon) throws NodeFactoryException {
        return null;
    }

    public Specimen createSpecimen(Study study, Taxon taxon) throws NodeFactoryException {
        return new SpecimenNull();
    }

    public Study createStudy(Study study) {
        return study;
    }

    public Study getOrCreateStudy(Study study) throws NodeFactoryException {
        return study;
    }

    public Study findStudy(String title) {
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
        return new Term(externalId, name);
    }

    public Term getOrCreatePhysiologicalState(String externalId, String name) throws NodeFactoryException {
        return new Term(externalId, name);
    }

    public Term getOrCreateLifeStage(String externalId, String name) throws NodeFactoryException {
        return new Term(externalId, name);
    }

    public TermLookupService getTermLookupService() {
        return name -> Collections.singletonList(new Term(name, (String) null));
    }

    public EcoregionFinder getEcoregionFinder() {
        return new EcoregionFinder() {
            public Collection<Ecoregion> findEcoregion(double lat, double lng) throws EcoregionFinderException {
                return Collections.emptyList();
            }

            public void shutdown() {
            }
        };
    }

    public AuthorIdResolver getAuthorResolver() {
        return authorURI -> "echo-[" + authorURI + "]";
    }

    public Term getOrCreateBasisOfRecord(String externalId, String name) throws NodeFactoryException {
        return new Term(externalId, name);
    }

    public Dataset getOrCreateDataset(Dataset dataset) {
        return dataset;
    }

    public Interaction createInteraction(Study study) {
        return null;
    }

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
}
