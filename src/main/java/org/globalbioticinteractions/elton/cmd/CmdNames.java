package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.GitHubImporterFactory;
import org.globalbioticinteractions.dataset.DatasetFinderLocal;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.StreamUtil;

import java.io.PrintStream;
import java.util.stream.Stream;

@Parameters(separators = "= ", commandDescription = "List Dataset (Taxon) Names For Local Datasets")
public class CmdNames extends CmdDefaultParams {
    private final static Log LOG = LogFactory.getLog(CmdNames.class);

    @Override
    public void run() {
        run(System.out);

    }

    void run(PrintStream out) {
        DatasetFinderLocal finder = CmdUtil.getDatasetFinderLocal(getCacheDir());

        NodeFactoryNull nodeFactory = new NodeFactoryNull() {
            Dataset dataset;

            @Override
            public Dataset getOrCreateDataset(Dataset dataset) {
                this.dataset = dataset;
                return super.getOrCreateDataset(dataset);
            }

            @Override
            public Specimen createSpecimen(Interaction interaction, Taxon taxon) throws NodeFactoryException {
                logTaxon(taxon, out);
                return super.createSpecimen(interaction, taxon);
            }

            private void logTaxon(Taxon taxon, PrintStream out) {
                Stream<String> rowStream = Stream.concat(StreamUtil.streamOf(taxon), StreamUtil.streamOf(dataset));
                String row = StreamUtil.tsvRowOf(rowStream);
                out.println(row);
            }

            @Override
            public Specimen createSpecimen(Study study, Taxon taxon) throws NodeFactoryException {
                logTaxon(taxon, out);
                return super.createSpecimen(study, taxon);
            }
        };

        try {
            CmdUtil.handleNamespaces(finder, namespace -> {
                String msg = "scanning for names in [" + namespace + "]...";
                LOG.info(msg);
                Dataset dataset = DatasetFactory.datasetFor(namespace, finder);
                nodeFactory.getOrCreateDataset(dataset);
                new GitHubImporterFactory()
                        .createImporter(dataset, nodeFactory)
                        .importStudy();
                LOG.info(msg + "done.");
            }, getNamespaces());
        } catch (DatasetFinderException e) {
            throw new RuntimeException("failed to complete name scan", e);
        }
    }

}


