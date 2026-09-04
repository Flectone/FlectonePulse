package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.logging.FLogger;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitSimpleVoiceModule extends MinecraftSimpleVoiceModule {

    private final Plugin plugin;
    private final MinecraftSimpleVoiceIntegration simpleVoiceIntegration;
    private final FLogger fLogger;

    @Inject
    public BukkitSimpleVoiceModule(FileFacade fileFacade,
                                   Plugin plugin,
                                   MinecraftSimpleVoiceIntegration simpleVoiceIntegration,
                                   FLogger fLogger) {
        super(fileFacade, simpleVoiceIntegration);

        this.plugin = plugin;
        this.simpleVoiceIntegration = simpleVoiceIntegration;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        try {
            BukkitVoicechatService service = plugin.getServer().getServicesManager().load(BukkitVoicechatService.class);
            if (service == null) return;

            service.registerPlugin(simpleVoiceIntegration);
            super.onEnable();
        } catch (Exception e) {
            simpleVoiceIntegration.logHookFailed(e);
        }
    }

}
