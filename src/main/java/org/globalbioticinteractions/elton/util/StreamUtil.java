package org.globalbioticinteractions.elton.util;

import org.apache.commons.lang.StringUtils;
import org.eol.globi.Version;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.eol.globi.util.DateUtil;
import org.eol.globi.util.ExternalIdUtil;
import org.globalbioticinteractions.dataset.CitationUtil;
import org.globalbioticinteractions.doi.DOI;
import org.globalbioticinteractions.elton.Elton;

import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StreamUtil {
    private static String version = Elton.getVersion();

    public static Stream<String> streamOf(Dataset dataset, String citation) {
        return Stream.of(
                dataset.getNamespace(),
                citation,
                dataset.getArchiveURI().toString(),
                dataset.getOrDefault("accessedAt", ""),
                dataset.getOrDefault("contentHash", ""),
                version);
    }

    public static Stream<String> streamOf(Taxon taxon) {
        return Stream.of(taxon.getExternalId(),
                taxon.getName(),
                taxon.getRank(),
                taxon.getPath(),
                taxon.getPathIds(),
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
}
