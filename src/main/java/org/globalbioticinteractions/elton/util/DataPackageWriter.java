package org.globalbioticinteractions.elton.util;

import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Study;
import org.eol.globi.util.CSVTSVUtil;
import org.eol.globi.util.DateUtil;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.util.MapDBUtil;
import org.mapdb.DB;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DataPackageWriter implements InteractionWriter {
    private final IdGenerator idGenerator;
    private final ZipOutputStream out;
    private final File tmpDir;
    private NavigableSet<String> events = null;
    private NavigableSet<String> occurrence = null;
    private NavigableSet<String> interactions = null;
    private AtomicBoolean firstRow = new AtomicBoolean(true);
    private String eventHeader = null;
    private String occurrenceHeader = null;
    private String interactionHeader = null;
    private DB db;

    public DataPackageWriter(OutputStream out, IdGenerator idGenerator, File tmpDir) {
        this.idGenerator = idGenerator;
        this.out = new ZipOutputStream(out);
        this.tmpDir = tmpDir;
    }

    @Override
    public void write(SpecimenImpl source, InteractType type, SpecimenImpl target, Study study, Dataset dataset) {

        lazyInit();

        String eventId = generateBlankNode();

        Date eventDate = source.getEventDate() == null ? source.getEventDate() : target.getEventDate();
        Location loc = source.getSampleLocation() == null ? source.getSampleLocation() : target.getSampleLocation();

        LinkedMap<String, String> event = createEvent(dataset, eventId, DateUtil.printDate(eventDate), loc);
        if (firstRow.get()) {
            eventHeader = toCSVHeader(event);
        }
        this.events.add(toCSVRow(event));

        String sourceOccurrenceId = generateBlankNode();
        Map<String, String> sourceOccurrence = createOccurrence(sourceOccurrenceId, eventId, source);
        if (firstRow.get()) {
            this.occurrenceHeader = toCSVHeader(sourceOccurrence);
        }
        this.occurrence.add(toCSVRow(sourceOccurrence));

        String targetOccurrenceId = generateBlankNode();
        Map<String, String> targetOccurrence = createOccurrence(targetOccurrenceId, eventId, target);
        this.occurrence.add(toCSVRow(targetOccurrence));

        String interactionId = generateBlankNode();

        Map<String, String> interaction = new LinkedMap<String, String>() {{
            put("organismInteractionID", interactionId);
            put("organismInteractionDescription", "");
            put("subjectOccurrenceID", sourceOccurrenceId);
            put("subjectOrganismPart", source.getBodyPart() == null ? "" : source.getBodyPart().getName());
            put("organismInteractionType", type.getLabel());
            put("organismInteractionTypeIRI", type.getIRI());
            put("organismInteractionTypeVocabulary", "");
            put("relatedOccurrenceID", targetOccurrenceId);
            put("relatedOrganismPart", target.getBodyPart() == null ? "" : target.getBodyPart().getName());
        }};

        if (firstRow.get()) {
            this.interactionHeader = toCSVHeader(interaction);
        }

        this.interactions.add(toCSVRow(interaction));

        firstRow.set(false);
    }

    private String toCSVRow(Map<String, String> valueMap) {
        return valueMap
                .values()
                .stream()
                .map(CSVTSVUtil::escapeCSV)
                .collect(Collectors.joining(","));
    }

    private String toCSVHeader(Map<String, String> valueMap) {
        return valueMap
                .keySet()
                .stream()
                .map(CSVTSVUtil::escapeCSV)
                .collect(Collectors.joining(","));
    }

    private LinkedMap<String, String> createEvent(Dataset dataset, String eventId, String eventDate, Location loc) {
        return new LinkedMap<String, String>() {
            {

                put("eventID", eventId);
                put("parentEventID", "");
                put("preferredEventName", "");
                put("eventType", "");
                put("eventTypeIRI", "");
                put("eventTypeVocabulary", "");
                put("datasetName", dataset.getNamespace());
                put("datasetID", dataset.getExternalId());
                put("fieldNumber", "");
                put("eventConductedBy", "");
                put("eventConductedByID", "");
                put("eventDate", eventDate);
                put("eventTime", "");
                put("startDayOfYear", "");
                put("endDayOfYear", "");
                put("year", "");
                put("month", "");
                put("day", "");
                put("verbatimEventDate", "");
                put("verbatimLocality", "");
                put("verbatimElevation", "");
                put("verbatimDepth", "");
                put("verbatimCoordinates", "");
                put("verbatimLatitude", "");
                put("verbatimLongitude", "");
                put("verbatimCoordinateSystem", "");
                put("verbatimSRS", "");
                put("georeferenceVerificationStatus", "");
                put("habitat", "");
                put("sampleSizeValue", "");
                put("sampleSizeUnit", "");
                put("eventEffort", "");
                put("fieldNotes", "");
                put("eventCitation", "");
                put("eventRemarks", "");
                put("locationID", "");
                put("higherGeographyID", "");
                put("higherGeography", "");
                put("continent", "");
                put("waterBody", "");
                put("islandGroup", "");
                put("island", "");
                put("country", "");
                put("countryCode", "");
                put("stateProvince", "");
                put("county", "");
                put("municipality", "");
                put("locality", loc == null ? "" : loc.getLocality());
                put("minimumElevationInMeters", "");
                put("maximumElevationInMeters", "");
                put("verticalDatum", "");
                put("minimumDepthInMeters", "");
                put("maximumDepthInMeters", "");
                put("minimumDistanceAboveSurfaceInMeters", "");
                put("maximumDistanceAboveSurfaceInMeters", "");
                put("locationRemarks", "");
                put("decimalLatitude", loc == null || loc.getLatitude() == null ? "" : Double.toString(loc.getLatitude()));
                put("decimalLongitude", loc == null || loc.getLongitude() == null ? "" : Double.toString(loc.getLongitude()));
                put("geodeticDatum", "");
                put("coordinateUncertaintyInMeters", "");
                put("coordinatePrecision", "");
                put("pointRadiusSpatialFit", "");
                put("footprintWKT", "");
                put("footprintSRS", "");
                put("footprintSpatialFit", "");
                put("georeferencedBy", "");
                put("georeferencedByID", "");
                put("georeferencedDate", "");
                put("georeferenceProtocol", "");
                put("georeferenceProtocolID", "");
                put("georeferenceSources", "");
                put("georeferenceRemarks", "");
                put("informationWithheld", "");
                put("dataGeneralizations", "");
                put("preferredSpatialRepresentation", "");
            }
        };
    }

    private Map<String, String> createOccurrence(String occurrenceId, String eventId, SpecimenImpl specimen) {
        return new LinkedMap<String, String>() {{
            put("occurrenceID", occurrenceId);
            put("eventID", eventId);
            put("surveyTargetID", "");
            put("recordedBy", "");
            put("recordedByID", "");
            put("organismQuantity", "");
            put("organismQuantityType", "");
            put("organismQuantityTypeIRI", "");
            put("organismQuantityTypeVocabulary", "");
            put("sex", specimen.getSex() == null ? "" : StringUtils.defaultIfBlank(specimen.getSex().getName(), ""));
            put("lifeStage", specimen.getLifeStage() == null ? "" : StringUtils.defaultIfBlank(specimen.getLifeStage().getName(), ""));
            put("reproductiveCondition", "");
            put("caste", "");
            put("behavior", "");
            put("vitality", "");
            put("establishmentMeans", "");
            put("degreeOfEstablishment", "");
            put("pathway", "");
            put("occurrenceStatus", "");
            put("occurrenceCitation", "");
            put("informationWithheld", "");
            put("dataGeneralizations", "");
            put("occurrenceRemarks", "");
            put("organismID", "");
            put("organismScope", "");
            put("organismScopeIRI", "");
            put("organismScopeVocabulary", "");
            put("organismName", "");
            put("organismRemarks", "");
            put("verbatimIdentification", "");
            put("taxonFormula", "");
            put("identifiedBy", "");
            put("identifiedByID", "");
            put("dateIdentified", "");
            put("identificationReferences", "");
            put("identificationVerificationStatus", "");
            put("identificationVerificationStatusIRI", "");
            put("identificationVerificationStatusVocabulary", "");
            put("identificationRemarks", "");
            put("taxonID", StringUtils.defaultIfBlank(specimen.taxon.getExternalId(), ""));
            put("kingdom", "");
            put("scientificName", StringUtils.defaultIfBlank(specimen.taxon.getName(), ""));
            put("taxonRank", StringUtils.defaultIfBlank(specimen.taxon.getRank(), ""));
            put("taxonRemarks", "");
        }};
    }

    private void lazyInit() {
        if (db == null) {
            db = MapDBUtil.tmpDB(tmpDir);
            events = db.getTreeSet("event" + UUID.randomUUID().toString());
            occurrence = db.getTreeSet("occurrence" + UUID.randomUUID().toString());
            interactions = db.getTreeSet("organism-interaction" + UUID.randomUUID().toString());
        }
    }

    private String generateBlankNode() {
        return "https://linker.bio/.well-known/genid/" + idGenerator.generate();
    }

    @Override
    public void close() throws IOException {
        out.putNextEntry(new ZipEntry("datapackage.json"));
        IOUtils.copy(
                getClass().getResourceAsStream("/org/globalbioticinteractions/elton/dwc-dp/datapackage.json"),
                out
        );
        out.putNextEntry(new ZipEntry("event.csv"));
        writeLine(eventHeader);
        writeLines(events);

        out.putNextEntry(new ZipEntry("occurrence.csv"));
        writeLine(occurrenceHeader);
        writeLines(occurrence);

        out.putNextEntry(new ZipEntry("organism-interaction.csv"));
        writeLine(interactionHeader);
        writeLines(interactions);

        out.flush();
        out.close();

        if (db != null) {
            db.close();
            events = null;
            eventHeader = null;
            occurrence = null;
            occurrenceHeader = null;
            interactions = null;
            interactionHeader = null;
        }



    }

    private void writeLines(Set<String> occurrence) throws IOException {
        for (String line : occurrence) {
            writeLine(line);
        }
    }

    private void writeLine(String line) throws IOException {
        IOUtils.write(line, out, StandardCharsets.UTF_8);
        IOUtils.write("\n", out, StandardCharsets.UTF_8);
    }
}
