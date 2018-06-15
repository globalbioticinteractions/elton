package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.LocationImpl;
import org.eol.globi.domain.StudyImpl;
import org.globalbioticinteractions.doi.DOI;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class StreamUtilTest {

    @Test
    public void eventDate() {
        Stream<String> dateStream = StreamUtil.streamOf(new Date(0));
        assertThat(dateStream.collect(Collectors.toList()), is(Arrays.asList("1970-01-01T00:00:00Z")));
    }

    @Test
    public void study() {
        Stream<String> dateStream = StreamUtil.streamOf(new StudyImpl("some title", "source source", new DOI("123", "456"), "some citation"));
        assertThat(dateStream.collect(Collectors.toList()), is(Arrays.asList("10.123/456", "https://doi.org/10.123/456", "some citation")));
    }

    @Test
    public void studyUnsupportedExternalIdScheme() {
        StudyImpl study = new StudyImpl("some title", "source source", new DOI("123", "456"), "some citation");
        study.setExternalId("some:id");
        Stream<String> dateStream = StreamUtil.streamOf(study);
        assertThat(dateStream.collect(Collectors.toList()), is(Arrays.asList("10.123/456", "https://doi.org/10.123/456", "some citation")));
    }

    @Test
    public void studySupportedExternalIdScheme() {
        StudyImpl study = new StudyImpl("some title", "source source", new DOI("123", "456"), "some citation");
        study.setExternalId("https://example.org/something");
        Stream<String> dateStream = StreamUtil.streamOf(study);
        assertThat(dateStream.collect(Collectors.toList()), is(Arrays.asList("10.123/456", "https://example.org/something", "some citation")));
    }

    @Test
    public void location() {
        LocationImpl loc = new LocationImpl(12.1, 12.2, null, null);
        loc.setLocality("some locality");
        loc.setLocalityId("some:localityId");
        Stream<String> locationStream = StreamUtil.streamOf(loc);
        assertThat(locationStream.collect(Collectors.toList()), is(Arrays.asList("12.1", "12.2", "some:localityId", "some locality")));
    }

    @Test(expected = IllegalStateException.class)
    public void tsvRowOf() {
        Stream<String> row = Stream.of("one", "two");
        assertThat(StreamUtil.tsvRowOf(Stream.concat(row, Stream.of("three"))), is("one\ttwo\tthree"));
        assertThat(StreamUtil.tsvRowOf(row), is("one\ttwo"));
    }


}