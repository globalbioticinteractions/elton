package org.globalbioticinteractions.elton.cmd;

import org.globalbioticinteractions.elton.Elton;
import picocli.CommandLine;

@CommandLine.Command (name = "version", description = "Print version")
public class CmdVersion implements Runnable {

    @Override
    public void run() {
        System.out.println(Elton.getVersionString());
    }

}
