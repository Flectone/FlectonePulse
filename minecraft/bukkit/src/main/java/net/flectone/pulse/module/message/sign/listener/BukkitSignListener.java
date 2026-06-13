package net.flectone.pulse.module.message.sign.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.message.sign.BukkitSignModule;
import net.flectone.pulse.service.FPlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BukkitSignListener implements Listener {

    private final FPlayerService fPlayerService;
    private final BukkitSignModule signModule;

    @EventHandler
    public void onSignChangeEvent(SignChangeEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        for (int i = 0; i < event.getLines().length; i++) {
            String line = event.getLine(i);

            int lineIndex = i;
            signModule.legacyFormat(fPlayer, line).ifPresent(string -> event.setLine(lineIndex, string));
        }
    }
}
