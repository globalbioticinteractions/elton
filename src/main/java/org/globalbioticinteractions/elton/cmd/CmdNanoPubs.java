package org.globalbioticinteractions.elton.cmd;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.stream.Stream;

import net.trustyuri.TrustyUriException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.eol.globi.util.ExternalIdUtil;
import org.globalbioticinteractions.dataset.CitationUtil;
import org.globalbioticinteractions.dataset.DatasetFinderLocal;
import org.globalbioticinteractions.elton.util.IdGenerator;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.NodeFactoryForDataset;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.SpecimenTaxonOnly;
import org.nanopub.MalformedNanopubException;
import org.nanopub.Nanopub;
import org.nanopub.NanopubImpl;
import org.nanopub.NanopubUtils;
import org.nanopub.trusty.MakeTrustyNanopub;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;

import com.beust.jcommander.Parameters;

@Parameters(separators = "= ", commandDescription = "Generate Nanopubs Describing Interactions in Local Datasets")
public class CmdNanoPubs extends CmdInteractions {
    private final static Log LOG = LogFactory.getLog(CmdNanoPubs.class);
    private IdGenerator idGenerator = () -> UUID.randomUUID().toString().replaceAll("-", "");

    void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public class NanoPubWriter implements InteractionWriter {
        private final PrintStream out;

        NanoPubWriter(PrintStream out) {
            this.out = out;
        }

        @Override
        public void write(SpecimenTaxonOnly source, InteractType type, SpecimenTaxonOnly target, Study study, Dataset dataset, Stream<String> datasetInfo) {
            String nanoPubId = idGenerator.generate();
            String pubHeader = "@prefix np: <http://www.nanopub.org/nschema#> ." +
                    "@prefix dcterms: <http://purl.org/dc/terms/> ." +
                    "@prefix prov: <http://www.w3.org/ns/prov#> ." +
                    "@prefix pav: <http://swan.mindinformatics.org/ontologies/1.2/pav/> ." +
                    "@prefix dct: <http://purl.org/dc/terms/> ." +
                    "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." +
                    "@prefix sio: <http://semanticscience.org/resource/> ." +
                    "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." +
                    "@prefix obo: <http://purl.obolibrary.org/obo/> ." +
                    "@prefix : <http://purl.org/nanopub/temp/NanoPub_" + nanoPubId + "#> ." +
                    "\n" +
                    ":Head {" +
                    "  : a np:Nanopublication ;" +
                    "    np:hasAssertion :Assertion ;" +
                    "    np:hasProvenance :Provenance ;" +
                    "    np:hasPublicationInfo :Pubinfo ." +
                    "}\n";

            StringBuilder builder = new StringBuilder();

            String pubBody =
                    " \n" +
                            ":Assertion {" +
                            "  :Interaction a obo:GO_0044419 ;" +
                            "    obo:RO_0000057 :Organism_1 ;" +
                            "    obo:RO_0000057 :Organism_2 .";
            builder.append(pubHeader);
            builder.append(pubBody);

            builder.append("  :Organism_1 <").append(type.getIRI())
                    .append("> :Organism_2 .\n");

            appendOrganismForTaxon(builder, "1", source.taxon);
            appendOrganismForTaxon(builder, "2", target.taxon);

            String eltonURI = "https://doi.org/10.5281/zenodo.998263";

            String datasetURI = StringUtils.isNotBlank(dataset.getDOI()) ? dataset.getDOI() : dataset.getArchiveURI().toString();

            builder.append("}" +
                    " " +
                    ":Provenance {");

            String studyDoi = StringUtils.isNotBlank(study.getDOI()) ? study.getDOI() : null;
            String citationString = StringUtils.isNotBlank(study.getCitation()) ? study.getCitation() : null;
            if (studyDoi != null || citationString != null) {
            	if (studyDoi == null) {
            		builder.append("  :Assertion prov:wasDerivedFrom :Study .");
                    builder.append(rdfCitationSnippetFor(citationString, ":Study"));
            	} else {
            		String studyUrl = getDoiUrl(studyDoi);
            		builder.append("  :Assertion prov:wasDerivedFrom <" + studyUrl + "> .");
            		if (citationString != null) {
                        builder.append(rdfCitationSnippetFor(citationString, "<" + studyUrl + ">"));
            		}
            	}
            } else {
            	builder.append("  :Assertion prov:wasDerivedFrom <" + datasetURI + "> .");
            }

            builder.append("}" +
                    " " +
                    ":Pubinfo {" +
                    "  : prov:wasDerivedFrom <" + datasetURI + "> ." +
                    rdfCitationSnippetFor(CitationUtil.citationOrDefaultFor(dataset, ""), "<" + datasetURI + ">") +
                    "  : pav:createdBy <" + eltonURI + "> ." +
                    "}");
            try {
            	Nanopub preNanopub = new NanopubImpl(builder.toString(), RDFFormat.TRIG);
            	Nanopub trustyNanopub = MakeTrustyNanopub.transform(preNanopub);
            	String trustyNanopubString = NanopubUtils.writeToString(trustyNanopub, RDFFormat.TRIG);
            	out.println(trustyNanopubString.replace("\n", " "));
            } catch (OpenRDFException | MalformedNanopubException | TrustyUriException ex) {
            	throw new RuntimeException(ex);
            }
        }

        private String rdfCitationSnippetFor(String citationString, String subject) {
            String desc = StringEscapeUtils.escapeXml(citationString.replace("\n", " "));
            return "  " + subject + " dct:bibliographicCitation \"" + desc + "\" .";
        }

        private void appendOrganismForTaxon(StringBuilder builder, String number, Taxon taxon) {
            String s = ExternalIdUtil.urlForExternalId(taxon.getExternalId());
            if (StringUtils.isNotBlank(s)) {
                builder.append("  :Organism_").append(number).append(" a <").append(s).append(">   .\n");
            }
            String name = StringEscapeUtils.escapeXml(taxon.getName()).replace("\n", " ");
            if (StringUtils.isNotBlank(name)) {
                builder.append("  :Organism_").append(number).append(" rdfs:label \"").append(name).append("\" .\n");
            }
        }

        private String getDoiUrl(String doi) {
        	try {
	        	if (doi.startsWith("doi:")) {
	        		return URLEncoder.encode(doi, "UTF8").replace("%2F", "/").replace("%3A", ":").replaceFirst("^doi:", "https://doi.org/");
	        	}
	        	if (doi.startsWith("10.")) {
	        		return "https://doi.org/" + URLEncoder.encode(doi, "UTF8").replace("%2F", "/").replace("%3A", ":");
	        	}
	        	return URLEncoder.encode(doi, "UTF8").replace("%2F", "/").replace("%3A", ":");
        	} catch (UnsupportedEncodingException ex) {
        		throw new RuntimeException(ex);
        	}
        }

    }

    @Override
    public void run() {
        run(System.out);
    }

    InteractionWriter createSerializer(PrintStream out) {
        return new NanoPubWriter(out);
    }

    void run(PrintStream out) {
        DatasetFinderLocal finder = CmdUtil.getDatasetFinderLocal(getCacheDir());

        InteractionWriter serializer = createSerializer(out);

        NodeFactoryNull nodeFactory = new NodeFactoryForDataset(serializer, dataset -> dataset);

        CmdUtil.handleNamespaces(finder, nodeFactory, getNamespaces(), "generating trusty nanopubs for");
    }

}


