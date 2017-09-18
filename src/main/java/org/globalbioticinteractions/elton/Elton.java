package org.globalbioticinteractions.elton;

/*
    Elton - a GloBI commandline tool to help access species interaction data.
 */

import org.globalbioticinteractions.elton.cmd.CmdLine;

import static java.lang.System.exit;

public class Elton {
    public static void main(String[] args) {
        try {
            CmdLine.run(args);
            exit(0);
        } catch (Throwable t) {
            exit(1);
        }
    }
}
