package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.RefNodeConstants;
import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.StatementListener;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static bio.guoda.preston.RefNodeConstants.HAS_VERSION;

public class ActivityListenerImpl implements ActivityListener {
    private final StatementListener listener;
    private final Supplier<Literal> clock;

    public ActivityListenerImpl(Literal startTime, Literal endTime, StatementListener listener) {
        this.listener = listener;
        this.clock = new Supplier<Literal>() {
            private AtomicInteger count = new AtomicInteger(0);
            @Override
            public Literal get() {
                int callNumber = count.getAndIncrement();
                return callNumber == 0 ? startTime : endTime;
            }
        };
    }

    @Override
    public void onStarted(IRI parentActivitiyId, IRI activityId, IRI request) {
        listener.on(RefNodeFactory.toStatement(activityId, activityId, RefNodeConstants.IS_A, RefNodeConstants.GENERATION));
        listener.on(RefNodeFactory.toStatement(activityId, activityId, RefNodeConstants.WAS_INFORMED_BY, parentActivitiyId));
        listener.on(RefNodeFactory.toStatement(activityId, activityId, RefNodeConstants.STARTED_AT_TIME, clock.get()));
    }

    @Override
    public void onCompleted(IRI parentActivityId, IRI activityId, IRI request, IRI response, URI localPathOfResponseData) {
        listener.on(RefNodeFactory.toStatement(activityId, response, RefNodeConstants.WAS_GENERATED_BY, activityId));
        listener.on(RefNodeFactory.toStatement(activityId, response, RefNodeConstants.QUALIFIED_GENERATION, activityId));
        listener.on(RefNodeFactory.toStatement(activityId, activityId, RefNodeConstants.GENERATED_AT_TIME, clock.get()));
        listener.on(RefNodeFactory.toStatement(activityId, activityId, RefNodeConstants.IS_A, RefNodeConstants.GENERATION));
        listener.on(RefNodeFactory.toStatement(activityId, activityId, RefNodeConstants.USED, request));
        listener.on(RefNodeFactory.toStatement(activityId, request, RefNodeConstants.HAS_VERSION, response));
        listener.on(RefNodeFactory.toStatement(activityId, activityId, RefNodeConstants.ENDED_AT_TIME, clock.get()));
    }
}
