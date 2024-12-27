package org.globalbioticinteractions.elton.store;

import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.process.StatementListener;

public class ProvLogger extends ProvLoggerWithClock {

    public ProvLogger(StatementListener listener) {
        super(listener, RefNodeFactory::nowDateTimeLiteral);
    }

}
