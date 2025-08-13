package net.flectone.pulse.module.message.objective.tabname.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Pulse;
import net.flectone.pulse.listener.PulseListener;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.player.PlayerJoinEvent;
import net.flectone.pulse.model.event.player.PlayerLoadEvent;
import net.flectone.pulse.model.event.player.PlayerQuitEvent;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;

@Singleton
public class TabnamePulseListener implements PulseListener {

    private final TabnameModule tabnameModule;

    @Inject
    public TabnamePulseListener(TabnameModule tabnameModule) {
        this.tabnameModule = tabnameModule;
    }

    @Pulse
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        FPlayer fPlayer = event.getPlayer();
        tabnameModule.create(fPlayer);
    }

    @Pulse
    public void onPlayerLoadEvent(PlayerLoadEvent event) {
        if (!event.isReload()) return;

        FPlayer fPlayer = event.getPlayer();
        tabnameModule.create(fPlayer);
    }

    @Pulse
    public void onPlayerQuit(PlayerQuitEvent event) {
        FPlayer fPlayer = event.getPlayer();
        tabnameModule.remove(fPlayer);
    }

}
