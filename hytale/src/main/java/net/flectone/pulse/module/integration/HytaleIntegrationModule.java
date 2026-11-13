package net.flectone.pulse.module.integration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.integration.deepl.DeeplModule;
import net.flectone.pulse.module.integration.luckperms.LuckPermsModule;
import net.flectone.pulse.module.integration.placeholderapi.HytalePlaceholderAPIModule;
import net.flectone.pulse.module.integration.yandex.YandexModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.LazyInstance;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Singleton
public class HytaleIntegrationModule extends IntegrationModuleImpl {

    private final LazyInstance<PlatformServerAdapter> platformServerAdapter;

    @Inject
    public HytaleIntegrationModule(FileFacade fileFacade,
                                   LazyInstance<PlatformServerAdapter> platformServerAdapter,
                                   ListenerRegistry listenerRegistry,
                                   ModuleController moduleController,
                                   LazyInstance<LuckPermsModule> luckPermsModule,
                                   LazyInstance<DeeplModule> deeplModule,
                                   LazyInstance<YandexModule> yandexModule) {
        super(fileFacade, platformServerAdapter, moduleController, listenerRegistry, luckPermsModule, deeplModule, yandexModule);

        this.platformServerAdapter = platformServerAdapter;
    }

    @Override
    public Set<@NonNull Class<? extends ModuleSimple>> children() {
        Set<@NonNull Class<? extends ModuleSimple>> builder = new LinkedHashSet<>(super.children());

        if (platformServerAdapter.get().hasProject("HelpChat:PlaceholderAPI")) {
            builder.add(HytalePlaceholderAPIModule.class);
        }

        return Collections.unmodifiableSet(builder);
    }

}
