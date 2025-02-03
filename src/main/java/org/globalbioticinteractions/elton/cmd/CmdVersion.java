package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.VersionUtil;
import org.globalbioticinteractions.elton.Elton;
import picocli.CommandLine;

import java.io.PrintStream;

@CommandLine.Command (name = "version", description = "Print versions")
public class CmdVersion implements Runnable {

    @CommandLine.Option(names = {"--verbose"},
            description = "include versions of libraries used by Elton also"
    )
    private boolean verbose = false;


    @Override
    public void run() {
        PrintStream out = System.out;
        run(out);
    }

    void run(PrintStream out) {
        String verboseVersion = "elton@" + Elton.getVersionString() + " preston@" + VersionUtil.getVersionString();
        out.println(verbose ? verboseVersion : Elton.getVersionString());
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
