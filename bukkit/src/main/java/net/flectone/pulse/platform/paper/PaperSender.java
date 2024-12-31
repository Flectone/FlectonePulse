package net.flectone.pulse.platform.paper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.platform.PlatformSender;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Singleton
public class PaperSender extends PlatformSender {

    @Inject
    public PaperSender() {}

    @Override
    public void sendMessage(FPlayer fPlayer, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        if (fPlayer.isUnknown()) {
            Bukkit.getConsoleSender().sendMessage(component);
            return;
        }

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        player.sendMessage(component);
    }

    @Override
    public void sendActionBar(FPlayer fPlayer, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        player.sendActionBar(component);
    }

    @Override
    public void sendPlayerListFooter(FPlayer fPlayer, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        player.sendPlayerListFooter(component);
    }

    @Override
    public void sendPlayerListHeader(FPlayer fPlayer, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        player.sendPlayerListHeader(component);
    }
}
