package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.lang.StringUtils;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.PropertyAndValueDictionary;
import org.eol.globi.domain.Study;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetRegistry;
import org.globalbioticinteractions.elton.util.DatasetProcessorForTSV;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NodeFactoryForDataset;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.SpecimenImpl;
import org.globalbioticinteractions.elton.util.StreamUtil;
import org.globalbioticinteractions.elton.util.TabularWriter;

import java.io.PrintStream;
import java.util.stream.Stream;

import static org.eol.globi.data.StudyImporterForMetaTable.EVENT_DATE;
import static org.eol.globi.data.StudyImporterForTSV.ARGUMENT_TYPE_ID;
import static org.eol.globi.data.StudyImporterForTSV.BASIS_OF_RECORD_ID;
import static org.eol.globi.data.StudyImporterForTSV.BASIS_OF_RECORD_NAME;
import static org.eol.globi.data.StudyImporterForTSV.DECIMAL_LATITUDE;
import static org.eol.globi.data.StudyImporterForTSV.DECIMAL_LONGITUDE;
import static org.eol.globi.data.StudyImporterForTSV.INTERACTION_TYPE_ID;
import static org.eol.globi.data.StudyImporterForTSV.INTERACTION_TYPE_NAME;
import static org.eol.globi.data.StudyImporterForTSV.LOCALITY_ID;
import static org.eol.globi.data.StudyImporterForTSV.LOCALITY_NAME;
import static org.eol.globi.data.StudyImporterForTSV.REFERENCE_CITATION;
import static org.eol.globi.data.StudyImporterForTSV.REFERENCE_DOI;
import static org.eol.globi.data.StudyImporterForTSV.REFERENCE_URL;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_BODY_PART_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_BODY_PART_NAME;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_CATALOG_NUMBER;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_COLLECTION_CODE;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_INSTITUTION_CODE;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_LIFE_STAGE_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_LIFE_STAGE_NAME;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_OCCURRENCE_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_SEX_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_SEX_NAME;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_BODY_PART_ID;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_BODY_PART_NAME;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_CATALOG_NUMBER;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_COLLECTION_CODE;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_INSTITUTION_CODE;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_LIFE_STAGE_ID;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_LIFE_STAGE_NAME;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_OCCURRENCE_ID;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_SEX_ID;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_SEX_NAME;
import static org.eol.globi.domain.PropertyAndValueDictionary.CATALOG_NUMBER;
import static org.eol.globi.domain.PropertyAndValueDictionary.COLLECTION_CODE;
import static org.eol.globi.domain.PropertyAndValueDictionary.INSTITUTION_CODE;
import static org.eol.globi.domain.PropertyAndValueDictionary.OCCURRENCE_ID;
import static org.eol.globi.service.TaxonUtil.SOURCE_TAXON_ID;
import static org.eol.globi.service.TaxonUtil.SOURCE_TAXON_NAME;
import static org.eol.globi.service.TaxonUtil.SOURCE_TAXON_PATH;
import static org.eol.globi.service.TaxonUtil.SOURCE_TAXON_PATH_NAMES;
import static org.eol.globi.service.TaxonUtil.TARGET_TAXON_ID;
import static org.eol.globi.service.TaxonUtil.TARGET_TAXON_NAME;
import static org.eol.globi.service.TaxonUtil.TARGET_TAXON_PATH;
import static org.eol.globi.service.TaxonUtil.TARGET_TAXON_PATH_NAMES;

@Parameters(separators = "= ", commandDescription = "List Interactions")
public class CmdInteractions extends CmdTabularWriterParams {

    public class TsvWriter implements InteractionWriter, TabularWriter {
        private final PrintStream out;

        TsvWriter(PrintStream out) {
            this.out = out;
        }

