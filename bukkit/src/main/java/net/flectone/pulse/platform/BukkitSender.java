package net.flectone.pulse.platform;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.model.FPlayer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@Singleton
public class BukkitSender extends Sender {

    private final BukkitAudiences audience;

    @Inject
    public BukkitSender(Plugin plugin) {
        this.audience = BukkitAudiences.create(plugin);
    }

    @Override
    public void sendMessage(FPlayer fPlayer, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        if (fPlayer.isUnknown()) {
            audience.sender(Bukkit.getConsoleSender()).sendMessage(component);
            return;
        }

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        audience.player(player).sendMessage(component);
    }

    @Override
    public void sendTitle(FPlayer fPlayer, Title.Times times, Component title, Component subTitle) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        audience.player(player).showTitle(Title.title(title, subTitle, times));
    }

    @Override
    public void sendActionBar(FPlayer fPlayer, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        audience.player(player).sendActionBar(component);
    }

    @Override
    public void sendPlayerListFooter(FPlayer fPlayer, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        audience.player(player).sendPlayerListFooter(component);
    }

    @Override
    public void sendPlayerListHeader(FPlayer fPlayer, Component component) {
        if (!Component.IS_NOT_EMPTY.test(component)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        audience.player(player).sendPlayerListHeader(component);
    }
}
