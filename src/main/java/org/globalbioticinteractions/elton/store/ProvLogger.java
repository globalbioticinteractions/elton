package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.ActivityUtil;
import bio.guoda.preston.process.StatementListener;
import bio.guoda.preston.process.StatementsEmitterAdapter;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.Quad;
import org.globalbioticinteractions.cache.ProvenanceLog;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class ProvLogger extends ProvLoggerWithClock {

    private StatementListener listener;

    public ProvLogger(StatementListener listener) {
        super(listener, RefNodeFactory::nowDateTimeLiteral);
    }

}
