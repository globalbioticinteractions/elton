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
import org.globalbioticinteractions.elton.util.ProgressCursor;
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

import static org.eol.globi.data.StudyImporterForTSV.SOURCE_CATALOG_NUMBER;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_COLLECTION_CODE;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_COLLECTION_ID;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_INSTITUTION_CODE;
import static org.eol.globi.data.StudyImporterForTSV.SOURCE_OCCURRENCE_ID;

@Parameters(separators = "= ", commandDescription = "Review Datasets. If no namespace is provided the local workdir is used.")
public class CmdReview extends CmdDefaultParams {
    private final static Log LOG = LogFactory.getLog(CmdReview.class);
    public static final String LOG_FORMAT_STRING = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s";

    @Parameter(names = {"-n", "--lines"}, description = "print first n number of lines")
    private Integer maxLines = null;

    @Parameter(names = {"--type"}, description = "select desired review comments types: info,note,summary", converter = ReviewCommentTypeConverter.class)
    private List<ReviewCommentType> desiredReviewCommentTypes = Arrays.asList(ReviewCommentType.values());

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
                reviewLocal(localNamespace, factory);
            }

            checkCacheOrRemote(remoteNamespaces, factory);

        } catch (StudyImporterException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkCacheOrRemote(List<String> namespaces, InputStreamFactory inputStreamFactory) throws StudyImporterException {
        for (String namespace : namespaces) {
            review(namespace, DatasetRegistryUtil.forCacheDir(getCacheDir(), inputStreamFactory), inputStreamFactory);
        }
    }

    private void reviewLocal(URI workDir, InputStreamFactory inputStreamFactory) throws StudyImporterException {
        DatasetRegistry finderLocal = DatasetRegistryUtil.forLocalDir(workDir, getTmpDir(), inputStreamFactory);
        review("local", finderLocal, inputStreamFactory);
    }

    private void review(String repoName, DatasetRegistry finder, InputStreamFactory inputStreamFactory) throws StudyImporterException {
        final AtomicLong noteCounter = new AtomicLong(0);
        final AtomicLong infoCounter = new AtomicLong(0);

        ParserFactoryLocal parserFactory = new ParserFactoryLocal();
        AtomicInteger interactionCounter = new AtomicInteger(0);
        ReviewReportLogger reviewReportLogger = createReviewReportLogger(repoName, noteCounter, infoCounter);

        NodeFactoryLogging nodeFactory = new NodeFactoryLogging(interactionCounter, reviewReportLogger);
        StudyImporterForRegistry studyImporter = new StudyImporterForRegistry(parserFactory, nodeFactory, finder);
        studyImporter.setLogger(reviewReportLogger);

        try {
            Dataset dataset = new DatasetFactory(
                    finder,
                    inputStreamFactory)
                    .datasetFor(repoName);

            if (StringUtils.isBlank(CitationUtil.citationOrDefaultFor(dataset, ""))) {
                reviewReportLogger.warn(null, "no citation found for dataset at [" + dataset.getArchiveURI() + "]");
            }
            nodeFactory.getOrCreateDataset(dataset);
            getStderr().print("creating review [" + repoName + "]... ");
            logHeader(getStdout());
            studyImporter.importData(dataset);
            if (interactionCounter.get() == 0) {
                reviewReportLogger.warn(null, "no interactions found");
            }
            getStderr().println("done.");
            reviewReportLogger.log(null, dataset.getArchiveURI().toString(), ReviewCommentType.summary);
        } catch (DatasetFinderException e) {
            reviewReportLogger.warn(null, "no local repository at [" + getWorkDir().toString() + "]");
            getStderr().println("failed.");
            throw new StudyImporterException(e);
        } catch (Throwable e) {
            e.printStackTrace();
            reviewReportLogger.severe(null, e.getMessage());
            throw new StudyImporterException(e);
        } finally {
            reviewReportLogger.log(null, interactionCounter.get() + " interaction(s)", ReviewCommentType.summary);
            reviewReportLogger.log(null, noteCounter.get() + " note(s)", ReviewCommentType.summary);
            reviewReportLogger.log(null, infoCounter.get() + " info(s)", ReviewCommentType.summary);
        }
        if (interactionCounter.get() == 0) {
            throw new StudyImporterException("No interactions found, nothing to review. Please check logs.");
        }
    }

    private void logHeader(PrintStream out) {
        logReviewComment(out, "reviewId", "reviewDate", "reviewer", "namespace", "reviewCommentType", "reviewComment", "archiveURI", "referenceUrl", "institutionCode", "collectionCode", "collectionId", "catalogNumber", "occurrenceId", "sourceCitation", "dataContext");
    }

    private ReviewReportLogger createReviewReportLogger(final String repoName, AtomicLong noteCounter, AtomicLong infoCounter) {
        return new ReviewReportLogger(infoCounter, noteCounter, repoName, desiredReviewCommentTypes);
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

    public void setDesiredReviewCommentTypes(List<ReviewCommentType> commentTypes) {
        desiredReviewCommentTypes = commentTypes;
    }

    private class NodeFactoryLogging extends NodeFactoryNull {
        final AtomicInteger counter;
        final ImportLogger importLogger;
        final ProgressCursor cursor = getProgressCursorFactory().createProgressCursor();

        public NodeFactoryLogging(AtomicInteger counter, ImportLogger importLogger) {
            this.counter = counter;
            this.importLogger = importLogger;
        }

        final Specimen specimen = new SpecimenNull() {
            @Override
            public void interactsWith(Specimen target, InteractType type, Location centroid) {
                int reportBatchSize = 10;
                int count = counter.get();
                ProgressUtil.logProgress(reportBatchSize, count, cursor);
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

    public static String getFindTermValueOrEmptyString(JsonNode message, String termURI) {
        String termValue = "";
        if (message.has(termURI)) {
            termValue = message.get(termURI).getTextValue();
        }
        return StringUtils.isBlank(termValue) ? "" : termValue;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }


    private class ReviewReportLogger implements ImportLogger {
        private final AtomicLong infoCounter;
        private final AtomicLong noteCounter;
        private final String repoName;
        private final List<ReviewCommentType> reviewTypes;
        private AtomicLong lineCount;

        public ReviewReportLogger(AtomicLong infoCounter, AtomicLong noteCounter, String repoName, List<ReviewCommentType> desiredReviewCommentTypes) {
            this.infoCounter = infoCounter;
            this.noteCounter = noteCounter;
            this.repoName = repoName;
            lineCount = new AtomicLong(0);
            this.reviewTypes = desiredReviewCommentTypes;
        }


        @Override
        public void info(LogContext ctx, String message) {
            log(ctx, message, ReviewCommentType.info);
            infoCounter.incrementAndGet();
        }

        @Override
        public void warn(LogContext ctx, String message) {
            logWithCounter(ctx, message, ReviewCommentType.note);
            noteCounter.incrementAndGet();
        }

        @Override
        public void severe(LogContext ctx, String message) {
            logWithCounter(ctx, message, ReviewCommentType.note);
            noteCounter.incrementAndGet();
        }

        private void logWithCounter(LogContext ctx, String message, ReviewCommentType commentType) {
            Integer maxLines = getMaxLines();

            if (maxLines == null || lineCount.get() < maxLines) {
                log(ctx, message, commentType);
            }

            lineCount.incrementAndGet();
        }

        public void log(LogContext ctx, String msg, ReviewCommentType commentType) {
            if (desiredReviewCommentTypes.contains(commentType)) {
                if (ctx == null) {
                    log(msg, commentType);
                } else {
                    logWithContext(ctx, msg, commentType);
                }
            }
        }

        private void logWithContext(LogContext ctx, String msg, ReviewCommentType commentType) {
            try {
                String contextString = ctx.toString();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode dataContext = mapper.readTree(contextString);
                ObjectNode review = mapper.createObjectNode();
                review.put("reviewId", reviewId);
                review.put("reviewDate", DateUtil.printDate(dateFactory.getDate()));
                review.put("reviewerName", getReviewerName());
                review.put("reviewCommentType", commentType.getLabel());
                review.put("reviewComment", msg);
                review.put("namespace", repoName);
                review.put("context", dataContext);

                String reviewJsonString = mapper.writeValueAsString(review);
                String archiveURI = getFindTermValueOrEmptyString(dataContext, DatasetConstant.ARCHIVE_URI);
                String catalogNumber = getFindTermValueOrEmptyString(dataContext, SOURCE_CATALOG_NUMBER);
                String collectionCode = getFindTermValueOrEmptyString(dataContext, SOURCE_COLLECTION_CODE);
                String collectionId = getFindTermValueOrEmptyString(dataContext, SOURCE_COLLECTION_ID);
                String institutionCode = getFindTermValueOrEmptyString(dataContext, SOURCE_INSTITUTION_CODE);
                String occurrenceId = getFindTermValueOrEmptyString(dataContext, SOURCE_OCCURRENCE_ID);
                String referenceUrl = getFindTermValueOrEmptyString(dataContext, "referenceUrl");
                String sourceCitation = getFindTermValueOrEmptyString(dataContext, StudyImporterForTSV.STUDY_SOURCE_CITATION);
                logReviewCommentWithReviewerInfo(getStdout(), repoName, commentType.getLabel(), msg, archiveURI, referenceUrl, institutionCode, collectionCode, collectionId, catalogNumber, occurrenceId, sourceCitation, reviewJsonString);
            } catch (IOException e) {
                log(e.getMessage(), ReviewCommentType.note);
            }
        }

        private void log(String msg, ReviewCommentType commentType) {
            String msgEscaped = CSVTSVUtil.escapeTSV(msg);
            PrintStream stdout = getStdout();
            logReviewCommentWithReviewerInfo(stdout, repoName, commentType.getLabel(), msgEscaped, "", "", "", "", "", "", "", "", "");
        }

    }
}
