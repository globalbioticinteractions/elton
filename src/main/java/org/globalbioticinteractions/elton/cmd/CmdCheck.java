package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.data.ParserFactoryLocal;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.data.StudyImporterForRegistry;
import org.eol.globi.data.StudyImporterForTSV;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.LogContext;
import org.eol.globi.domain.RelTypes;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.service.Dataset;
import org.eol.globi.service.DatasetConstant;
import org.eol.globi.service.DatasetFactory;
import org.eol.globi.service.DatasetFinderException;
import org.eol.globi.service.DatasetRegistry;
import org.eol.globi.util.CSVTSVUtil;
import org.eol.globi.util.DateUtil;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.dataset.CitationUtil;
import org.globalbioticinteractions.elton.Elton;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.ProgressUtil;
import org.globalbioticinteractions.elton.util.SpecimenNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Parameters(separators = "= ", commandDescription = "Check Dataset Accessibility. If no namespace is provided the local workdir is used.")
public class CmdCheck extends CmdDefaultParams {
    private final static Log LOG = LogFactory.getLog(CmdCheck.class);
    public static final String LOG_FORMAT_STRING = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s";

    @Parameter(names = {"-n", "--lines"}, description = "print first n number of lines")
    private Integer maxLines = null;

    private DateFactory dateFactory = Date::new;

    private String reviewerName = "GloBI automated reviewer (elton-" + Elton.getVersion() + ")";

    private String reviewId = UUID.randomUUID().toString();

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

            InputStreamFactory factory = createInputStreamFactory();

