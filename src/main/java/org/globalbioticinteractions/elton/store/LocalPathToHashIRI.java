package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.store.Dereferencer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.globalbioticinteractions.cache.CacheUtil;
import org.globalbioticinteractions.cache.ProvenanceLog;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalPathToHashIRI implements Dereferencer<IRI> {

    private final File dataFolder;

    private final Pattern CLASSPATH_URL_PATTERN = Pattern.compile("([a-z]+:)*(classpath:)(?<classpath>[^!]+)(.*)");


    public LocalPathToHashIRI(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    @Override
    public IRI get(IRI iri) throws IOException {
        URI original = URI.create(iri.getIRIString());
        URI translated = null;
        if (CacheUtil.isInCacheDir(dataFolder, original)) {
            translated = getLocationInDataDir(iri, original, translated);
        } else {
            translated = handleAsClasspath(iri);
        }
        return RefNodeFactory.toIRI(translated == null ? original : translated);
    }

    private URI handleAsClasspath(IRI iri) {
        URI translated = null;
        Matcher matcher = CLASSPATH_URL_PATTERN.matcher(iri.getIRIString());
        if (matcher.matches()) {
            URL location = getClass().getResource(matcher.group("classpath"));
            if (location != null) {
                try {
                    translated = location.toURI();
                } catch (URISyntaxException e) {
                    // ignore
                }
            }
        }
        return translated;
    }

    private URI getLocationInDataDir(IRI iri, URI original, URI translated) {
        Matcher filePatternMatcher = CacheUtil.FILE_URL_PATTERN.matcher(iri.getIRIString());
        if (filePatternMatcher.matches()) {
            String filepath = filePatternMatcher.group("filepath");
            Pattern hashPattern = Pattern.compile("(.*)(?<hash>[a-f0-9]{64})$");
            Matcher matcher = hashPattern.matcher(filepath);
            if (matcher.matches()) {
                String hash = matcher.group("hash");
                filepath = Hasher.toHashIRI(HashType.sha256, hash).getIRIString();
                String filePrefix = filePatternMatcher.group(2);
                String suffix = filePatternMatcher.group(4);
                String toBeReplaced =
                        filePrefix
                                + filePatternMatcher.group("filepath")
                                + suffix;

                String translatedString = StringUtils.replace(
                        original.toString(),
                        toBeReplaced,
                        filepath + suffix
                );

                translated = URI.create(translatedString);
            }
        }
        return translated;
    }
}
