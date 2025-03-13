package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Study;
import org.globalbioticinteractions.dataset.Dataset;

import java.io.Closeable;

public interface InteractionWriter extends Closeable {
    void write(SpecimenImpl source, InteractType type, SpecimenImpl target, Study study, Dataset dataset);
}
