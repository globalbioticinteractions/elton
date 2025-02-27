package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.HashType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HashCalculatorImplTest {

    @Test
    public void calculateSha256() throws IOException, NoSuchAlgorithmException {
        String contentID = new HashCalculatorImpl(HashType.sha256).calculateContentHash(IOUtils.toInputStream("hello", StandardCharsets.UTF_8), NullOutputStream.INSTANCE);
        assertThat(contentID, is("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));
    }

    @Test
    public void calculateMD5() throws IOException, NoSuchAlgorithmException {
        String contentID = new HashCalculatorImpl(HashType.md5).calculateContentHash(IOUtils.toInputStream("hello", StandardCharsets.UTF_8), NullOutputStream.INSTANCE);
        assertThat(contentID, is("5d41402abc4b2a76b9719d911017c592"));
    }

}