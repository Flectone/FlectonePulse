package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.resolver.FileResolver;

@Singleton
public class SimpleVoiceModule extends AbstractModule {

    private final Integration.Simplevoice config;
    private final Permission.Integration.Simplevoice permission;

    @Getter private static SimpleVoiceIntegration simpleVoiceIntegration;

    @Inject
    public SimpleVoiceModule(FileResolver fileResolver,
                             SimpleVoiceIntegration simpleVoiceIntegration) {

        this.config = fileResolver.getIntegration().getSimplevoice();
        this.permission = fileResolver.getPermission().getIntegration().getSimplevoice();

        SimpleVoiceModule.simpleVoiceIntegration = simpleVoiceIntegration;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);
        simpleVoiceIntegration.hook();
    }

    @Override
    public void onDisable() {
        simpleVoiceIntegration.unhook();
    }

    @Override
    protected boolean isConfigEnable() {
        return config.isEnable();
    }
}
