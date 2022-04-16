package org.globalbioticinteractions.elton.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.eol.globi.data.CharsetConstant;
import org.eol.globi.data.DatasetImporterForRegistry;
import org.eol.globi.data.DatasetImporterForTSV;
import org.eol.globi.data.ImportLogger;
import org.eol.globi.data.NodeFactoryException;
import org.eol.globi.data.ParserFactoryLocal;
import org.eol.globi.data.StudyImporterException;
import org.eol.globi.domain.InteractType;
import org.eol.globi.domain.Interaction;
import org.eol.globi.domain.Location;
import org.eol.globi.domain.LogContext;
import org.eol.globi.domain.RelTypes;
import org.eol.globi.domain.Specimen;
import org.eol.globi.domain.Study;
import org.eol.globi.domain.Taxon;
import org.eol.globi.util.CSVTSVUtil;
import org.eol.globi.util.DatasetImportUtil;
import org.eol.globi.util.DateUtil;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.dataset.CitationUtil;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetConstant;
import org.globalbioticinteractions.dataset.DatasetFactory;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.elton.Elton;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.ProgressUtil;
import org.globalbioticinteractions.elton.util.SpecimenNull;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.eol.globi.data.DatasetImporterForTSV.SOURCE_CATALOG_NUMBER;
import static org.eol.globi.data.DatasetImporterForTSV.SOURCE_COLLECTION_CODE;
import static org.eol.globi.data.DatasetImporterForTSV.SOURCE_COLLECTION_ID;
import static org.eol.globi.data.DatasetImporterForTSV.SOURCE_INSTITUTION_CODE;
import static org.eol.globi.data.DatasetImporterForTSV.SOURCE_OCCURRENCE_ID;

@CommandLine.Command(
        name = "review",
        aliases = {"test", "check"},
        description = "Review Datasets. If no namespace is provided the local workdir is used."
)
public class CmdReview extends CmdTabularWriterParams {
    public static final String LOG_FORMAT_STRING = "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s";
    public static final long LOG_NUMBER_OF_FIELDS = Arrays.stream(LOG_FORMAT_STRING.split("\t")).filter(x -> x.equals("%s")).count();

    @CommandLine.Option(names = {"-n", "--lines"}, description = "print first n number of lines")
    private Integer maxLines = null;

    @CommandLine.Option(names = {"--type"}, description = "select desired review comments types: info,note,summary")

    private List<ReviewCommentType> desiredReviewCommentTypes = Arrays.asList(ReviewCommentType.values());

    private DateFactory dateFactory = Date::new;

    private String reviewerName = "GloBI automated reviewer (elton-" + Elton.getVersionString() + ")";

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

