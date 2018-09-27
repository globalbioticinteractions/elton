package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Study;
import org.eol.globi.service.Dataset;

public interface InteractionWriter {
    void write(SpecimenImpl source, InteractType type, SpecimenImpl target, Study study, Dataset dataset);
}
