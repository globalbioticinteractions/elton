package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import org.globalbioticinteractions.elton.Elton;

@Parameters(separators = "= ", commandDescription = "Show Version")
public class CmdVersion implements Runnable {

    @Override
    public void run() {
        System.out.println(Elton.getVersion());
    }

}
