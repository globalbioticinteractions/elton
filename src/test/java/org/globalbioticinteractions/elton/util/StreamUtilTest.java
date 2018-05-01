package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.LocationImpl;
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
    public void location() {
        LocationImpl loc = new LocationImpl(12.1, 12.2, null, null);
        loc.setLocality("some locality");
        loc.setLocalityId("some:localityId");
        Stream<String> locationStream = StreamUtil.streamOf(loc);
        assertThat(locationStream.collect(Collectors.toList()), is(Arrays.asList("12.1", "12.2", "some:localityId", "some locality")));
    }


}