package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.globalbioticinteractions.elton.Elton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@CommandLine.Command(
        name = "config-man",
        aliases = {"config-manpage", "install-manpage"},
        description = "Installs/configures Elton man page, so you can type [man preston] on unix-like system to learn more about Preston. "
)
public class CmdInstallManual implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CmdInstallManual.class);

    @Override
    public void run() {
        File manPageDir = new File("/usr/local/share/man/man1/");
        Class<Elton> programClass = Elton.class;
        String programName = StringUtils.lowerCase(programClass.getSimpleName());
        File file = new File(manPageDir, programName + ".1");
        String packageName = "/" + programClass.getPackage().getName().replace('.', '/');
        try (InputStream resourceAsStream = getClass().getResourceAsStream(packageName + "/docs/manpage/" + programName + ".1")) {
            if (manPageDir.exists()) {
                IOUtils.copy(resourceAsStream,
                        new FileOutputStream(file));
            } else {
                throw new IOException("no man page directory found at [" + manPageDir.getAbsolutePath() + "]");
            }
            LOG.info("installed man page at [" + file.getAbsolutePath() + "]");
        } catch (IOException e) {
            LOG.error("failed to install man page at [" + file.getAbsolutePath() + "]", e);
        }
    }

}
