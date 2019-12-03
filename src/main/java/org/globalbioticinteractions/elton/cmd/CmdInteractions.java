package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Study;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetRegistry;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.DatasetProcessorForTSV;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NodeFactoryForDataset;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.SpecimenImpl;
import org.globalbioticinteractions.elton.util.StreamUtil;
import org.globalbioticinteractions.elton.util.TabularWriter;

import java.io.PrintStream;
import java.util.stream.Stream;

import static org.eol.globi.data.StudyImporterForMetaTable.EVENT_DATE;
import static org.eol.globi.data.StudyImporterForTSV.*;

@Parameters(separators = "= ", commandDescription = "List Interacting Taxon Pairs For Local Datasets")
public class CmdInteractions extends CmdTabularWriterParams {

    public class TsvWriter implements InteractionWriter, TabularWriter {
        private final PrintStream out;

        TsvWriter(PrintStream out) {
            this.out = out;
        }

        @Override
        public void write(SpecimenImpl source, InteractType type, SpecimenImpl target, Study study, Dataset dataset) {
            Stream<String> interactStream = Stream.of(type.getIRI(), type.getLabel());

            Stream<String> rowStream = Stream.of(
                    Stream.of(source.getExternalId()),
                    StreamUtil.streamOf(source.taxon),
                    StreamUtil.streamOf(source),
                    interactStream,
                    Stream.of(target.getExternalId()),
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

        @Override
        public void writeHeader() {
            out.println(StreamUtil.tsvRowOf(Stream.concat(Stream.of(
                    SOURCE_OCCURRENCE_ID,
                    SOURCE_TAXON_ID,
                    SOURCE_TAXON_NAME,
                    "sourceTaxonRank",
                    "sourceTaxonPath",
                    "sourceTaxonPathIds",
                    "sourceTaxonPathNames",
                    SOURCE_BODY_PART_ID,
                    SOURCE_BODY_PART_NAME,
                    SOURCE_LIFE_STAGE_ID,
                    SOURCE_LIFE_STAGE_NAME,
                    INTERACTION_TYPE_ID,
                    INTERACTION_TYPE_NAME,
                    TARGET_OCCURRENCE_ID,
                    TARGET_TAXON_ID,
                    TARGET_TAXON_NAME,
                    "targetTaxonRank",
                    "targetTaxonPath",
                    "targetTaxonPathIds",
                    "targetTaxonPathNames",
                    TARGET_BODY_PART_ID,
                    TARGET_BODY_PART_NAME,
                    TARGET_LIFE_STAGE_ID,
                    TARGET_LIFE_STAGE_NAME,
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

        DatasetRegistry registry = DatasetRegistryUtil.forCacheDirOrLocalDir(getCacheDir(), getWorkDir(), getTmpDir(), createInputStreamFactory());

        NodeFactoryNull nodeFactory = new NodeFactoryForDataset(writer, new DatasetProcessorForTSV());
        CmdUtil.handleNamespaces(registry, nodeFactory, getNamespaces(), "scanning for interactions in");
    }
}


