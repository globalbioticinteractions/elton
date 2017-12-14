package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.domain.InteractType;
import org.eol.globi.service.Dataset;

public interface InteractionSerializerInterface {
    void serialize(SpecimenTaxonOnly source, InteractType type, SpecimenTaxonOnly target, Dataset dataset);
}
