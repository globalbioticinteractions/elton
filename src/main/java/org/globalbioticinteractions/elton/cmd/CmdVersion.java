package org.globalbioticinteractions.elton.cmd;

import com.beust.jcommander.Parameters;
import com.jcabi.manifests.Manifests;
import org.apache.commons.lang.StringUtils;
import org.eol.globi.Version;
import org.globalbioticinteractions.elton.Elton;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Parameters(separators = "= ", commandDescription = "Show Version")
public class CmdVersion implements Runnable {

    @Override
    public void run() {
        System.out.println(CmdVersion.class.getPackage().getImplementationVersion());
    }

}
