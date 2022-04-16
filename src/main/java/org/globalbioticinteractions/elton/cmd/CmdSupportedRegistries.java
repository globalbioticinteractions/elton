package org.globalbioticinteractions.elton.cmd;


import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

@CommandLine.Command (
        name = "registries",
        description = "Lists supported Registries"
)
public class CmdSupportedRegistries implements Runnable {

    @Override
    public void run() {
        TreeList<String> registryNames = new TreeList<>(DatasetRegistryFactoryImpl.getSupportedRegistries());
        System.out.println(StringUtils.join(registryNames, "\n"));
    }
}
