package org.globalbioticinteractions.elton.util;

import org.apache.commons.lang.StringUtils;
import org.eol.globi.Version;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.eol.globi.util.ExternalIdUtil;
import org.globalbioticinteractions.dataset.CitationUtil;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtil {
    public static Stream<String> streamOf(Dataset dataset) {
        return Stream.of(
                dataset.getNamespace(),
                CitationUtil.citationOrDefaultFor(dataset, ""),
                dataset.getArchiveURI().toString(),
                dataset.getOrDefault("accessedAt", ""),
                dataset.getOrDefault("contentHash", ""),
                Version.getVersion());
    }

    public static Stream<String> streamOf(Taxon taxon) {
        return Stream.of(taxon.getName(),
                taxon.getRank(),
                taxon.getExternalId(),
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
        String doi = study == null ? "" : study.getDOI();
        String urlOrEmpty = ExternalIdUtil.urlForExternalId(StringUtils.isBlank(doi) ? study.getExternalId() : doi);
        String citationOrEmpty = study == null ? "" : study.getCitation();
        return Stream.of(urlOrEmpty, citationOrEmpty);
    }
}
