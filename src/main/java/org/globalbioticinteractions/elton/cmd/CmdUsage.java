package org.globalbioticinteractions.elton.cmd;


import com.beust.jcommander.Parameters;

@Parameters(commandDescription = "Shows usage")
public class CmdUsage implements Runnable {

    @Override
    public void run() {
        StringBuilder out = new StringBuilder();
        new CmdLine().buildCommander().usage(out);
        System.err.append(out.toString());
    }
}
