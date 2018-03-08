package org.globalbioticinteractions.elton;

/*
    Elton - a GloBI commandline tool to help access species interaction data.
 */

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eol.globi.Version;
import org.globalbioticinteractions.elton.cmd.CmdLine;

import static java.lang.System.exit;

public class Elton {
    private static final Log LOG = LogFactory.getLog(Elton.class);

    public static void main(String[] args) {
        try {
            CmdLine.run(args);
            exit(0);
        } catch (Throwable t) {
            exit(1);
        }
    }

    public static String getVersion() {
        String version = Elton.class.getPackage().getImplementationVersion();
        return StringUtils.isBlank(version) ? "dev" : version;
    }

}
