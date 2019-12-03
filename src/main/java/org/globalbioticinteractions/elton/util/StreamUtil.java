package org.globalbioticinteractions.elton.util;

import org.apache.commons.lang.StringUtils;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.domain.Term;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetConstant;
import org.eol.globi.util.DateUtil;
import org.eol.globi.util.ExternalIdUtil;
import org.globalbioticinteractions.doi.DOI;
import org.globalbioticinteractions.elton.Elton;

import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtil {
    private static String version = Elton.getVersion();

    public static Stream<String> streamOf(Dataset dataset) {
        return Stream.of(
                dataset.getNamespace(),
                dataset.getCitation(),
                dataset.getArchiveURI().toString(),
                dataset.getOrDefault(DatasetConstant.LAST_SEEN_AT, ""),
                dataset.getOrDefault(DatasetConstant.CONTENT_HASH, ""),
                version);
    }

    public static Stream<String> streamOf(Taxon taxon) {
        return Stream.of(taxon.getExternalId(),
                taxon.getName(),
                taxon.getRank(),
                taxon.getPathIds(),
                taxon.getPath(),
                taxon.getPathNames());
    }

    public static String tsvRowOf(Stream<String> rowStream) {
        return rowStream
                .map(term -> null == term ? "" : term)
                .map(term -> term.replaceAll("[\\t\\n\\r]", " "))
                .collect(Collectors.joining("\t"));
    }

    public static Stream<String> streamOf(Study study) {
        String doiOrEmpty = "";
        String urlOrEmpty = "";
        String citationOrEmpty = "";
        if (null != study) {
            DOI doi = study.getDOI();
            doiOrEmpty = null == doi ? "" : doi.toString();
            String externalId = StringUtils.defaultIfBlank(study.getExternalId(), "");
            urlOrEmpty = ExternalIdUtil.urlForExternalId(externalId);
            if (StringUtils.isBlank(urlOrEmpty) && null != doi) {
                urlOrEmpty = doi.toURI().toString();
            }
            citationOrEmpty = StringUtils.defaultIfBlank(study.getCitation(), "");
        }
        return Stream.of(doiOrEmpty, urlOrEmpty, citationOrEmpty);
    }

    public static Stream<String> streamOf(Date date) {
        return Stream.of(DateUtil.printDate(date));
    }

    public static Stream<String> streamOf(Location loc) {
        return loc == null
                ? Stream.of("", "", "", "")
                : Stream.of(loc.getLatitude() == null ? "" : loc.getLatitude().toString()
                , loc.getLongitude() == null ? "" : loc.getLongitude().toString()
                , StringUtils.defaultString(loc.getLocalityId())
                , StringUtils.defaultString(loc.getLocality()));
    }

    public static Stream<String> datasetHeaderFields() {
        return Stream.of(DatasetConstant.NAMESPACE,
                DatasetConstant.CITATION,
                DatasetConstant.ARCHIVE_URI,
                DatasetConstant.LAST_SEEN_AT,
                DatasetConstant.CONTENT_HASH,
                "eltonVersion");
    }

    public static Stream<String> streamOf(Specimen specimen) {
        return Stream.concat(streamOf(specimen.getBodyPart()), streamOf(specimen.getLifeStage()));
    }

    public static Stream<String> streamOf(Term term) {
        return Stream.of(emptyOrId(term), emptyOrName(term));
    }

    private static String emptyOrId(Term term) {
        return term == null || term.getId() == null ? "" : term.getId();
    }

    private static String emptyOrName(Term term) {
        return term == null || term.getName() == null ? "" : term.getName();
    }
}
