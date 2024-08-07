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
import org.eol.globi.util.ResourceServiceLocal;
import org.eol.globi.util.ResourceServiceLocalAndRemote;
import org.globalbioticinteractions.dataset.CitationUtil;
import org.globalbioticinteractions.dataset.Dataset;
import org.globalbioticinteractions.dataset.DatasetConstant;
import org.globalbioticinteractions.dataset.DatasetFactory;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.elton.Elton;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;
import org.globalbioticinteractions.elton.util.NodeFactoryNull;
import org.globalbioticinteractions.elton.util.ProgressCursorFactory;
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
    public static final String REVIEWER_DEFAULT = "GloBI automated reviewer (elton-" + Elton.getVersionString() + ")";

    @CommandLine.Option(names = {"-n", "--lines"}, description = "print first n number of lines")
    private Long maxLines = null;

    @CommandLine.Option(names = {"--type"}, description = "select desired review comments types: info,note,summary")

    private List<ReviewCommentType> desiredReviewCommentTypes = Arrays.asList(ReviewCommentType.values());

    private DateFactory dateFactory = Date::new;

    private String reviewerName = REVIEWER_DEFAULT;

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
                DatasetRegistry registryLocal = DatasetRegistryUtil.forLocalDir(
                        localNamespace,
                        getCacheDir(),
                        new ResourceServiceLocalAndRemote(factory, new File(getCacheDir()))
                );

                review(DatasetRegistryUtil.NAMESPACE_LOCAL, registryLocal, factory, shouldSkipHeader());
            }

            reviewCachedOrRemote(remoteNamespaces, factory);

        } catch (StudyImporterException e) {
            throw new RuntimeException(e);
        }
    }

    private void reviewCachedOrRemote(List<String> namespaces, InputStreamFactory inputStreamFactory) throws StudyImporterException {
        for (String namespace : namespaces) {
            review(namespace,
                    DatasetRegistryUtil.forCacheDir(
                            getCacheDir(),
                            new ResourceServiceLocal(inputStreamFactory)
                    ),
                    inputStreamFactory,
                    shouldSkipHeader()
            );
        }
    }

    private void review(String namespace, DatasetRegistry registry, InputStreamFactory inputStreamFactory, boolean shouldSkipHeader) throws StudyImporterException {
        ReviewReport report = createReport(namespace, CmdReview.this.reviewId, CmdReview.this.getReviewerName(), CmdReview.this.dateFactory);
        ReviewReportLogger logger = new ReviewReportLogger(report, getStdout(), getMaxLines(), getProgressCursorFactory());

        try {
            getStderr().print("creating review [" + namespace + "]... ");

            Dataset dataset = new DatasetFactory(
                    registry,
                    inputStreamFactory)
                    .datasetFor(namespace);

            if (!shouldSkipHeader) {
                logReviewHeader(getStdout());
            }

            NodeFactoryReview nodeFactory = new NodeFactoryReview(
                    report.getInteractionCounter(),
                    logger,
                    getProgressCursorFactory()
            );

            ParserFactoryLocal parserFactory = new ParserFactoryLocal(getClass());
            DatasetImporterForRegistry studyImporter = new DatasetImporterForRegistry(
                    parserFactory,
                    nodeFactory,
                    registry
            );
            studyImporter.setLogger(logger);
            File workDir = new File(getWorkDir());
            studyImporter.setWorkDir(workDir);
            DatasetImportUtil.importDataset(
                    null,
                    dataset,
                    nodeFactory,
                    studyImporter.getLogger(),
                    studyImporter.getGeoNamesService(),
                    workDir
            );

            if (report.getInteractionCounter().get() == 0) {
                logger.warn(null, "no interactions found");
            }
            getStderr().println("done.");
            log(null, dataset.getArchiveURI().toString(), ReviewCommentType.summary, report, getStdout());
        } catch (DatasetRegistryException e) {
            logger.warn(null, "no local repository at [" + getWorkDir().toString() + "]");
            getStderr().println("failed.");
            throw new StudyImporterException(e);
        } catch (Throwable e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintWriter(out));
            e.printStackTrace();
            logger.severe(null, new String(out.toByteArray()));
            throw new StudyImporterException(e);
        } finally {
            log(null, report.getInteractionCounter().get() + " interaction(s)", ReviewCommentType.summary, report, getStdout());
            log(null, report.getNoteCounter().get() + " note(s)", ReviewCommentType.summary, report, getStdout());
            log(null, report.getInfoCounter().get() + " info(s)", ReviewCommentType.summary, report, getStdout());
        }
        if (report.getInteractionCounter().get() == 0) {
            throw new StudyImporterException("No interactions found, nothing to review. Please check logs.");
        }
    }

    private ReviewReport createReport(String namespace, String reviewId1, String reviewerName1, DateFactory dateFactory1) {
        final AtomicLong noteCounter = new AtomicLong(0);
        final AtomicLong infoCounter = new AtomicLong(0);
        final AtomicLong interactionCounter = new AtomicLong(0);
        AtomicLong lineCount = new AtomicLong(0);

        return new ReviewReport(infoCounter, noteCounter, namespace, desiredReviewCommentTypes, lineCount,
                reviewId1, dateFactory1, reviewerName1, interactionCounter);
    }

    public static void logReviewHeader(PrintStream out) {
        logReviewComment(out, "reviewId", "reviewDate", "reviewer", "namespace", "reviewCommentType", "reviewComment", "archiveURI", "referenceUrl", "institutionCode", "collectionCode", "collectionId", "catalogNumber", "occurrenceId", "sourceCitation", "dataContext");
    }

    private static void logWithContext(LogContext ctx, String msg, ReviewCommentType commentType, String reviewId, DateFactory dateFactory, String namespace, String reviewerName, PrintStream stdout) {
        try {
            String contextString = ctx.toString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode dataContext = parseAndSortContext(contextString);
            ObjectNode review = mapper.createObjectNode();
            review.put("reviewId", reviewId);
            review.put("reviewDate", DateUtil.printDate(dateFactory.getDate()));
            review.put("reviewerName", reviewerName);
            review.put("reviewCommentType", commentType.getLabel());
            review.put("reviewComment", msg);
            review.put("namespace", namespace);
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
            logReviewCommentWithReviewerInfo(stdout, reviewId, dateFactory, reviewerName, namespace, commentType.getLabel(), msg, archiveURI, referenceUrl, institutionCode, collectionCode, collectionId, catalogNumber, occurrenceId, sourceCitation, reviewJsonString);
        } catch (IOException e) {
            CmdReview.log(e.getMessage(), namespace, stdout, ReviewCommentType.note.getLabel(), reviewId, dateFactory, reviewerName);
        }
    }


    public static void log(LogContext ctx, String msg, ReviewCommentType commentType, ReviewReport report, PrintStream stdout) {
        if (report.getDesiredReviewCommentTypes().contains(commentType)) {
            if (ctx == null) {
                CmdReview.log(msg, report.getNamespace(), stdout, commentType.getLabel(), report.getReviewId(), report.getDateFactory(), report.getReviewerName());
            } else {
                logWithContext(ctx, msg, commentType, report.getReviewId(), report.getDateFactory(), report.getNamespace(), report.getReviewerName(), stdout);
            }
        }
    }


    private static void log(String msg, String namespace, PrintStream stdout, String label, String reviewId, DateFactory dateFactory, String reviewerName) {
        logReviewCommentWithReviewerInfo(
                stdout,
                reviewId,
                dateFactory,
                reviewerName,
                namespace,
                label,
                CSVTSVUtil.escapeTSV(msg), "", "", "", "", "", "", "", "", "");
    }


    private static void logReviewCommentWithReviewerInfo(PrintStream out, String reviewId, DateFactory dateFactory, String reviewerName, String... fields) {
        out.print('\n');
        Stream<String> enrichedFields = Stream.concat(
                Stream.of(
                        reviewId,
                        DateUtil.printDate(dateFactory.getDate()),
                        reviewerName
                )
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

    public Long getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(Long maxLines) {
        this.maxLines = maxLines;
    }

    public void setDateFactory(DateFactory dateFactory) {
        this.dateFactory = dateFactory;
    }

    public void setDesiredReviewCommentTypes(List<ReviewCommentType> commentTypes) {
        desiredReviewCommentTypes = commentTypes;
    }

    public static class NodeFactoryReview extends NodeFactoryNull {
        final AtomicLong counter;
        final ImportLogger importLogger;

        public NodeFactoryReview(AtomicLong counter, ImportLogger importLogger) {
            this(counter, importLogger, new ProgressCursorFactoryNoop());
        }

        public NodeFactoryReview(AtomicLong counter, ImportLogger importLogger, ProgressCursorFactory progressCursorFactory) {
            this.counter = counter;
            this.importLogger = importLogger;
            this.progressCursorFactory = progressCursorFactory;
        }

        private ProgressCursorFactory progressCursorFactory;
        final Specimen specimen = new SpecimenNull() {
            @Override
            public void interactsWith(Specimen target, InteractType type, Location centroid) {
                long count = counter.get();
                ProgressUtil.logProgress(ProgressUtil.SPECIMEN_CREATED_PROGRESS_BATCH_SIZE, count, progressCursorFactory.createProgressCursor());
                counter.getAndIncrement();
            }
        };

        @Override
        public Dataset getOrCreateDataset(Dataset dataset) {
            String citationString = CitationUtil.citationFor(dataset);
            if (StringUtils.startsWith(citationString, "<")
                    && StringUtils.endsWith(citationString, ">")) {
                importLogger.warn(null, "no citation found for dataset at [" + dataset.getArchiveURI() + "]");
            }
            return super.getOrCreateDataset(dataset);
        }


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

}
