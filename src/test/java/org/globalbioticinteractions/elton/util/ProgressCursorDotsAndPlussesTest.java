package org.globalbioticinteractions.elton.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ProgressCursorDotsAndPlussesTest {

    @Test
    public void progress() throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProgressCursor progressCursor = new ProgressCursorDotsAndPlusses(new PrintStream(out));
        progressCursor.increment();
        assertThat(out.toString(StandardCharsets.UTF_8.name()), is("."));
    }

    @Test
    public void progressNextLine() throws UnsupportedEncodingException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ProgressCursorDotsAndPlusses progressCursor = new ProgressCursorDotsAndPlusses(new PrintStream(out));
        for (int i = 0; i < progressCursor.getWidth(); i++) {
            progressCursor.increment();
        }
        assertThat(out.toString(StandardCharsets.UTF_8.name()), is(StringUtils.repeat('.', progressCursor.getWidth() - 1) + "\r+"));
    }

}