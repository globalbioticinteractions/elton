package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.InteractType;
import org.eol.globi.service.Dataset;

public interface InteractionWriter {
    void write(SpecimenTaxonOnly source, InteractType type, SpecimenTaxonOnly target, Dataset dataset);
}
