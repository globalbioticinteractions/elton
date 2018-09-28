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
import org.eol.globi.domain.Location;
import org.eol.globi.domain.LogContext;
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

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

@Parameters(separators = "= ", commandDescription = "Check Dataset Accessibility. If no namespace is provided the local workdir is used.")
public class CmdCheck extends CmdDefaultParams {
    private final static Log LOG = LogFactory.getLog(CmdCheck.class);

    @Override
    public void run() {
        try {
            if (getNamespaces().isEmpty()) {
                checkLocal();
            } else {
                checkCacheOrRemote();
            }
        } catch (StudyImporterException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkCacheOrRemote() throws StudyImporterException {
        for (String namespace : getNamespaces()) {
            check(namespace, DatasetRegistryUtil.forCacheDir(getCacheDir()));
        }
    }

    private void checkLocal() throws StudyImporterException {
        String localNamespace = "local";
        DatasetRegistry finderLocal = DatasetRegistryUtil.forLocalDir(getWorkDir());
        check(localNamespace, finderLocal);
    }

    private void check(String repoName, DatasetRegistry finder) throws StudyImporterException {
        final Set<String> infos = Collections.synchronizedSortedSet(new TreeSet<String>());
        final Set<String> warnings = Collections.synchronizedSortedSet(new TreeSet<String>());
        final Set<String> errors = Collections.synchronizedSortedSet(new TreeSet<String>());

        ParserFactoryLocal parserFactory = new ParserFactoryLocal();
        AtomicInteger counter = new AtomicInteger(0);
        ImportLogger importLogger = createImportLogger(repoName, infos, warnings, errors);

        NodeFactoryLogging nodeFactory = new NodeFactoryLogging(counter, importLogger);
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
            if (warnings.size() > 0 || errors.size() > 0 || counter.get() == 0) {
                throw new StudyImporterException("check not successful, please check log.");
            }
        } catch (DatasetFinderException e) {
            getStdout().println(repoName + "\tno local repository at [" + getWorkDir().toString() + "].");
            throw new StudyImporterException(e);
        } finally {
            infos.forEach(getStdout()::println);
            warnings.forEach(getStdout()::println);
            errors.forEach(getStdout()::println);
            getStdout().println(repoName + "\t" + counter.get() + " interaction(s)");
            getStdout().println(repoName + "\t" + errors.size() + " error(s)");
            getStdout().println(repoName + "\t" + warnings.size() + " warning(s)");

        }
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

        public NodeFactoryLogging(AtomicInteger counter, ImportLogger importLogger) {
            this.counter = counter;
            this.importLogger = importLogger;
        }

        final Specimen specimen = new SpecimenNull() {
            @Override
            public void interactsWith(Specimen target, InteractType type, Location centroid) {
                if (counter.get() > 0 && counter.get() % 1000 == 0) {
                    CmdCheck.super.getStderr().println();
                }
                if (counter.get() % 10 == 0) {
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
        public void setUnixEpochProperty(Specimen specimen, Date date) throws NodeFactoryException {
            if (date != null && date.after(new Date())) {
                importLogger.warn(null, "date [" + DateUtil.printDate(date) + "] is in the future");
            }
        }
    }
}
