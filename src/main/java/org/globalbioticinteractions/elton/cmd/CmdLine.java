package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CmdLine {
    private final static Log LOG = LogFactory.getLog(CmdLine.class);

    public static void run(JCommander actual) {
        if (actual == null) {
            throw new MissingCommandException("no command provided");
        } else if (!(actual.getObjects().get(0) instanceof Runnable)) {
            throw new MissingCommandException("invalid command provided");
        } else {
            Object cmdObject = actual.getObjects().get(0);
            ((Runnable) cmdObject).run();
        }
    }


    public static void run(String[] args) throws Throwable {
        try {
            JCommander jc = new CmdLine().buildCommander();
            jc.parse(args);
            CmdLine.run(jc.getCommands().get(jc.getParsedCommand()));
        } catch (MissingCommandException ex) {
            System.err.println(ex.getMessage());
            new CmdUsage().run();
        }
    }

    JCommander buildCommander() {
        return JCommander.newBuilder()
                .addObject(new CmdUsage())
                .addCommand("usage", new CmdUsage(), "help")
                .addCommand("list", new CmdList())
                .addCommand("update", new CmdUpdate())
                .addCommand("names", new CmdNames())
                .addCommand("interactions", new CmdInteractions())
                .addCommand("nanopubs", new CmdNanoPubs())
                .addCommand("check", new CmdCheck())
                .addCommand("version", new CmdVersion())
                .build();
    }

}