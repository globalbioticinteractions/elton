package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.GitHubImporterFactory;
import org.globalbioticinteractions.dataset.DatasetFinderLocal;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.SpecimenTaxonOnly;
import org.globalbioticinteractions.elton.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

@Parameters(separators = "= ", commandDescription = "List Interacting Taxon Pairs For Local Datasets")
public class CmdInteractions extends CmdDefaultParams {
    private final static Log LOG = LogFactory.getLog(CmdInteractions.class);

    public class TsvWriter implements InteractionWriter {
        private final PrintStream out;

        TsvWriter(PrintStream out) {
            this.out = out;
        }

        @Override
        public void write(SpecimenTaxonOnly source, InteractType type, SpecimenTaxonOnly target, Study study, Dataset dataset, Stream<String> datasetInfo) {
            Stream<String> interactStream = Stream.of(type.getIRI(), type.getLabel());

            Stream<String> rowStream = Stream.of(
                    StreamUtil.streamOf(source.taxon),
                    interactStream,
                    StreamUtil.streamOf(target.taxon),
                    StreamUtil.streamOf(study),
                    datasetInfo).flatMap(x -> x);
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

        NodeFactoryNull nodeFactory = new NodeFactoryNull() {
            Dataset dataset;
            List<String> datasetInfo;

            @Override
            public Dataset getOrCreateDataset(final Dataset dataset) {
                this.dataset = new Dataset() {

                    @Override
                    public InputStream getResource(String resourceName) throws IOException {
                        return dataset.getResource(resourceName);
                    }

                    @Override
                    public URI getResourceURI(String resourceName) {
                        return dataset.getResourceURI(resourceName);
                    }

                    @Override
                    public URI getArchiveURI() {
                        return dataset.getArchiveURI();
                    }

                    @Override
                    public String getNamespace() {
                        return dataset.getNamespace();
                    }

                    @Override
                    public JsonNode getConfig() {
                        return dataset.getConfig();
                    }

                    @Override
                    public String getCitation() {
                        return dataset.getCitation();
                    }

                    @Override
                    public String getFormat() {
                        return dataset.getFormat();
                    }

                    @Override
                    public String getOrDefault(String key, String defaultValue) {
                        return null;
                    }

                    @Override
                    public String getDOI() {
                        return dataset.getDOI();
                    }

                    @Override
                    public URI getConfigURI() {
                        return null;
                    }

                    @Override
                    public void setConfig(JsonNode config) {

                    }

                    @Override
                    public void setConfigURI(URI configURI) {

                    }
                };
                this.datasetInfo = CmdUtil.datasetInfo(dataset);
                return super.getOrCreateDataset(dataset);
            }

            @Override
            public Specimen createSpecimen(Interaction interaction, Taxon taxon) throws NodeFactoryException {
                return new SpecimenTaxonOnly(dataset, datasetInfo.stream(), interaction.getStudy(), serializer, taxon);
            }

            @Override
            public Specimen createSpecimen(Study study, Taxon taxon) throws NodeFactoryException {
                return new SpecimenTaxonOnly(dataset, datasetInfo.stream(), study, serializer, taxon);
            }
        };

        try {
            CmdUtil.handleNamespaces(finder, namespace -> {
                String msg = "scanning for interactions in [" + namespace + "]...";
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


