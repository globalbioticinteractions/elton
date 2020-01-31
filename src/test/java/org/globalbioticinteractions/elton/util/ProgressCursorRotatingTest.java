package org.globalbioticinteractions.elton.util;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProgressCursorRotatingTest {

    @Test
    public void progress() throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProgressCursor progressCursor = new ProgressCursorRotating(new PrintStream(out));
        for(int i=0; i< 8; i++) {
            progressCursor.increment();
        }
        assertThat(out.toString(StandardCharsets.UTF_8.name()), is("-\b\\\b|\b/\b-\b\\\b|\b-"));
    }

}