package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.module.AbstractModule;
import org.bukkit.plugin.Plugin;

@Singleton
public class SimpleVoiceModule extends AbstractModule {

    private final Integration.Simplevoice config;
    private final Permission.Integration.Simplevoice permission;

    private final Plugin plugin;
    private final SimpleVoiceIntegration simpleVoiceIntegration;

    @Inject
    public SimpleVoiceModule(FileManager fileManager,
                             Plugin plugin,
                             SimpleVoiceIntegration simpleVoiceIntegration) {
        this.plugin = plugin;
        this.simpleVoiceIntegration = simpleVoiceIntegration;

        config = fileManager.getIntegration().getSimplevoice();
        permission = fileManager.getPermission().getIntegration().getSimplevoice();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        BukkitVoicechatService service = plugin.getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service == null) return;

        service.registerPlugin(simpleVoiceIntegration);
        simpleVoiceIntegration.hook();
    }

    @Override
    protected boolean isConfigEnable() {
        return config.isEnable();
    }

}
