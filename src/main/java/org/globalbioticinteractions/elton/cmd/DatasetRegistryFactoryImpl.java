package org.globalbioticinteractions.elton.cmd;

import bio.guoda.preston.HashType;
import bio.guoda.preston.cmd.ActivityContext;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.rdf.api.IRI;
import org.eol.globi.service.ResourceService;
import org.eol.globi.util.InputStreamFactory;
import org.eol.globi.util.ResourceServiceLocalAndRemote;
import org.globalbioticinteractions.cache.ContentPathFactory;
import org.globalbioticinteractions.cache.ContentPathFactoryDepth0;
import org.globalbioticinteractions.dataset.DatasetRegistryException;
import org.globalbioticinteractions.dataset.DatasetRegistry;
import org.globalbioticinteractions.dataset.DatasetRegistryGitHubArchive;
import org.globalbioticinteractions.dataset.DatasetRegistryZenodo;
import org.globalbioticinteractions.elton.store.ActivityListener;
import org.globalbioticinteractions.elton.util.DatasetRegistrySingleDir;
import org.globalbioticinteractions.elton.util.DatasetRegistryUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

public class DatasetRegistryFactoryImpl implements DatasetRegistryFactory {
    private static final Map<String, Class<? extends DatasetRegistry>> REGISTRY_LOOKUP = MapUtils.unmodifiableMap(new TreeMap<String, Class<? extends DatasetRegistry>>() {{
        put(DatasetRegistryUtil.NAMESPACE_LOCAL, DatasetRegistrySingleDir.class);
        put(DatasetRegistryUtil.NAMESPACE_ZENODO, DatasetRegistryZenodo.class);
        put(DatasetRegistryUtil.NAMESPACE_GITHUB, DatasetRegistryGitHubArchive.class);
    }});

    private final InputStreamFactory inputStreamFactory;
    private final URI workDir;
    private final String dataDir;

    private final String provDir;
    private final ActivityListener activityListener;
    private final ResourceService resourceService;
    private ActivityContext activityContext;
    private Supplier<IRI> iriSupplier;
    private HashType hashType;

    public DatasetRegistryFactoryImpl(
            URI workDir,
            InputStreamFactory inputStreamFactory,
            String dataDir,
            String provDir,
            ActivityListener activityListener,
            ActivityContext activityContext,
            Supplier<IRI> iriSupplier,
            ResourceService resourceService,
            HashType hashType) {
        this.workDir = workDir;
        this.inputStreamFactory = inputStreamFactory;
        this.dataDir = dataDir;
        this.provDir = provDir;
        this.activityListener = activityListener;
        this.activityContext = activityContext;
        this.iriSupplier = iriSupplier;
        this.resourceService = resourceService;
        this.hashType = hashType;
    }

    @Override
    public DatasetRegistry createRegistryByName(String name) throws DatasetRegistryException {
        Class<? extends DatasetRegistry> registryClass = REGISTRY_LOOKUP.get(name);
        if (registryClass == null) {
            throw new DatasetRegistryException("failed to create registry for [" + name + "]: not supported");
        }
        try {
            Class<?>[] paramTypes = {URI.class, ResourceService.class, ContentPathFactory.class, String.class, String.class, ActivityListener.class, ActivityContext.class, Supplier.class, HashType.class};
            Optional<Constructor<? extends DatasetRegistry>> constructor = constructorFor(registryClass, paramTypes);
            ResourceService resourceService = this.resourceService == null
                    ? new ResourceServiceLocalAndRemote(inputStreamFactory, new File(getWorkDir()))
                    : this.resourceService;
            if (!constructor.isPresent()) {
                Class<?>[] paramTypesShort = {ResourceService.class};
                Optional<Constructor<? extends DatasetRegistry>> constructor2 = constructorFor(registryClass, paramTypesShort);
                return constructor2
                        .orElseThrow(() -> new DatasetRegistryException("failed to create registry for [" + name + "] using [" + registryClass.getSimpleName() + "]")
                        ).newInstance(resourceService);
            } else {
                return constructor.get().newInstance(
                        getWorkDir(),
                        resourceService,
                        new ContentPathFactoryDepth0(),
                        getDataDir(),
                        getProvDir(),
                        getActivityListener(),
                        activityContext,
                        iriSupplier,
                        hashType);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new DatasetRegistryException("failed to create registry for [" + name + "] using [" + registryClass.getSimpleName() + "]", e);
        }
    }

    private Optional<Constructor<? extends DatasetRegistry>> constructorFor(Class<? extends DatasetRegistry> registryClass, Class<?>[] paramTypes) {
        try {
            Constructor<? extends DatasetRegistry> constructor = registryClass.getConstructor(paramTypes);
            return Optional.of(constructor);
        } catch (NoSuchMethodException ex) {
            return Optional.empty();
        }
    }

    public static Set<String> getSupportedRegistries() {
        return REGISTRY_LOOKUP.keySet();
    }

    public URI getWorkDir() {
        return workDir;
    }

    public String getDataDir() {
        return dataDir;
    }

    public String getProvDir() {
        return provDir;
    }

    public ActivityListener getActivityListener() {
        return activityListener;
    }
}
