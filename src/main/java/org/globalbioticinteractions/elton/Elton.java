package org.globalbioticinteractions.elton;

/*
    Elton - a GloBI commandline tool to help access species interaction data.
 */

import org.apache.commons.lang.StringUtils;
import org.globalbioticinteractions.elton.cmd.CmdDatasets;
import org.globalbioticinteractions.elton.cmd.CmdInit;
import org.globalbioticinteractions.elton.cmd.CmdInstallManual;
import org.globalbioticinteractions.elton.cmd.CmdInteractions;
import org.globalbioticinteractions.elton.cmd.CmdList;
import org.globalbioticinteractions.elton.cmd.CmdNames;
import org.globalbioticinteractions.elton.cmd.CmdNanoPubs;
import org.globalbioticinteractions.elton.cmd.CmdReview;
import org.globalbioticinteractions.elton.cmd.CmdSupportedRegistries;
import org.globalbioticinteractions.elton.cmd.CmdUpdate;
import org.globalbioticinteractions.elton.cmd.CmdVersion;
import picocli.CommandLine;
import picocli.codegen.docgen.manpage.ManPageGenerator;

import static java.lang.System.exit;

@CommandLine.Command(name = "elton",
        versionProvider = Elton.class,
        subcommands = {
                CmdInit.class,
                CmdInteractions.class,
                CmdNames.class,
                CmdReview.class,
                CmdDatasets.class,
                CmdSupportedRegistries.class,
                CmdVersion.class,
                CmdNanoPubs.class,
                CmdList.class,
                CmdUpdate.class,
                CmdInstallManual.class,
                ManPageGenerator.class,
                CommandLine.HelpCommand.class
        },
        description = "elton - access to species interaction datasets",
        mixinStandardHelpOptions = true)

public class Elton implements CommandLine.IVersionProvider {

    public String[] getVersion() {
        return new String[] {getVersionString()};
    }

    public static String getVersionString() {
        String version = Elton.class.getPackage().getImplementationVersion();
        return StringUtils.isBlank(version) ? "dev" : version;
    }


    public static void main(String[] args) {
        try {
            int exitCode = run(args);
            System.exit(exitCode);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            exit(1);
        }
    }

    public static int run(String[] args) {
        return new CommandLine(new Elton()).execute(args);
    }

}
