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
import org.eol.globi.service.DatasetFinder;
import org.eol.globi.service.DatasetFinderGitHubArchiveMaster;
import org.eol.globi.service.DatasetFinderProxy;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.SpecimenNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

@Parameters(separators = "= ", commandDescription = "Check Dataset Accessibility")
public class CmdCheck extends CmdOfflineParams {
    private final static Log LOG = LogFactory.getLog(CmdCheck.class);

    @Override
    public void run() {
        try {
            for (String namespace : getNamespaces()) {
                check(namespace, getCacheDir());
            }
        } catch (StudyImporterException e) {
            throw new RuntimeException(e);
        }
    }

    private void check(String repoName, String cacheDir) throws StudyImporterException {
        final Set<String> infos = Collections.synchronizedSortedSet(new TreeSet<String>());
        final Set<String> warnings = Collections.synchronizedSortedSet(new TreeSet<String>());
        final Set<String> errors = Collections.synchronizedSortedSet(new TreeSet<String>());

        DatasetFinder finder = isOffline()
                ? CmdUtil.getDatasetFinderLocal(getCacheDir())
                : createDataFinderForGitHub(repoName, cacheDir);

        ParserFactoryLocal parserFactory = new ParserFactoryLocal();
        StudyImporterForGitHubData studyImporterForGitHubData = new StudyImporterForGitHubData(parserFactory, new NodeFactoryLogging(), finder);
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
                return "[" + repoName + "]: [" + message + "]";
            }
        });

        try {
            studyImporterForGitHubData.importData(repoName);
            if (warnings.size() > 0 || errors.size() > 0 || NodeFactoryLogging.counter.get() == 0) {
                throw new StudyImporterException(getResultMsg(repoName, warnings, errors) + ", please check your log.");
            }
        } finally {
            infos.forEach(LOG::info);
            warnings.forEach(LOG::warn);
            errors.forEach(LOG::error);
            System.out.println("\n" + getResultMsg(repoName, warnings, errors));
        }
    }

    private DatasetFinder createDataFinderForGitHub(String namespace, String cacheDir) {
        List<DatasetFinder> finders = Collections.singletonList(new DatasetFinderGitHubArchiveMaster(Collections.singletonList(namespace)));
        DatasetFinderProxy finder = new DatasetFinderProxy(finders);
        return CmdUtil.createDataFinderLoggingCaching(finder, namespace, cacheDir);
    }

    private static String getResultMsg(String repoName, Set<String> warnings, Set<String> errors) {
        return "found [" + NodeFactoryLogging.counter.get() + "] interactions in [" + repoName + "]"
                + " and encountered [" + warnings.size() + "] warnings and [" + errors.size() + "] errors";
    }

    private static class NodeFactoryLogging extends NodeFactoryNull {
        final static AtomicInteger counter = new AtomicInteger(0);
        final static Specimen specimen = new SpecimenNull() {
            @Override
            public void interactsWith(Specimen target, InteractType type, Location centroid) {
                if (counter.get() > 0 && counter.get() % 1000 == 0) {
                    System.out.println();
                }
                if (counter.get() % 10 == 0) {
                    System.out.print(".");
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
