package net.flectone.pulse.platform.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PaperMessageSender {

    private final FLogger fLogger;

    public boolean sendMessage(FPlayer fPlayer, String serialized) {
        Player player = Bukkit.getPlayer(fPlayer.uuid());
        if (player == null) return false;

        try {
            Component component = GsonComponentSerializer.gson().deserialize(serialized);
            player.sendMessage(component);
        } catch (Exception e) {
            fLogger.warning(e, "Failed to deserialize message %s", serialized);
            return false;
        }

        return true;
    }

}
