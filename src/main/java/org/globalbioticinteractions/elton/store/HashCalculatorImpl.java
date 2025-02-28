package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import org.globalbioticinteractions.dataset.HashCalculator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashCalculatorImpl implements HashCalculator {
    private final HashType hashType;
    private static final Pattern HEX_PATTERN = Pattern.compile("hash://[a-z0-9]+/(?<hexPart>[a-f0-9]+).*");

    public HashCalculatorImpl(HashType hashType) {
        this.hashType = hashType;
    }

    @Override
    public String calculateContentHash(InputStream is, OutputStream os) throws IOException, NoSuchAlgorithmException {
        String iriString = Hasher.calcHashIRI(is, os, hashType).getIRIString();
        return getHexPartIfAvailable(iriString);
    }

    public  static String getHexPartIfAvailable(String iriString) {
        Matcher matcher = HEX_PATTERN.matcher(iriString);
        return matcher.matches() ? matcher.group("hexPart") : iriString;
    }
}
