package net.flectone.pulse.module.integration.simplevoice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitSimpleVoiceModule extends SimpleVoiceModule {

    private final Plugin plugin;
    private final SimpleVoiceIntegration simpleVoiceIntegration;

    @Inject
    public BukkitSimpleVoiceModule(FileResolver fileResolver,
                                   Plugin plugin,
                                   SimpleVoiceIntegration simpleVoiceIntegration) {
        super(fileResolver, simpleVoiceIntegration);

        this.plugin = plugin;
        this.simpleVoiceIntegration = simpleVoiceIntegration;
    }

    @Override
    public void onEnable() {
        BukkitVoicechatService service = plugin.getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service == null) return;

        service.registerPlugin(simpleVoiceIntegration);
        super.onEnable();
    }

}
