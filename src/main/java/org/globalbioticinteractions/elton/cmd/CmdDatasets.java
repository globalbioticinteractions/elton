package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Study;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFinder;
import org.globalbioticinteractions.elton.util.DatasetFinderUtil;
import org.globalbioticinteractions.elton.util.DatasetProcessor;
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
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_LIFE_STAGE_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_LIFE_STAGE_NAME;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_OCCURRENCE_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_TAXON_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_TAXON_NAME;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_BODY_PART_ID;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_BODY_PART_NAME;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_LIFE_STAGE_ID;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_LIFE_STAGE_NAME;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_OCCURRENCE_ID;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_TAXON_ID;
import static org.eol.globi.data.StudyImporterForTSV.TARGET_TAXON_NAME;

@Parameters(separators = "= ", commandDescription = "List Info For Local Datasets")
public class CmdDatasets extends CmdTabularWriterParams {

    public class TsvDatasetWriter implements TabularWriter {
        private final PrintStream out;

        TsvDatasetWriter(PrintStream out) {
            this.out = out;
        }

        public void write(Dataset dataset) {
            String row = StreamUtil.tsvRowOf(CmdUtil.datasetInfo(dataset).stream());
            out.println(row);
        }

        @Override
        public void writeHeader() {
            out.println(StreamUtil.tsvRowOf(StreamUtil.datasetHeaderFields()));
        }
    }

    @Override
    public void run() {
        run(System.out);
    }

    void run(PrintStream out) {
        TsvDatasetWriter serializer = new TsvDatasetWriter(out);
        if (!shouldSkipHeader()) {
            serializer.writeHeader();
        }

        DatasetFinder finder = DatasetFinderUtil.forCacheDirOrLocalDir(getCacheDir(), getWorkDir());


        final DatasetProcessor proxied = new DatasetProcessorForTSV();
        DatasetProcessor datasetProcessor = dataset -> {
            serializer.write(dataset);
            return proxied.process(dataset);
        };

        NodeFactory nodeFactory = new NodeFactoryNull() {
            @Override
            public Dataset getOrCreateDataset(Dataset dataset) {
                return datasetProcessor.process(dataset);
            }
        };

        CmdUtil.handleNamespaces(finder, nodeFactory, getNamespaces(), "scanning for datasets in");
    }
}


