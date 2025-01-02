package org.globalbioticinteractions.elton.util;

import org.eol.globi.service.ResourceService;
import org.eol.globi.util.InputStreamFactory;
import org.eol.globi.util.ResourceServiceFactoryRemote;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class ResourceServiceRemote implements ResourceService {

    private final InputStreamFactory factory;
    private final File cacheDir;

    public ResourceServiceRemote(InputStreamFactory factory, File tmpDir) {
        this.factory = factory;
        this.cacheDir = tmpDir;
    }

    @Override
    public InputStream retrieve(URI resourceName) throws IOException {
        InputStream is = null;

        if (resourceName != null) {
            ResourceService resourceService = new ResourceServiceFactoryRemote(factory, cacheDir)
                    .serviceForResource(resourceName);
            if (resourceService == null) {
                throw new IOException("cannot retrieve content of unsupported resource identifier [" + resourceName.toString() + "]");
            } else {
                is = resourceService.retrieve(resourceName);
            }
        }

        return is;
    }
}
