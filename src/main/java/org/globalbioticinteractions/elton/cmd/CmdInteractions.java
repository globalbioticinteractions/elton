package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Study;
import org.eol.globi.service.Dataset;
import org.globalbioticinteractions.dataset.DatasetFinderLocal;
import org.globalbioticinteractions.elton.util.DatasetProcessorForTSV;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NodeFactoryForDataset;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.SpecimenTaxonOnly;
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
        public void write(SpecimenTaxonOnly source, InteractType type, SpecimenTaxonOnly target, Study study, Dataset dataset) {
            Stream<String> interactStream = Stream.of(type.getIRI(), type.getLabel());

            Stream<String> rowStream = Stream.of(
                    StreamUtil.streamOf(source.taxon),
                    interactStream,
                    StreamUtil.streamOf(target.taxon),
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
                    SOURCE_TAXON_ID,
                    SOURCE_TAXON_NAME,
                    "sourceTaxonRank",
                    "sourceTaxonPath",
                    "sourceTaxonPathIds",
                    "sourceTaxonPathNames",
                    INTERACTION_TYPE_ID,
                    INTERACTION_TYPE_NAME,
                    TARGET_TAXON_ID,
                    TARGET_TAXON_NAME,
                    "targetTaxonRank",
                    "targetTaxonPath",
                    "targetTaxonPathIds",
                    "targetTaxonPathNames",
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

    InteractionWriter createSerializer(PrintStream out) {
        return new TsvWriter(out);
    }

    void run(PrintStream out) {
        DatasetFinderLocal finder = CmdUtil.getDatasetFinderLocal(getCacheDir());

        TsvWriter serializer = new TsvWriter(out);

        if (!shouldSkipHeader()) {
            serializer.writeHeader();
        }

        NodeFactoryNull nodeFactory = new NodeFactoryForDataset(serializer, new DatasetProcessorForTSV());

        CmdUtil.handleNamespaces(finder, nodeFactory, getNamespaces(), "scanning for interactions in");
    }
}


