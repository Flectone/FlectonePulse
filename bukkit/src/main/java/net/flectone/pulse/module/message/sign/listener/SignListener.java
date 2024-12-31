package net.flectone.pulse.module.message.sign.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.sign.BukkitSignModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

@Singleton
public class SignListener implements Listener {

    private final FPlayerManager fPlayerManager;
    private final BukkitSignModule signModule;

    @Inject
    public SignListener(FPlayerManager fPlayerManager,
                        BukkitSignModule signModule) {
        this.fPlayerManager = fPlayerManager;
        this.signModule = signModule;
    }

    @EventHandler
    public void signChangeEvent(SignChangeEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerManager.get(event.getPlayer());

        for (int x = 0; x < event.getLines().length; x++) {
            String string = event.getLine(x);

            if (string == null || string.isEmpty()) continue;

            string = signModule.format(fPlayer, string);
            if (string == null) continue;

            event.setLine(x, string);
        }
    }
}
