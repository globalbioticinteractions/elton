package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.process.StatementsEmitterAdapter;
import org.apache.commons.rdf.api.Quad;

public class LoggingEmitter extends StatementsEmitterAdapter {
    private final CmdDefaultParams out;

    LoggingEmitter(CmdDefaultParams out) {
        this.out = out;
    }

    @Override
    public void emit(Quad statement) {
        out.getStdout().println(statement.toString());
    }
}
