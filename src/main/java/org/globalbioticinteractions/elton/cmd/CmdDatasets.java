package org.globalbioticinteractions.elton.cmd;


import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.elton.util.StreamUtil;
import org.globalbioticinteractions.elton.util.TabularWriter;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "datasets",
        description = CmdDatasets.DESCRIPTION
)
public class CmdDatasets extends CmdTabularWriterParams {

    public static final String DESCRIPTION = "List Datasets Details";
    public static final String RECORD_TYPE_DATASET = "dataset";

    @Override
    public String getRecordType() {
        return RECORD_TYPE_DATASET;
    }

    public class TsvDatasetWriter implements TabularWriter {
        private final PrintStream out;

        TsvDatasetWriter(PrintStream out) {
            this.out = out;
        }

        public void write(Dataset dataset) {
            Stream<String> datasetInfo = CmdUtil.datasetInfo(dataset).stream();
            String row = StreamUtil.tsvRowOf(datasetInfo);
            out.println(row);
        }

        @Override
        public void writeHeader() {
            Stream<String> datasetHeaders = StreamUtil.datasetHeaderFields();
            out.println(StreamUtil.tsvRowOf(datasetHeaders));
        }
    }

    @Override
    public void doRun() {
        run(getStdout());
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    void run(PrintStream out) {
        PrintStream dataSink = getDataSink(out);
        TsvDatasetWriter serializer = new TsvDatasetWriter(dataSink);
        if (!shouldSkipHeader()) {
            serializer.writeHeader();
        }

        DatasetRegistry registry = getDatasetRegistryWithProv();

        try {
            CmdUtil.handleNamespaces(registry,
                    namespace -> serializer.write(registry.datasetFor(namespace)),
                    getNamespaces());
        } catch (DatasetRegistryException e) {
            throw new RuntimeException("failed to datasets", e);
        }

    }
}


