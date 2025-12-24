package org.globalbioticinteractions.elton.cmd;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class ReviewReport {
    private final AtomicLong infoCounter;
    private final AtomicLong noteCounter;

    private final AtomicLong interactionCounter;
    private final String namespace;
    private final List<ReviewCommentType> desiredReviewCommentTypes;
    private final AtomicLong lineCount;
    private final String reviewId;
    private final DateFactory dateFactory;
    private final String reviewerName;
    private final String provenanceAnchor;

    ReviewReport(String namespace,
                 List<ReviewCommentType> desiredReviewCommentTypes,
                 String provenanceAnchor) {
        this.infoCounter = new AtomicLong(0);
        this.noteCounter = new AtomicLong(0);
        this.interactionCounter = new AtomicLong(0);
        this.namespace = namespace;
        this.desiredReviewCommentTypes = desiredReviewCommentTypes;
        this.lineCount = new AtomicLong(0);
        this.dateFactory = () -> new Date();
        this.reviewId = UUID.randomUUID().toString();
        this.reviewerName = CmdReview.REVIEWER_DEFAULT;
        this.provenanceAnchor = provenanceAnchor;
    }

    ReviewReport(AtomicLong infoCounter,
                 AtomicLong noteCounter,
                 String namespace,
                 List<ReviewCommentType> desiredReviewCommentTypes,
                 AtomicLong lineCount,
                 String reviewId,
                 DateFactory dateFactory,
                 String reviewerName,
                 AtomicLong interactionCounter) {
        this.infoCounter = infoCounter;
        this.noteCounter = noteCounter;
        this.interactionCounter = interactionCounter;
        this.namespace = namespace;
        this.desiredReviewCommentTypes = desiredReviewCommentTypes;
        this.lineCount = lineCount;
        this.dateFactory = dateFactory;
        this.reviewId = reviewId;
        this.reviewerName = reviewerName;
        this.provenanceAnchor = null;
    }

    public AtomicLong getInfoCounter() {
        return infoCounter;
    }

    public AtomicLong getNoteCounter() {
        return noteCounter;
    }

    public AtomicLong getInteractionCounter() {
        return interactionCounter;
    }

    public String getNamespace() {
        return namespace;
    }

    public List<ReviewCommentType> getDesiredReviewCommentTypes() {
        return desiredReviewCommentTypes;
    }

    public AtomicLong getLineCount() {
        return lineCount;
    }

    public String getReviewId() {
        return reviewId;
    }

    public DateFactory getDateFactory() {
        return dateFactory;
    }

    public String getReviewerName() {
        return reviewerName;
    }


    public String getProvenanceAnchor() {
        return provenanceAnchor;
    }
}
