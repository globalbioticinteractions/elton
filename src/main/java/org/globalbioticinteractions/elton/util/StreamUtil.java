package org.globalbioticinteractions.elton.util;

import org.eol.globi.Version;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamUtil {
    public static Stream<String> streamOf(Dataset dataset) {
        return Stream.of(
                dataset.getNamespace(),
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
}