        @Override
        public void write(SpecimenImpl source, InteractType type, SpecimenImpl target, Study study, Dataset dataset) {
            Stream<String> interactStream = Stream.of(type.getIRI(), type.getLabel());

            String sourceOccurrenceId = valueOrEmpty(source, OCCURRENCE_ID);
            String sourceCatalogNumber = valueOrEmpty(source, CATALOG_NUMBER);
            String sourceCollectionCode = valueOrEmpty(source, COLLECTION_CODE);
            String sourceInstitutionCode = valueOrEmpty(source, INSTITUTION_CODE);

            String targetOccurrenceId = valueOrEmpty(target, OCCURRENCE_ID);
            String targetCatalogNumber = valueOrEmpty(target, CATALOG_NUMBER);
            String targetCollectionCode = valueOrEmpty(target, COLLECTION_CODE);
            String targetInstitutionCode = valueOrEmpty(target, INSTITUTION_CODE);

            Stream<String> rowStream = Stream.of(
                    Stream.of(source.isSupportingClaim() ? PropertyAndValueDictionary.SUPPORTS : PropertyAndValueDictionary.REFUTES),
                    Stream.of(sourceOccurrenceId, sourceCatalogNumber, sourceCollectionCode, sourceInstitutionCode),
                    StreamUtil.streamOf(source.taxon),
                    StreamUtil.streamOf(source),
                    interactStream,
                    Stream.of(targetOccurrenceId, targetCatalogNumber, targetCollectionCode, targetInstitutionCode),
                    StreamUtil.streamOf(target.taxon),
                    StreamUtil.streamOf(target),
                    StreamUtil.streamOf(target.getBasisOfRecord()),
                    StreamUtil.streamOf(target.getEventDate()),
                    StreamUtil.streamOf(target.getSampleLocation()),
                    StreamUtil.streamOf(study),
                    CmdUtil.datasetInfo(dataset).stream()).flatMap(x -> x);
            String row = StreamUtil.tsvRowOf(rowStream);
            out.println(row);
        }

        private String valueOrEmpty(SpecimenImpl source, String key) {
            String value = source.getProperty(key);
            return StringUtils.isBlank(value) ? "" : value;
        }

        @Override
        public void writeHeader() {
            out.println(StreamUtil.tsvRowOf(Stream.concat(Stream.of(
                    ARGUMENT_TYPE_ID,
                    SOURCE_OCCURRENCE_ID,
                    SOURCE_CATALOG_NUMBER,
                    SOURCE_COLLECTION_CODE,
                    SOURCE_INSTITUTION_CODE,
                    SOURCE_TAXON_ID,
                    SOURCE_TAXON_NAME,
                    "sourceTaxonRank",
                    "sourceTaxonPathIds",
                    SOURCE_TAXON_PATH,
                    SOURCE_TAXON_PATH_NAMES,
                    SOURCE_BODY_PART_ID,
                    SOURCE_BODY_PART_NAME,
                    SOURCE_LIFE_STAGE_ID,
                    SOURCE_LIFE_STAGE_NAME,
                    SOURCE_SEX_ID,
                    SOURCE_SEX_NAME,
                    INTERACTION_TYPE_ID,
                    INTERACTION_TYPE_NAME,
                    TARGET_OCCURRENCE_ID,
                    TARGET_CATALOG_NUMBER,
                    TARGET_COLLECTION_CODE,
                    TARGET_INSTITUTION_CODE,
                    TARGET_TAXON_ID,
                    TARGET_TAXON_NAME,
                    "targetTaxonRank",
                    "targetTaxonPathIds",
                    TARGET_TAXON_PATH,
                    TARGET_TAXON_PATH_NAMES,
                    TARGET_BODY_PART_ID,
                    TARGET_BODY_PART_NAME,
                    TARGET_LIFE_STAGE_ID,
                    TARGET_LIFE_STAGE_NAME,
                    TARGET_SEX_ID,
                    TARGET_SEX_NAME,
                    BASIS_OF_RECORD_ID,
                    BASIS_OF_RECORD_NAME,
                    EVENT_DATE,
                    DECIMAL_LATITUDE,
                    DECIMAL_LONGITUDE,
                    LOCALITY_ID,
                    LOCALITY_NAME,
                    REFERENCE_DOI,
                    REFERENCE_URL,
                    REFERENCE_CITATION

            ), StreamUtil.datasetHeaderFields())));
        }
    }

    @Override
    public void run() {
        run(System.out);
    }

    void run(PrintStream out) {
        TsvWriter writer = new TsvWriter(out);
        if (!shouldSkipHeader()) {
            writer.writeHeader();
        }

        DatasetRegistry registry = DatasetRegistryUtil.forCacheDirOrLocalDir(
                getCacheDir(),
                getWorkDir(),
                getTmpDir(),
                createInputStreamFactory());

        NodeFactoryNull nodeFactory = new NodeFactoryForDataset(writer, new DatasetProcessorForTSV());
        CmdUtil.handleNamespaces(registry, nodeFactory, getNamespaces(), "listing interactions", getStderr());
    }
}


