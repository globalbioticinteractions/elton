package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.elton.util.InteractionWriter;
import org.globalbioticinteractions.elton.util.SpecimenImpl;
import org.globalbioticinteractions.elton.util.StreamUtil;
import org.globalbioticinteractions.elton.util.TaxonWriter;

import java.io.IOException;
import java.io.PrintStream;
import java.util.stream.Stream;

public class WriterTaxonTSV implements TaxonWriter, InteractionWriter {

    private final PrintStream out;

    public WriterTaxonTSV(PrintStream out) {
        this.out = out;
    }

    @Override
    public void write(Taxon taxon, Dataset dataset) {
        Stream<String> rowStream = Stream.concat(StreamUtil.streamOf(taxon), StreamUtil.streamOf(dataset));
        String row = StreamUtil.tsvRowOf(rowStream);
        out.println(row);
    }

    @Override
    public void writeHeader() {
        out.println(StreamUtil.tsvRowOf(
                Stream.concat(Stream.of(
                        "taxonId",
                        "taxonName",
                        "taxonRank",
                        "taxonPathIds",
                        "taxonPath",
                        "taxonPathNames"),
                        StreamUtil.datasetHeaderFields())));
    }

    @Override
    public void write(SpecimenImpl source, InteractType type, SpecimenImpl target, Study study, Dataset dataset) {

    }

    @Override
    public void close() throws IOException {

    }
}
