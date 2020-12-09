package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.tool.NullImportLogger;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.StreamUtil;
import org.globalbioticinteractions.elton.util.TaxonWriter;

import java.io.PrintStream;
import java.util.stream.Stream;

@Parameters(separators = "= ", commandDescription = "List Taxa")
public class CmdNames extends CmdTabularWriterParams {

    @Override
    public void run() {
        run(System.out);
    }

    void run(PrintStream out) {
        TaxonWriter writer = createWriter(out);
        if (!shouldSkipHeader()) {
            writer.writeHeader();
        }

        DatasetRegistry registry = DatasetRegistryUtil.forCacheDirOrLocalDir(getCacheDir(), getWorkDir(), createInputStreamFactory());
        NodeFactory nodeFactory = createFactory(writer);
        CmdUtil.handleNamespaces(registry, nodeFactory, getNamespaces(), "listing taxa", getStderr(), new NullImportLogger());
    }

    private TaxonWriter createWriter(PrintStream out) {
        return new TaxonWriter() {

            @Override
            public void write(Taxon taxon, Dataset dataset) {
                Stream<String> rowStream = Stream.concat(StreamUtil.streamOf(taxon), StreamUtil.streamOf(dataset));
                String row = StreamUtil.tsvRowOf(rowStream);
                out.println(row);
            }

            @Override
            public void writeHeader() {
                out.println(StreamUtil.tsvRowOf(
                        Stream.concat(Stream.of(
                                "taxonId",
                                "taxonName",
                                "taxonRank",
                                "taxonPathIds",
                                "taxonPath",
                                "taxonPathNames"),
                                StreamUtil.datasetHeaderFields())));
            }
        };
    }

    private NodeFactoryNull createFactory(TaxonWriter writer) {
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


