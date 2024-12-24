package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.eol.globi.data.ImportLogger;
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
import java.util.concurrent.atomic.AtomicLong;

public class WriterUtil {

    private static NodeFactory nodeFactoryForInteractionWriting(boolean shouldWriteHeader, PrintStream stdout) {
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

    public static NodeFactory nodeFactoryForReviewWriting(boolean shouldWriteHeader,
                                                          PrintStream out,
                                                          ImportLogger importLogger) {
        if (shouldWriteHeader) {
            CmdReview.logReviewHeader(out);
        }
        return new CmdReview.NodeFactoryReview(new AtomicLong(0), importLogger);
    }

    public static NodeFactory getNodeFactoryForType(String recordType, boolean shouldWriteHeader, PrintStream out, ImportLogger logger) {
        NodeFactory factory;
        if (StringUtils.equals("interaction", recordType)) {
            factory = nodeFactoryForInteractionWriting(shouldWriteHeader, out);
        } else if (StringUtils.equals("name", recordType)) {
            factory = nodeFactoryForTaxonWriting(shouldWriteHeader, out);
        } else if (StringUtils.equals("review", recordType)) {
            factory = nodeFactoryForReviewWriting(shouldWriteHeader, out, logger);
        } else {
            throw new NotImplementedException("no node factory for [" + recordType + "] available yet.");
        }
        return factory;
    }
}
