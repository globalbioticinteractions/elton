package org.globalbioticinteractions.elton.cmd;

import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.dataset.DatasetFinderException;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;

public class DatasetRegistryFactoryImpl implements DatasetRegistryFactory {
    private final InputStreamFactory inputStreamFactory;

    public DatasetRegistryFactoryImpl(InputStreamFactory inputStreamFactory) {
        this.inputStreamFactory = inputStreamFactory;
    }

    @Override
    public DatasetRegistry createRegistryByName(String name) throws DatasetFinderException {
        Map<String, Class<? extends DatasetRegistry>> registryLookup = new TreeMap<String, Class<? extends DatasetRegistry>>() {{
            put("zenodo", DatasetRegistryZenodo.class);
            put("github", DatasetRegistryGitHubArchive.class);
        }};
        Class<? extends DatasetRegistry> registryClass = registryLookup.get(name);
        if (registryClass == null) {
            throw new DatasetFinderException("failed to create registry for [" + name + "]: not supported");
        }
        try {
            Constructor<? extends DatasetRegistry> constructor = registryClass.getConstructor(InputStreamFactory.class);
            return constructor.newInstance(inputStreamFactory);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new DatasetFinderException("failed to create registry for [" + name + "]", e);
        }
    }
}
