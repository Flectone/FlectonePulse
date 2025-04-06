package net.flectone.pulse.adapter;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.message.afk.AfkModule;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.module.message.format.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.module.message.sidebar.SidebarModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.formatter.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

@Singleton
public class BukkitPlayerAdapter extends PlatformPlayerAdapter {

    private final Injector injector;

    @Inject
    public BukkitPlayerAdapter(Injector injector) {
        this.injector = injector;
    }

    @NotNull
    public FPlayer get(Object player) {
        if (!(player instanceof Player bukkitPlayer)) return FPlayer.UNKNOWN;

        if (!bukkitPlayer.isOnline()) return FPlayer.UNKNOWN;
        return get(bukkitPlayer.getUniqueId());
    }

    @Override
    public int getEntityId(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return 0;

        return player.getEntityId();
    }

    @Override
    public UUID getPlayerByEntityId(int id) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.getEntityId() == id)
                .findFirst()
                .map(Entity::getUniqueId)
                .orElse(null);
    }

    @Override
    public String getName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return "";

        return player.getName();
    }

    @Override
    public UUID getUUID(Object player) {
        if (player instanceof Player onlinePlayer) {
            return onlinePlayer.getUniqueId();
        }

        return null;
    }

    @Override
    public Object convertToPlatformPlayer(FPlayer fPlayer) {
        return Bukkit.getPlayer(fPlayer.getUuid());
    }

    @Override
    public String getName(Object player) {
        if (player instanceof CommandSender commandSender) {
            return commandSender.getName();
        }

        return null;
    }

    @Override
    public boolean hasPlayedBefore(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        return player.hasPlayedBefore();
    }

    @Override
    public boolean isOnline(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        return player.isOnline();
    }

    @Override
    public long getFirstPlayed(FPlayer fPlayer) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(fPlayer.getUuid());

        return player.getFirstPlayed();
    }

    @Override
    public long getLastPlayed(FPlayer fPlayer) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(fPlayer.getUuid());

        return player.getLastPlayed();
    }

    @Override
    public long getAllTimePlayed(FPlayer fPlayer) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(fPlayer.getUuid());

        return player.getStatistic(Statistic.PLAY_ONE_MINUTE) * 60L;
    }

    @Override
    public String getWorldName(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return "";

        return player.getWorld().getName();
    }

    @Override
    public String getWorldEnvironment(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return "";

        return player.getWorld().getEnvironment().toString().toLowerCase();
    }

    @Override
    public String getIp(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return null;

        InetSocketAddress address = player.getAddress();
        if (address == null) return null;

        return address.getHostString();
    }

    @Override
    public int getObjectiveScore(UUID uuid, ObjectiveMode objectiveValueType) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return 0;

        if (objectiveValueType == null) return 0;

        return switch (objectiveValueType) {
            case HEALTH -> (int) Math.round(player.getHealth() * 10.0)/10;
            case LEVEL -> player.getLevel();
            case FOOD -> player.getFoodLevel();
            case PING -> player.getPing();
            case ARMOR -> {
                AttributeInstance armor = player.getAttribute(Attribute.GENERIC_ARMOR);
                yield armor != null ? (int) Math.round(armor.getValue() * 10.0)/10 : 0;
            }
            case ATTACK -> {
                AttributeInstance damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                yield damage != null ? (int) Math.round(damage.getValue() * 10.0)/10 : 0;
            }
        };
    }

    @Override
    public double distance(FPlayer first, FPlayer second) {
        Player firstPlayer = Bukkit.getPlayer(first.getUuid());
        if (firstPlayer == null) return -1.0;

        Player secondPlayer = Bukkit.getPlayer(second.getUuid());
        if (secondPlayer == null) return -1.0;

        World world = firstPlayer.getLocation().getWorld();
        if (world == null) return -1.0;
        if (!world.equals(secondPlayer.getLocation().getWorld())) return -1.0;

        return firstPlayer.getLocation().distance(secondPlayer.getLocation());
    }

    @Override
    public GameMode getGamemode(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return GameMode.SURVIVAL;

        return SpigotConversionUtil.fromBukkitGameMode(player.getGameMode());
    }

    @Override
    public Object getItem(@NotNull UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;

        return player.getInventory().getItemInMainHand().getType() == Material.AIR
                ? player.getInventory().getItemInOffHand()
                : player.getInventory().getItemInMainHand();
    }

    @Override
    public Component getPlayerListHeader(FPlayer fPlayer) {
        String header = injector.getInstance(HeaderModule.class).getCurrentMessage(fPlayer);
        if (header != null) {
            return injector.getInstance(MessageFormatter.class).builder(fPlayer, header).build();
        }

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Component.empty();

        header = player.getPlayerListHeader();
        if (header == null) return Component.empty();

        return LegacyComponentSerializer.legacySection().deserialize(header);
    }

    @Override
    public Component getPlayerListFooter(FPlayer fPlayer) {
        String footer = injector.getInstance(FooterModule.class).getCurrentMessage(fPlayer);
        if (footer != null) {
            return injector.getInstance(MessageFormatter.class).builder(fPlayer, footer).build();
        }

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Component.empty();

        footer = player.getPlayerListFooter();
        if (footer == null) return Component.empty();

        return LegacyComponentSerializer.legacySection().deserialize(footer);
    }

    @Override
    public List<UUID> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Entity::getUniqueId)
                .toList();
    }

    @Override
    public boolean isConsole(Object player) {
        return player instanceof ConsoleCommandSender;
    }

    @Override
    public void clear(FPlayer fPlayer) {
        injector.getInstance(AfkModule.class).remove("quit", fPlayer);
        injector.getInstance(ScoreboardModule.class).remove(fPlayer);
        injector.getInstance(BelownameModule.class).remove(fPlayer);
        injector.getInstance(TabnameModule.class).remove(fPlayer);
    }

    @Override
    public void update(FPlayer fPlayer) {
        injector.getInstance(WorldModule.class).update(fPlayer);
        injector.getInstance(AfkModule.class).remove("", fPlayer);
        injector.getInstance(StreamModule.class).setStreamPrefix(fPlayer, fPlayer.isSetting(FPlayer.Setting.STREAM));
        injector.getInstance(ScoreboardModule.class).add(fPlayer);
        injector.getInstance(BelownameModule.class).add(fPlayer);
        injector.getInstance(TabnameModule.class).add(fPlayer);
        injector.getInstance(PlayerlistnameModule.class).update();
        injector.getInstance(SidebarModule.class).send(fPlayer);
        injector.getInstance(FooterModule.class).send(fPlayer);
        injector.getInstance(HeaderModule.class).send(fPlayer);
        injector.getInstance(BrandModule.class).send(fPlayer);
    }
}
