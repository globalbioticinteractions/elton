package org.globalbioticinteractions.elton.cmd;


import com.beust.jcommander.Parameters;
import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.lang3.StringUtils;

@Parameters(commandDescription = "Lists supported Registries")
public class CmdSupportedRegistries implements Runnable {

    @Override
    public void run() {
        TreeList<String> registryNames = new TreeList<>(DatasetRegistryFactoryImpl.getSupportedRegistries());
        System.out.println(StringUtils.join(registryNames, "\n"));
    }
}
