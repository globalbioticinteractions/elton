package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import java.io.PrintStream;
import java.util.List;
import java.util.stream.Stream;

@Parameters(separators = "= ", commandDescription = "List Interacting Taxon Pairs For Local Datasets")
public class CmdInteractions extends CmdDefaultParams {
    public class TsvWriter implements InteractionWriter {
        private final PrintStream out;

        TsvWriter(PrintStream out) {
            this.out = out;
        }

        @Override
        public void write(SpecimenTaxonOnly source, InteractType type, SpecimenTaxonOnly target, Study study, Dataset dataset, List<String> datasetInfo) {
            Stream<String> interactStream = Stream.of(type.getIRI(), type.getLabel());

            Stream<String> rowStream = Stream.of(
                    StreamUtil.streamOf(source.taxon),
                    interactStream,
                    StreamUtil.streamOf(target.taxon),
                    StreamUtil.streamOf(target.getEventDate()),
                    StreamUtil.streamOf(target.getSampleLocation()),
                    StreamUtil.streamOf(study),
                    datasetInfo.stream()).flatMap(x -> x);
            String row = StreamUtil.tsvRowOf(rowStream);
            out.println(row);
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

        InteractionWriter serializer = createSerializer(out);

        NodeFactoryNull nodeFactory = new NodeFactoryForDataset(serializer, new DatasetProcessorForTSV());

        CmdUtil.handleNamespaces(finder, nodeFactory, getNamespaces(), "scanning for interactions in");
    }
}


