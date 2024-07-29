package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.elton.util.DatasetProcessorForTSV;
import org.globalbioticinteractions.elton.util.NodeFactoryForDataset;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.TaxonWriter;

import java.io.PrintStream;

public class WriterUtil {
    static NodeFactory nodeFactoryForInteractionWriting(boolean shouldWriteHeader, PrintStream stdout) {
        WriterInteractionsTSV writer = new WriterInteractionsTSV(stdout);
        if (shouldWriteHeader) {
            writer.writeHeader();
        }

        return new NodeFactoryForDataset(writer, new DatasetProcessorForTSV());
    }

    static NodeFactory nodeFactoryForTaxonWriting(boolean shouldWriteHeader, PrintStream out) {
        TaxonWriter writer = new WriterTaxonTSV(out);
        if (shouldWriteHeader) {
            writer.writeHeader();
        }
        return createFactory(writer);
    }

    private static NodeFactoryNull createFactory(TaxonWriter writer) {
        return new NodeFactoryNull() {
            Dataset dataset;

            @Override
            public Dataset getOrCreateDataset(Dataset dataset) {
                this.dataset = dataset;
                return super.getOrCreateDataset(dataset);
            }

            @Override
            public Specimen createSpecimen(Interaction interaction, Taxon taxon) throws NodeFactoryException {
                writer.write(taxon, dataset);
                return super.createSpecimen(interaction, taxon);
            }


            @Override
            public Specimen createSpecimen(Study study, Taxon taxon) throws NodeFactoryException {
                return super.createSpecimen(study, taxon);
            }
        };
    }
}
