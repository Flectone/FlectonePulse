package net.flectone.pulse.module.message.tab.playerlist.ticker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.ticker.AbstractTicker;

@Singleton
public class PlayerlistnameTicker extends AbstractTicker {

    @Inject
    public PlayerlistnameTicker(PlayerlistnameModule playerListNameModule) {
        super(playerListNameModule::send);
    }
}
