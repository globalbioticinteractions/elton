package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.JCommander;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CmdLine {
    private final static Log LOG = LogFactory.getLog(CmdLine.class);

    public static void run(JCommander actual) {
        if (null != actual && actual.getObjects().get(0) instanceof Runnable) {
            ((Runnable) actual.getObjects().get(0)).run();
        }
    }

    public static void run(String[] args) {
        JCommander jc = new CmdLine().buildCommander();
        try {
            jc.parse(args);
            CmdLine.run(jc.getCommands().get(jc.getParsedCommand()));
        } catch (Exception ex) {
            LOG.error("unexpected exception", ex);
            StringBuilder out = new StringBuilder();
            jc.usage(out);
            System.err.append(out.toString());
        }
    }

    public class CommandMain implements Runnable {

        @Override
        public void run() {

        }
    }

    public JCommander buildCommander() {
        return JCommander.newBuilder()
                .addObject(new CommandMain())
                .addCommand("list", new CmdList())
                .addCommand("update", new CmdUpdate())
                .addCommand("names", new CmdNames())
                .addCommand("check", new CmdCheck())
                .addCommand("version", new CmdVersion())
                .build();
    }


}