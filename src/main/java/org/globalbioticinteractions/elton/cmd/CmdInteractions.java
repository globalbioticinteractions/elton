package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.Hasher;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.StatementsEmitterAdapter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactory;
import org.eol.globi.domain.LogContext;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.store.ProvUtil;
import org.globalbioticinteractions.elton.util.ProgressUtil;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static bio.guoda.preston.RefNodeFactory.toIRI;
import static bio.guoda.preston.RefNodeFactory.toStatement;

@CommandLine.Command(
        name = "interactions",
        aliases = {"interaction", "interact"},
        description = CmdInteractions.DESCRIPTION
)
public class CmdInteractions extends CmdTabularWriterParams {

    public static final String DESCRIPTION = "List Interactions";
    public static final String RECORD_TYPE_INTERACTION = "interaction";

    @Override
    public void doRun() {
        run(getStdout());
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    void run(PrintStream out) {

        DatasetRegistry registry = getDatasetRegistryWithProv();

        NodeFactory nodeFactory = getNodeFactoryForProv(out);

        final File file = new File(getWorkDir());
        CmdUtil.handleNamespaces(
                registry,
                getNamespaces(),
                "listing interactions",
                getStderr(),
                getNamespaceHandler(registry, nodeFactory, file, getLogger())
        );

        emitProcessDescription();
    }

    @Override
    public String getRecordType() {
        return RECORD_TYPE_INTERACTION;
    }
}


