package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.data.ParserFactoryLocal;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.data.StudyImporterForRegistry;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.LogContext;
import org.eol.globi.domain.RelTypes;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetRegistry;
import org.eol.globi.util.DateUtil;
import org.globalbioticinteractions.dataset.CitationUtil;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.SpecimenNull;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

@Parameters(separators = "= ", commandDescription = "Check Dataset Accessibility. If no namespace is provided the local workdir is used.")
public class CmdCheck extends CmdDefaultParams {
    private final static Log LOG = LogFactory.getLog(CmdCheck.class);

    @Override
    public void run() {
        try {
            List<URI> localNamespaces = new ArrayList<>();
            List<String> remoteNamespaces = new ArrayList<>();
            if (getNamespaces().isEmpty()) {
                localNamespaces.add(getWorkDir());
            } else {
                for (String namespace : getNamespaces()) {
                    URI uri = URI.create(namespace);
                    if (uri.isAbsolute() && new File(uri).exists()) {
                        localNamespaces.add(uri);
                    } else {
                        remoteNamespaces.add(namespace);
                    }
                }
            }

            for (URI localNamespace : localNamespaces) {
                checkLocal(localNamespace);
            }
            checkCacheOrRemote(remoteNamespaces);


        } catch (StudyImporterException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkCacheOrRemote(List<String> namespaces) throws StudyImporterException {
        for (String namespace : namespaces) {
            check(namespace, DatasetRegistryUtil.forCacheDir(getCacheDir()));
        }
    }

    private void checkLocal(URI workDir) throws StudyImporterException {
        DatasetRegistry finderLocal = DatasetRegistryUtil.forLocalDir(workDir);
        check("local", finderLocal);
    }

    private void check(String repoName, DatasetRegistry finder) throws StudyImporterException {
        final Set<String> infos = Collections.synchronizedSortedSet(new TreeSet<String>());
        final Set<String> warnings = Collections.synchronizedSortedSet(new TreeSet<String>());
        final Set<String> errors = Collections.synchronizedSortedSet(new TreeSet<String>());

        ParserFactoryLocal parserFactory = new ParserFactoryLocal();
        AtomicInteger counter = new AtomicInteger(0);
        ImportLogger importLogger = createImportLogger(repoName, infos, warnings, errors);

        NodeFactoryLogging nodeFactory = new NodeFactoryLogging(counter, importLogger, getWidthOrDefault());
        StudyImporterForRegistry studyImporterForGitHubData = new StudyImporterForRegistry(parserFactory, nodeFactory, finder);
        studyImporterForGitHubData.setLogger(importLogger);

        try {
            Dataset dataset = DatasetFactory.datasetFor(repoName, finder);
            if (StringUtils.isBlank(CitationUtil.citationOrDefaultFor(dataset, ""))) {
                importLogger.warn(null, "no citation found for dataset at [" + dataset.getArchiveURI() + "]");
            }
            nodeFactory.getOrCreateDataset(dataset);
            String msg = "checking [" + repoName + "] at [" + dataset.getArchiveURI().toString() + "]...";
            getStderr().println(msg);
            studyImporterForGitHubData.importData(dataset);
            getStderr().println(" done.");
            getStdout().println(repoName + "\t" + dataset.getArchiveURI().toString());
        } catch (DatasetFinderException e) {
            getStdout().println(repoName + "\tno local repository at [" + getWorkDir().toString() + "].");
            throw new StudyImporterException(e);
        } catch (Throwable e) {
            errors.add(e.getMessage());
            throw new StudyImporterException(e);
        } finally {
            infos.forEach(getStdout()::println);
            warnings.forEach(getStdout()::println);
            errors.forEach(getStdout()::println);
            getStdout().println(repoName + "\t" + counter.get() + " interaction(s)");
            getStdout().println(repoName + "\t" + errors.size() + " error(s)");
            getStdout().println(repoName + "\t" + warnings.size() + " warning(s)");
        }
        if (warnings.size() > 0 || errors.size() > 0) {
            throw new StudyImporterException("check not successful, please check log.");
        } else if (counter.get() == 0) {
            throw new StudyImporterException("failed to find any interactions, please check dataset configuration and format.");
        }
    }

    private int getWidthOrDefault() {
        final int widthDefault = 80;
        int width = widthDefault;
        try {
            width = TerminalBuilder.builder().build().getWidth();
        } catch (IOException e) {
            // ignore
        }
        return width > 0 ? width : widthDefault;
    }

    private ImportLogger createImportLogger(String repoName, Set<String> infos, Set<String> warnings, Set<String> errors) {
        return new ImportLogger() {
            @Override
            public void info(LogContext study, String message) {
                addUntilFull(message, infos);
            }

            @Override
            public void warn(LogContext study, String message) {
                addUntilFull(message, warnings);
            }

            @Override
            public void severe(LogContext study, String message) {
                addUntilFull(message, errors);
            }

            private void addUntilFull(String message, Set<String> msgs) {
                if (msgs.size() == 500) {
                    msgs.add(">= 500 unique messages, turning off logging.");
                } else if (msgs.size() < 500) {
                    msgs.add(msgForRepo(message));
                }
            }

            String msgForRepo(String message) {
                return repoName + "\t" + message;
            }
        };
    }

    private class NodeFactoryLogging extends NodeFactoryNull {
        final AtomicInteger counter;
        final ImportLogger importLogger;
        private final int width;

        public NodeFactoryLogging(AtomicInteger counter, ImportLogger importLogger, int width) {
            this.counter = counter;
            this.importLogger = importLogger;
            this.width = width;
        }

        final Specimen specimen = new SpecimenNull() {
            @Override
            public void interactsWith(Specimen target, InteractType type, Location centroid) {
                int reportBatchSize = 10;
                if (counter.get() > 0 && counter.get() % (reportBatchSize * width) == 0) {
                    CmdCheck.super.getStderr().println();
                }
                if (counter.get() % reportBatchSize == 0) {
                    CmdCheck.super.getStderr().print(".");
                }
                counter.getAndIncrement();
            }
        };


        @Override
        public Specimen createSpecimen(Study study, Taxon taxon) throws NodeFactoryException {
            return specimen;
        }

        @Override
        public Specimen createSpecimen(Interaction interaction, Taxon taxon) throws NodeFactoryException {
            return specimen;
        }

        @Override
        public Specimen createSpecimen(Study study, Taxon taxon, RelTypes... types) throws NodeFactoryException {
            return specimen;
        }

        @Override
        public void setUnixEpochProperty(Specimen specimen, Date date) throws NodeFactoryException {
            if (date != null && date.after(new Date())) {
                importLogger.warn(null, "date [" + DateUtil.printDate(date) + "] is in the future");
            }
        }
    }
}
