package net.flectone.pulse.manager;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.Module;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ModuleManager {

    private final Injector injector;

    @Inject
    public ModuleManager(Injector injector) {
        this.injector = injector;
    }

    public void reload() {
        recursiveReload(Module.class);
    }

    public Map<String, Integer> getModules(Class<? extends AbstractModule> clazz) {
        Map<String, Integer> modules = new HashMap<>();

        recursiveGet(clazz, modules);

        return modules;
    }

    private void recursiveGet(Class<? extends AbstractModule> clazz, Map<String, Integer> modules) {
        AbstractModule module = injector.getInstance(clazz);

        modules.put(clazz.getSimpleName(), module.isEnable() ? 1 : 0);

        injector.getInstance(clazz)
                .getChildren()
                .forEach(subModule -> recursiveGet(subModule, modules));
    }

    private void recursiveReload(Class<? extends AbstractModule> clazz) {
        AbstractModule module = injector.getInstance(clazz);
        module.setEnable(module.isConfigEnable());

        if (module.isEnable()) {
            module.reload();
            module.getChildren().forEach(this::recursiveReload);
        } else {
            module.getChildren().forEach(subModule -> injector.getInstance(subModule).setEnable(false));
        }
    }
}
