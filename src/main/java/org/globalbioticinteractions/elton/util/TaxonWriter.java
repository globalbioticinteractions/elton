package org.globalbioticinteractions.elton.util;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.globalbioticinteractions.dataset.Dataset;

public interface TaxonWriter extends TabularWriter {
    void write(Taxon taxon, Dataset dataset);
}
