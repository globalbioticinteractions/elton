package org.globalbioticinteractions.elton;

/*
    Elton - a GloBI commandline tool to help access species interaction data.
 */

import bio.guoda.preston.RefNodeFactory;
import bio.guoda.preston.cmd.ActivityContext;
import bio.guoda.preston.process.ActivityUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Quad;
import org.globalbioticinteractions.elton.cmd.CmdStream;
import org.globalbioticinteractions.elton.cmd.CmdDatasets;
import org.globalbioticinteractions.elton.cmd.CmdGet;
import org.globalbioticinteractions.elton.cmd.CmdInit;
import org.globalbioticinteractions.elton.cmd.CmdInstallManual;
import org.globalbioticinteractions.elton.cmd.CmdInteractions;
import org.globalbioticinteractions.elton.cmd.CmdList;
import org.globalbioticinteractions.elton.cmd.CmdLog;
import org.globalbioticinteractions.elton.cmd.CmdNames;
import org.globalbioticinteractions.elton.cmd.CmdNanoPubs;
import org.globalbioticinteractions.elton.cmd.CmdReview;
import org.globalbioticinteractions.elton.cmd.CmdSupportedRegistries;
import org.globalbioticinteractions.elton.cmd.CmdUpdate;
import org.globalbioticinteractions.elton.cmd.CmdVersion;
import picocli.CommandLine;
import picocli.codegen.docgen.manpage.ManPageGenerator;

import java.util.List;

import static java.lang.System.exit;

@CommandLine.Command(name = "elton",
        versionProvider = Elton.class,
        subcommands = {
                CmdInit.class,
                CmdInteractions.class,
                CmdLog.class,
                CmdGet.class,
                CmdNames.class,
                CmdReview.class,
                CmdDatasets.class,
                CmdSupportedRegistries.class,
                CmdVersion.class,
                CmdNanoPubs.class,
                CmdList.class,
                CmdStream.class,
                CmdUpdate.class,
                CmdInstallManual.class,
                ManPageGenerator.class,
                CommandLine.HelpCommand.class
        },
        description = "discover existing species interaction datasets",
        mixinStandardHelpOptions = true)

public class Elton implements CommandLine.IVersionProvider {

    private static final String ELTON_DESCRIPTION = "Elton helps to access, review and index existing species interaction datasets.";
    private static final IRI ELTON_CONCEPT_DOI = RefNodeFactory.toIRI("https://zenodo.org/doi/10.5281/zenodo.998263");

    public static List<Quad> getEltonDescription(ActivityContext ctx) {
        String citationString = "Jorrit Poelen, Tobias Kuhn & Katrin Leinweber. (2017/2024). globalbioticinteractions/elton: "
                + getVersionString()
                + ". Zenodo. "
                + ELTON_CONCEPT_DOI.getIRIString();

        return ActivityUtil.generateSoftwareAgentProcessDescription(
                ctx,
                ELTON_CONCEPT_DOI,
                ELTON_CONCEPT_DOI,
                citationString,
                ELTON_DESCRIPTION);
    }

    public String[] getVersion() {
        return new String[]{getVersionString()};
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
