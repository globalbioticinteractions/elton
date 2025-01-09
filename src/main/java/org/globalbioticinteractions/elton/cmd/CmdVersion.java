package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.VersionUtil;
import org.globalbioticinteractions.elton.Elton;
import picocli.CommandLine;

import java.io.PrintStream;

@CommandLine.Command (name = "version", description = "Print versions")
public class CmdVersion implements Runnable {

    @Override
    public void run() {
        PrintStream out = System.out;
        run(out);
    }

    void run(PrintStream out) {
        out.println("elton@" + Elton.getVersionString() + " preston@" + VersionUtil.getVersionString());
    }

}
