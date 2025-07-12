package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.module.AbstractModule;
import org.bukkit.plugin.Plugin;

@Singleton
public class SimpleVoiceModule extends AbstractModule {

    private final Integration.Simplevoice config;
    private final Permission.Integration.Simplevoice permission;

    private final Plugin plugin;
    private final SimpleVoiceIntegration simpleVoiceIntegration;

    @Inject
    public SimpleVoiceModule(FileResolver fileResolver,
                             Plugin plugin,
                             SimpleVoiceIntegration simpleVoiceIntegration) {
        this.plugin = plugin;
        this.simpleVoiceIntegration = simpleVoiceIntegration;

        config = fileResolver.getIntegration().getSimplevoice();
        permission = fileResolver.getPermission().getIntegration().getSimplevoice();
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        BukkitVoicechatService service = plugin.getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service == null) return;

        service.registerPlugin(simpleVoiceIntegration);
        simpleVoiceIntegration.hook();
    }

    @Override
    public void onDisable() {
    @Override
    protected boolean isConfigEnable() {
        return config.isEnable();
    }

}
