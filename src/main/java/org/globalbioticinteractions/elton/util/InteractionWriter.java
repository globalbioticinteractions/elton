package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Study;
import org.eol.globi.service.Dataset;

import java.util.List;
import java.util.stream.Stream;

public interface InteractionWriter {
    void write(SpecimenTaxonOnly source, InteractType type, SpecimenTaxonOnly target, Study study, Dataset dataset, List<String> datasetInfo);
}
