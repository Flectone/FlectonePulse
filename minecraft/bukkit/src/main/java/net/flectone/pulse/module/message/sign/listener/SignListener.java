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

import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SignListener implements Listener {

    private final FPlayerService fPlayerService;
    private final BukkitSignModule signModule;

    @EventHandler
    public void signChangeEvent(SignChangeEvent event) {
        if (event.isCancelled()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(event.getPlayer().getUniqueId());

        for (int x = 0; x < event.getLines().length; x++) {
            String string = event.getLine(x);

            Optional<String> formattedString = signModule.format(fPlayer, string);
            if (formattedString.isPresent()) {
                event.setLine(x, formattedString.get());
            }
        }
    }
}
