package org.globalbioticinteractions.elton.util;

import net.trustyuri.TrustyUriException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eol.globi.domain.Environment;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.eol.globi.util.DateUtil;
import org.eol.globi.util.ExternalIdUtil;
import org.globalbioticinteractions.dataset.CitationUtil;
import org.globalbioticinteractions.doi.DOI;
import org.nanopub.MalformedNanopubException;
import org.nanopub.Nanopub;
import org.nanopub.NanopubImpl;
import org.nanopub.NanopubUtils;
import org.nanopub.trusty.MakeTrustyNanopub;
import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class NanoPubWriter implements InteractionWriter {
    private final IdGenerator idGenerator;
    private final PrintStream out;

    public NanoPubWriter(PrintStream out, IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        this.out = out;
    }

    @Override
    public void write(SpecimenTaxonOnly source, InteractType type, SpecimenTaxonOnly target, Study study, Dataset dataset, List<String> datasetInfo) {
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
                "@prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> ." +
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

        if (target.getSampleLocation() != null) {
            String locId = ExternalIdUtil.urlForExternalId(target.getSampleLocation().getLocalityId());
            if (StringUtils.isNotBlank(locId)) {
                builder.append("  :Interaction obo:BFO_0000066 <").append(locId).append("> .\n");
            }
            for (Environment env : target.getSampleLocation().getEnvironments()) {
                String envoId = ExternalIdUtil.urlForExternalId(env.getExternalId());
                if (StringUtils.isNotBlank(envoId)) {
                    builder.append("  :Interaction obo:BFO_0000066 <").append(envoId).append("> .\n");
                }
            }

            if (target.getSampleLocation().getLatitude() != null) {
                builder.append("  :Interaction  geo:latitude \"").append(target.getSampleLocation().getLatitude()).append("\"^^xsd:decimal . \n");
            }

            if (target.getSampleLocation().getLongitude() != null) {
                builder.append("  :Interaction  geo:longitude \"").append(target.getSampleLocation().getLongitude()).append("\"^^xsd:decimal . \n");
            }
        }

        if (target.getEventDate() != null) {
            builder.append("  :Interaction  prov:atTime \"").append(DateUtil.printDate(target.getEventDate())).append("\"^^xsd:dateTime . \n");
        }

        String eltonURI = "https://doi.org/10.5281/zenodo.998263";

        String datasetURI = extractDatasetURI(dataset);

        builder.append("}" +
                " " +
                ":Provenance {");

        String studyDoi = study.getDOI() != null ? study.getDOI().toURI().toString() : null;
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


        builder.append("} :Pubinfo { : prov:wasDerivedFrom <")
                .append(datasetURI).append("> .")
                .append(rdfCitationSnippetFor(CitationUtil.citationOrDefaultFor(dataset, ""), "<" + datasetURI + ">"))
                .append("  : pav:createdBy <").append(eltonURI).append("> .")
                .append("  : dcterms:license <https://creativecommons.org/licenses/by/4.0/> . }");
        try {
            Nanopub preNanopub = new NanopubImpl(builder.toString(), RDFFormat.TRIG);
            Nanopub trustyNanopub = MakeTrustyNanopub.transform(preNanopub);
            String trustyNanopubString = NanopubUtils.writeToString(trustyNanopub, RDFFormat.TRIG);
            out.println(trustyNanopubString.replace("\n", " "));
        } catch (OpenRDFException | MalformedNanopubException | TrustyUriException ex) {
            throw new RuntimeException(ex);
        }
    }

    static String extractDatasetURI(Dataset dataset) {
        DOI doi = dataset.getDOI();
        String datasetURI = doi == null ? "" : doi.toURI().toString();
        if (StringUtils.isBlank(datasetURI)) {
            if (StringUtils.startsWith(dataset.getArchiveURI().toString(), "https://github.com/")) {
                datasetURI = dataset.getArchiveURI().toString().replaceFirst("/archive/[a-z0-9]+\\.zip", "");
            } else {
                datasetURI = dataset.getArchiveURI().toString();
            }
        }
        return datasetURI;
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