            reviewCachedOrRemote(remoteNamespaces, factory);

        } catch (StudyImporterException e) {
            throw new RuntimeException(e);
        }
    }

    private void reviewCachedOrRemote(List<String> namespaces, InputStreamFactory inputStreamFactory) throws StudyImporterException {
        for (String namespace : namespaces) {
            review(namespace, DatasetRegistryUtil.forCacheDir(getCacheDir(), inputStreamFactory), inputStreamFactory);
        }
    }

    private void reviewLocal(URI workDir, InputStreamFactory inputStreamFactory) throws StudyImporterException {
        DatasetRegistry registryLocal = DatasetRegistryUtil.forLocalDir(workDir, getCacheDir(), inputStreamFactory);
        review("local", registryLocal, inputStreamFactory);
    }

    private void review(String repoName, DatasetRegistry registry, InputStreamFactory inputStreamFactory) throws StudyImporterException {
        final AtomicLong noteCounter = new AtomicLong(0);
        final AtomicLong infoCounter = new AtomicLong(0);

        ParserFactoryLocal parserFactory = new ParserFactoryLocal();
        AtomicInteger interactionCounter = new AtomicInteger(0);
        ReviewReportLogger reviewReportLogger = createReviewReportLogger(repoName, noteCounter, infoCounter);

        NodeFactoryLogging nodeFactory = new NodeFactoryLogging(interactionCounter, reviewReportLogger);
        DatasetImporterForRegistry studyImporter = new DatasetImporterForRegistry(parserFactory, nodeFactory, registry);
        studyImporter.setLogger(reviewReportLogger);

        try {
            Dataset dataset = new DatasetFactory(
                    registry,
                    inputStreamFactory)
                    .datasetFor(repoName);

            String citationString = CitationUtil.citationFor(dataset);
            if (StringUtils.startsWith(citationString, "<")
                    && StringUtils.endsWith(citationString, ">")) {
                reviewReportLogger.warn(null, "no citation found for dataset at [" + dataset.getArchiveURI() + "]");
            }
            nodeFactory.getOrCreateDataset(dataset);
            getStderr().print("creating review [" + repoName + "]... ");
            if (!shouldSkipHeader()) {
                logHeader(getStdout());
            }

            DatasetImportUtil.importDataset(
                    null,
                    dataset,
                    nodeFactory,
                    studyImporter.getLogger(),
                    studyImporter.getGeoNamesService()
            );

            if (interactionCounter.get() == 0) {
                reviewReportLogger.warn(null, "no interactions found");
            }
            getStderr().println("done.");
            reviewReportLogger.log(null, dataset.getArchiveURI().toString(), ReviewCommentType.summary);
        } catch (DatasetRegistryException e) {
            reviewReportLogger.warn(null, "no local repository at [" + getWorkDir().toString() + "]");
            getStderr().println("failed.");
            throw new StudyImporterException(e);
        } catch (Throwable e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintWriter(out));
            e.printStackTrace();
            reviewReportLogger.severe(null, new String(out.toByteArray()));
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

    static void logReviewComment(PrintStream out, Object... fields) {
        if (fields.length != LOG_NUMBER_OF_FIELDS) {
            throw new IllegalArgumentException("not enough log fields: need [" + LOG_NUMBER_OF_FIELDS + "], but found [" + fields.length + "] in [" + StringUtils.join(fields, CharsetConstant.SEPARATOR));
        }
        out.print(String.format(LOG_FORMAT_STRING, Stream.of(fields).map(x -> x == null ? "" : CSVTSVUtil.escapeTSV(x.toString())).toArray()));
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

        public NodeFactoryLogging(AtomicInteger counter, ImportLogger importLogger) {
            this.counter = counter;
            this.importLogger = importLogger;
        }

        final Specimen specimen = new SpecimenNull() {
            @Override
            public void interactsWith(Specimen target, InteractType type, Location centroid) {
                int count = counter.get();
                ProgressUtil.logProgress(ProgressUtil.SPECIMEN_CREATED_PROGRESS_BATCH_SIZE, count, getProgressCursorFactory().createProgressCursor());
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
        if (message.has(termURI) && message.hasNonNull(termURI)) {
            termValue = message.get(termURI).asText();
        }
        return StringUtils.isBlank(termValue) ? "" : termValue;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }


    static JsonNode parseAndSortContext(String content) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dataContext = objectMapper.readTree(content);
        return dataContext.isObject()
                ? sortJsonObjByPropertyNames(objectMapper, dataContext)
                : dataContext;
    }

    private static ObjectNode sortJsonObjByPropertyNames(ObjectMapper mapper, JsonNode dataContext) {
        final ObjectNode dataContextSorted = mapper.createObjectNode();
        final Spliterator<Map.Entry<String, JsonNode>> fields
                = Spliterators.spliteratorUnknownSize(dataContext.fields(), 0);

        StreamSupport
                .stream(fields, false)
                .sorted((o1, o2) -> StringUtils.compare(o1.getKey(), o2.getKey()))
                .forEach(kv -> dataContextSorted.set(kv.getKey(), kv.getValue()));

        return dataContextSorted;
    }

    private class ReviewReportLogger implements ImportLogger {
        private final AtomicLong infoCounter;
        private final AtomicLong noteCounter;
        private final String repoName;
        private AtomicLong lineCount;

        public ReviewReportLogger(AtomicLong infoCounter, AtomicLong noteCounter, String repoName, List<ReviewCommentType> desiredReviewCommentTypes) {
            this.infoCounter = infoCounter;
            this.noteCounter = noteCounter;
            this.repoName = repoName;
            lineCount = new AtomicLong(0);
            lineCount = new AtomicLong(0);
        }


        @Override
        public void info(LogContext ctx, String message) {
            logWithCounter(ctx, message, ReviewCommentType.info);
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

            long l = lineCount.incrementAndGet();
            if (l % ProgressUtil.LOG_ACTIVITY_PROGRESS_BATCH_SIZE == 0) {
                getProgressCursorFactory().createProgressCursor().increment();
            }
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
                JsonNode dataContext = parseAndSortContext(contextString);
                ObjectNode review = mapper.createObjectNode();
                review.put("reviewId", reviewId);
                review.put("reviewDate", DateUtil.printDate(dateFactory.getDate()));
                review.put("reviewerName", getReviewerName());
                review.put("reviewCommentType", commentType.getLabel());
                review.put("reviewComment", msg);
                review.put("namespace", repoName);
                review.set("context", dataContext);

                String reviewJsonString = mapper.writeValueAsString(review);
                String archiveURI = getFindTermValueOrEmptyString(dataContext, DatasetConstant.ARCHIVE_URI);
                String catalogNumber = getFindTermValueOrEmptyString(dataContext, SOURCE_CATALOG_NUMBER);
                String collectionCode = getFindTermValueOrEmptyString(dataContext, SOURCE_COLLECTION_CODE);
                String collectionId = getFindTermValueOrEmptyString(dataContext, SOURCE_COLLECTION_ID);
                String institutionCode = getFindTermValueOrEmptyString(dataContext, SOURCE_INSTITUTION_CODE);
                String occurrenceId = getFindTermValueOrEmptyString(dataContext, SOURCE_OCCURRENCE_ID);
                String referenceUrl = getFindTermValueOrEmptyString(dataContext, "referenceUrl");
                String sourceCitation = getFindTermValueOrEmptyString(dataContext, DatasetImporterForTSV.STUDY_SOURCE_CITATION);
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
