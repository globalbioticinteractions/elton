package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.collections4.MapUtils;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.dataset.DatasetFinderException;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DatasetRegistryFactoryImpl implements DatasetRegistryFactory {
    private static final Map<String, Class<? extends DatasetRegistry>> REGISTRY_LOOKUP = MapUtils.unmodifiableMap(new TreeMap<String, Class<? extends DatasetRegistry>>() {{
        put("zenodo", DatasetRegistryZenodo.class);
        put("github", DatasetRegistryGitHubArchive.class);
    }});

    private final InputStreamFactory inputStreamFactory;

    public DatasetRegistryFactoryImpl(InputStreamFactory inputStreamFactory) {
        this.inputStreamFactory = inputStreamFactory;
    }

    @Override
    public DatasetRegistry createRegistryByName(String name) throws DatasetFinderException {
        Class<? extends DatasetRegistry> registryClass = REGISTRY_LOOKUP.get(name);
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

    public static Set<String> getSupportedRegistries() {
        return REGISTRY_LOOKUP.keySet();
    }
}
