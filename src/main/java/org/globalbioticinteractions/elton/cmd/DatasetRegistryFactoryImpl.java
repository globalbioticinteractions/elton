package org.globalbioticinteractions.elton.cmd;

import org.apache.commons.collections4.MapUtils;
import org.eol.globi.util.InputStreamFactory;
import org.globalbioticinteractions.dataset.DatasetFinderException;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;
import org.globalbioticinteractions.elton.util.DatasetRegistrySingleDir;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

public class DatasetRegistryFactoryImpl implements DatasetRegistryFactory {
    private static final Map<String, Class<? extends DatasetRegistry>> REGISTRY_LOOKUP = MapUtils.unmodifiableMap(new TreeMap<String, Class<? extends DatasetRegistry>>() {{
        put("local", DatasetRegistrySingleDir.class);
        put("zenodo", DatasetRegistryZenodo.class);
        put("github", DatasetRegistryGitHubArchive.class);
    }});

    private final InputStreamFactory inputStreamFactory;
    private final URI workDir;
    private final String cacheDir;

    public DatasetRegistryFactoryImpl(URI workDir, String cacheDir, InputStreamFactory inputStreamFactory) {
        this.workDir = workDir;
        this.cacheDir = cacheDir;
        this.inputStreamFactory = inputStreamFactory;
    }

    @Override
    public DatasetRegistry createRegistryByName(String name) throws DatasetFinderException {
        Class<? extends DatasetRegistry> registryClass = REGISTRY_LOOKUP.get(name);
        if (registryClass == null) {
            throw new DatasetFinderException("failed to create registry for [" + name + "]: not supported");
        }
        try {
            Class<?>[] paramTypes = {URI.class, String.class, InputStreamFactory.class};
            Optional<Constructor<? extends DatasetRegistry>> constructor = constructorFor(registryClass, paramTypes);
            if (!constructor.isPresent()) {
                Class<?>[] paramTypesShort = {InputStreamFactory.class};
                Optional<Constructor<? extends DatasetRegistry>> constructor2 = constructorFor(registryClass, paramTypesShort);
                return constructor2
                        .orElseThrow(() -> new DatasetFinderException("failed to create registry for [" + name + "]")
                        ).newInstance(inputStreamFactory);
            } else {
                return constructor.get().newInstance(getWorkDir(), getCacheDir(), inputStreamFactory);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new DatasetFinderException("failed to create registry for [" + name + "]", e);
        }
    }

    private Optional<Constructor<? extends DatasetRegistry>> constructorFor(Class<? extends DatasetRegistry> registryClass, Class<?>[] paramTypes) {
        try {
            Constructor<? extends DatasetRegistry> constructor = registryClass.getConstructor(paramTypes);
            return Optional.of(constructor);
        } catch(NoSuchMethodException ex) {
            return Optional.empty();
        }
    }

    public static Set<String> getSupportedRegistries() {
        return REGISTRY_LOOKUP.keySet();
    }

    public URI getWorkDir() {
        return workDir;
    }

    public String getCacheDir() {
        return cacheDir;
    }
}
