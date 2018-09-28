package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetRegistry;
import org.globalbioticinteractions.elton.util.DatasetProcessor;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.NamespaceHandler;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.StreamUtil;
import org.globalbioticinteractions.elton.util.TabularWriter;

import java.io.PrintStream;
import java.util.stream.Stream;

@Parameters(separators = "= ", commandDescription = "List Info For Local Datasets")
public class CmdDatasets extends CmdTabularWriterParams {

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
    public void run() {
        run(System.out);
    }

    void run(PrintStream out) {
        TsvDatasetWriter serializer = new TsvDatasetWriter(out);
        if (!shouldSkipHeader()) {
            serializer.writeHeader();
        }

        DatasetRegistry finder = DatasetRegistryUtil.forCacheDirOrLocalDir(getCacheDir(), getWorkDir());

        try {
            CmdUtil.handleNamespaces(finder,
                    namespace -> serializer.write(finder.datasetFor(namespace)),
                    getNamespaces());
        } catch (DatasetFinderException e) {
            throw new RuntimeException("failed to datasets", e);
        }

    }
}


