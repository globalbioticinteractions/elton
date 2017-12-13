package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.Version;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.GitHubImporterFactory;
import org.globalbioticinteractions.dataset.DatasetFinderLocal;

import java.io.PrintStream;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Parameters(separators = "= ", commandDescription = "List Interacting Taxon Pairs For Local Datasets")
public class CmdInteractions extends CmdDefaultParams {
    private final static Log LOG = LogFactory.getLog(CmdInteractions.class);

    private class SpecimenTaxonOnly extends SpecimenNull {
        private final Dataset dataset;
        private final PrintStream out;
        private final Taxon taxon;


        public SpecimenTaxonOnly(Dataset dataset, PrintStream out, Taxon taxon) {
            this.dataset = dataset;
            this.out = out;
            this.taxon = taxon;
        }

        @Override
        public void interactsWith(Specimen target, InteractType type, Location centroid) {
            if (target instanceof SpecimenTaxonOnly) {
                Stream<String> interactStream = Stream.of(type.getIRI(), type.getLabel());

                Stream<String> rowStream = Stream.of(
                        StreamUtil.streamOf(taxon),
                        interactStream,
                        StreamUtil.streamOf(((SpecimenTaxonOnly) target).taxon),
                        StreamUtil.streamOf(dataset),
                        Stream.of(Version.getVersion())).flatMap(x -> x);
                String row = StreamUtil.tsvRowOf(rowStream);
                out.println(row);
            }
        }
    }

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
                return new SpecimenTaxonOnly(dataset, out, taxon);
            }

            @Override
            public Specimen createSpecimen(Study study, Taxon taxon) throws NodeFactoryException {
                return new SpecimenTaxonOnly(dataset, out, taxon);
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