            for (URI localNamespace : localNamespaces) {
                checkLocal(localNamespace, factory);
            }
            checkCacheOrRemote(remoteNamespaces, factory);


        } catch (StudyImporterException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkCacheOrRemote(List<String> namespaces, InputStreamFactory inputStreamFactory) throws StudyImporterException {
        for (String namespace : namespaces) {
            check(namespace, DatasetRegistryUtil.forCacheDir(getCacheDir(), inputStreamFactory), inputStreamFactory);
        }
    }

    private void checkLocal(URI workDir, InputStreamFactory inputStreamFactory) throws StudyImporterException {
        DatasetRegistry finderLocal = DatasetRegistryUtil.forLocalDir(workDir, getTmpDir(), inputStreamFactory);
        check("local", finderLocal, inputStreamFactory);
    }

    private void check(String repoName, DatasetRegistry finder, InputStreamFactory inputStreamFactory) throws StudyImporterException {
        final AtomicLong warningCount = new AtomicLong(0);
        final AtomicLong errorCount = new AtomicLong(0);

        ParserFactoryLocal parserFactory = new ParserFactoryLocal();
        AtomicInteger interactionCounter = new AtomicInteger(0);
        ImportLogger importLogger = createImportLogger(repoName, warningCount, errorCount);

        NodeFactoryLogging nodeFactory = new NodeFactoryLogging(interactionCounter, importLogger);
        StudyImporterForRegistry studyImporter = new StudyImporterForRegistry(parserFactory, nodeFactory, finder);
        studyImporter.setLogger(importLogger);

        try {
            Dataset dataset = new DatasetFactory(
                    finder,
                    inputStreamFactory)
                    .datasetFor(repoName);

            if (StringUtils.isBlank(CitationUtil.citationOrDefaultFor(dataset, ""))) {
                importLogger.warn(null, "no citation found for dataset at [" + dataset.getArchiveURI() + "]");
            }
            nodeFactory.getOrCreateDataset(dataset);
            String msg = "checking [" + repoName + "] at [" + dataset.getArchiveURI().toString() + "]...";
            getStderr().println(msg);
            logHeader(getStdout());
            studyImporter.importData(dataset);
            getStderr().println(" done.");
            importLogger.info(null, dataset.getArchiveURI().toString());
        } catch (DatasetFinderException e) {
            importLogger.info(null, "no local repository at [" + getWorkDir().toString() + "]");
            throw new StudyImporterException(e);
        } catch (Throwable e) {
            e.printStackTrace();
            importLogger.severe(null, e.getMessage());
            throw new StudyImporterException(e);
        } finally {
            importLogger.info(null, interactionCounter.get() + " interaction(s)");
            importLogger.info(null, errorCount.get() + " error(s)");
            importLogger.info(null, warningCount.get() + " warning(s)");
        }
        if (warningCount.get() + errorCount.get() > 0) {
            throw new StudyImporterException("check not successful, please check log.");
        } else if (interactionCounter.get() == 0) {
            throw new StudyImporterException("failed to find any interactions, please check dataset configuration and format.");
        }
    }

    private void logHeader(PrintStream out) {
        logReviewComment(out, "reviewId", "reviewDate", "reviewer", "namespace", "reviewComment", "archiveURI", "referenceUrl", "institutionCode", "collectionCode", "collectionId", "catalogNumber", "occurrenceId", "sourceCitation", "dataContext");
    }

    private ImportLogger createImportLogger(final String repoName, AtomicLong warningCount, AtomicLong errorCount) {
        return new ImportLogger() {
            private AtomicLong lineCount = new AtomicLong(0);

            @Override
            public void info(LogContext ctx, String message) {
                log(ctx, message);
            }

            @Override
            public void warn(LogContext ctx, String message) {
                log(ctx, message, "warn");
                warningCount.incrementAndGet();
            }

            @Override
            public void severe(LogContext ctx, String message) {
                log(ctx, message, "error");
                errorCount.incrementAndGet();
            }

            private void log(LogContext ctx, String message, String level) {
                Integer maxLines = getMaxLines();

                if (maxLines == null || lineCount.get() < maxLines) {
                    log(ctx, message);
                }

                lineCount.incrementAndGet();
            }

            private void log(LogContext ctx, String msg) {
                if (ctx == null) {
                    log(msg);
                } else {
                    try {
                        String contextString = ctx.toString();
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode dataContext = mapper.readTree(contextString);
                        ObjectNode review = mapper.createObjectNode();
                        review.put("reviewId", reviewId);
                        review.put("reviewDate", DateUtil.printDate(dateFactory.getDate()));
                        review.put("reviewerName", getReviewerName());
                        review.put("reviewComment", msg);
                        review.put("namespace", repoName);
                        review.put("context", dataContext);

                        String reviewJsonString = mapper.writeValueAsString(review);
                        String archiveURI = getFindTermValue(dataContext, DatasetConstant.ARCHIVE_URI);
                        String catalogNumber = getFindTermValue(dataContext, "sourceCatalogNumber");
                        String collectionCode = getFindTermValue(dataContext, "sourceCollectionCode");
                        String collectionId = getFindTermValue(dataContext, "sourceCollectionId");
                        String institutionCode = getFindTermValue(dataContext, "sourceInstitutionCode");
                        String occurrenceId = getFindTermValue(dataContext, "sourceOccurrenceId");
                        String referenceUrl = getFindTermValue(dataContext, "referenceUrl");
                        String sourceCitation = getFindTermValue(dataContext, StudyImporterForTSV.STUDY_SOURCE_CITATION);
                        logReviewCommentWithReviewerInfo(getStdout(), repoName, msg, archiveURI, referenceUrl, institutionCode, collectionCode, collectionId, catalogNumber, occurrenceId, sourceCitation, reviewJsonString);
                    } catch (IOException e) {
                        log(e.getMessage());
                    }
                }
            }

            private void log(String msg) {
                String msgEscaped = CSVTSVUtil.escapeTSV(msg);
                PrintStream stdout = getStdout();
                logReviewCommentWithReviewerInfo(stdout, repoName, msgEscaped, "", "", "", "", "", "", "", "", "");
            }

        };
    }

    private void logReviewCommentWithReviewerInfo(PrintStream out, String... fields) {
        out.print('\n');
        Stream<String> enrichedFields = Stream.concat(Stream.of(reviewId, DateUtil.printDate(dateFactory.getDate()), getReviewerName())
                , Arrays.stream(fields));
        logReviewComment(out, enrichedFields.toArray());
    }

    private String getReviewerName() {
        return this.reviewerName;
    }

    private static void logReviewComment(PrintStream out, Object... fields) {
        out.print(String.format(LOG_FORMAT_STRING, fields));
    }

    public Integer getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(Integer maxLines) {
        this.maxLines = maxLines;
    }

    public void setDateFactory(DateFactory dateFactory) {
        this.dateFactory = dateFactory;
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
                int reportBatchSize = 10;
                int count = counter.get();
                ProgressUtil.logProgress(reportBatchSize, count, getProgressCursor());
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

        }
    }

    public static String getFindTermValue(JsonNode message, String termURI) {
        String termValue = "";
        if (message.has(termURI)) {
            termValue = message.get(termURI).getTextValue();
        }
        return termValue;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }


}
