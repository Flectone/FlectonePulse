package net.flectone.pulse.module.message.reload.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.event.message.MessageReceiveEvent;
import net.flectone.pulse.module.message.reload.ReloadModule;
import net.flectone.pulse.util.constant.MinecraftTranslationKey;

@Singleton
public class ReloadPulseListener implements PulseListener {

    private final ReloadModule reloadModule;

    @Inject
    public ReloadPulseListener(ReloadModule reloadModule) {
        this.reloadModule = reloadModule;
    }

    @Pulse
    public void onTranslatableMessageReceiveEvent(MessageReceiveEvent event) {
        if (event.getTranslationKey() != MinecraftTranslationKey.COMMANDS_RELOAD_SUCCESS) return;

        event.setCancelled(true);
        reloadModule.send(event.getFPlayer());
    }

}