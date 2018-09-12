package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.data.ParserFactoryLocal;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.data.StudyImporterForGitHubData;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.LogContext;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinder;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetFinderGitHubArchiveMaster;
import org.eol.globi.service.DatasetFinderProxy;
import org.eol.globi.service.DatasetImpl;
import org.eol.globi.service.GitHubImporterFactory;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.SpecimenNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.exit;

@Parameters(separators = "= ", commandDescription = "Check Dataset Accessibility. If no namespace is provided the local workdir is used.")
public class CmdCheck extends CmdOfflineParams {
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
            check(namespace, isOffline()
                    ? CmdUtil.getDatasetFinderLocal(getCacheDir())
                    : createDataFinderForGitHub(namespace, getCacheDir()));
        }
    }

    private void checkLocal() throws StudyImporterException {
        String localNamespace = "local";
        DatasetFinder finderLocal = new DatasetFinder() {

            @Override
            public Collection<String> findNamespaces() throws DatasetFinderException {
                return Collections.singletonList(localNamespace);
            }

            @Override
            public Dataset datasetFor(String namespace) throws DatasetFinderException {
                return new DatasetImpl(localNamespace, getWorkDir());
            }
        };
        check(localNamespace, finderLocal);
    }

    private void check(String repoName, DatasetFinder finder) throws StudyImporterException {
        final Set<String> infos = Collections.synchronizedSortedSet(new TreeSet<String>());
        final Set<String> warnings = Collections.synchronizedSortedSet(new TreeSet<String>());
        final Set<String> errors = Collections.synchronizedSortedSet(new TreeSet<String>());

        ParserFactoryLocal parserFactory = new ParserFactoryLocal();
        AtomicInteger counter = new AtomicInteger(0);
        NodeFactoryLogging nodeFactory = new NodeFactoryLogging(counter);
        StudyImporterForGitHubData studyImporterForGitHubData = new StudyImporterForGitHubData(parserFactory, nodeFactory, finder);
        studyImporterForGitHubData.setLogger(new ImportLogger() {
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
        });

        try {
            Dataset dataset = DatasetFactory.datasetFor(repoName, finder);
            nodeFactory.getOrCreateDataset(dataset);
            String msg = "checking [" + repoName + "] at [" + dataset.getArchiveURI().toString() + "]...";
            getStderr().println(msg);
            studyImporterForGitHubData.importData(dataset);
            getStderr().println(" done.");
            if (warnings.size() > 0 || errors.size() > 0 || counter.get() == 0) {
                throw new StudyImporterException("check not successful, please check log.");
            }
            getStdout().println(repoName + "\t" + dataset.getArchiveURI().toString());
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

    private DatasetFinder createDataFinderForGitHub(String namespace, String cacheDir) {
        List<DatasetFinder> finders = Collections.singletonList(new DatasetFinderGitHubArchiveMaster(Collections.singletonList(namespace)));
        DatasetFinderProxy finder = new DatasetFinderProxy(finders);
        return CmdUtil.createDataFinderLoggingCaching(finder, namespace, cacheDir);
    }

    private static String getResultMsg(String repoName, Set<String> warnings, Set<String> errors, AtomicInteger counter) {
        return "found [" + counter.get() + "] interactions in [" + repoName + "]"
                + " and encountered [" + warnings.size() + "] warnings and [" + errors.size() + "] errors";
    }

    private class NodeFactoryLogging extends NodeFactoryNull {
        final AtomicInteger counter;

        NodeFactoryLogging(AtomicInteger counter) {
            this.counter = counter;
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
    }
}
