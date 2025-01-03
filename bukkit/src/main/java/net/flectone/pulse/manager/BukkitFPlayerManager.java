package net.flectone.pulse.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.flectone.pulse.annotation.Sync;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.file.model.Sound;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.module.message.contact.afk.BukkitAfkModule;
import net.flectone.pulse.module.message.format.name.BukkitNameModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.UUID;

@Singleton
public class BukkitFPlayerManager extends FPlayerManager {

    private final String WEBSITE_AVATAR_URL = "https://mc-heads.net/avatar/<skin>/8.png";
    private final String WEBSITE_BODY_URL = "https://mc-heads.net/player/<skin>/16";

    private final ThreadManager threadManager;

    @Inject
    private WorldModule worldModule;

    @Inject
    private BukkitAfkModule afkModule;

    @Inject
    private StreamModule streamModule;

    @Inject
    private BukkitNameModule nameModule;

    @Inject
    private BelownameModule belowNameModule;

    @Inject
    private TabnameModule tabnameModule;

    @Inject
    private PlayerlistnameModule playerListNameModule;

    @Inject
    private FooterModule footerModule;

    @Inject
    private HeaderModule headerModule;

    @Inject
    private BrandModule brandModule;

    @Inject
    private IntegrationModule integrationModule;

    @Inject
    public BukkitFPlayerManager(FileManager fileManager,
                                ThreadManager threadManager) {
        super(fileManager);

        this.threadManager = threadManager;
    }

    @NotNull
    public FPlayer get(Object player) {
        if (!(player instanceof Player bukkitPlayer)) return FPlayer.UNKNOWN;

        if (!bukkitPlayer.isOnline()) return FPlayer.UNKNOWN;
        return get(bukkitPlayer.getUniqueId());
    }

    @Override
    public @NotNull FPlayer getOnline(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) return FPlayer.UNKNOWN;

        return get(player);
    }

    @Override
    public int getEntityId(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return 0;

        return player.getEntityId();
    }

    @Override
    public FPlayer convert(Object sender) {
        if (!(sender instanceof CommandSender commandSender)) return FPlayer.UNKNOWN;

        return commandSender instanceof Player player
                ? get(player)
                : commandSender instanceof ConsoleCommandSender
                        ? get(FPlayer.UNKNOWN.getUuid())
                        : new FPlayer(commandSender.getName());
    }

    @Override
    public FPlayer put(Database database, UUID uuid, int entityId, String name, String ip) throws SQLException {
        database.insertPlayer(uuid, name);

        FPlayer fPlayer = database.getFPlayer(uuid);
        put(fPlayer);

        database.setColors(fPlayer);
        database.setIgnores(fPlayer);
        fPlayer.getMutes().addAll(database.getModerations(fPlayer, Moderation.Type.MUTE));

        fPlayer.setOnline(true);
        fPlayer.setIp(ip);
        fPlayer.setCurrentName(name);
        fPlayer.setEntityId(entityId);

        database.updateFPlayer(fPlayer);

        worldModule.update(fPlayer);
        afkModule.remove("", fPlayer);
        streamModule.setStreamPrefix(fPlayer, fPlayer.is(FPlayer.Setting.STREAM));
        nameModule.add(fPlayer);
        belowNameModule.add(fPlayer);
        tabnameModule.add(fPlayer);
        playerListNameModule.update();
        footerModule.send(fPlayer);
        headerModule.send(fPlayer);
        brandModule.send(fPlayer);

        return fPlayer;
    }

    @Override
    public void remove(Database database, FPlayer fPlayer) throws SQLException {
        fPlayer.setOnline(false);

        afkModule.remove("quit", fPlayer);

        database.updateFPlayer(fPlayer);

        nameModule.remove(fPlayer);
        belowNameModule.remove(fPlayer);
        tabnameModule.remove(fPlayer);
    }

    @Override
    public String getIP(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return null;

        InetSocketAddress playerAddress = player.getAddress();
        if (playerAddress == null || playerAddress.isUnresolved()) return null;

        return playerAddress.getHostString();
    }

    @Override
    public boolean hasPlayedBefore(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        return player.hasPlayedBefore();
    }

    @Sync
    @Override
    public void playSound(Sound sound, FPlayer fPlayer) {
        if (sound == null) return;
        if (!sound.isEnable()) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;
        if (!player.hasPermission(sound.getPermission())) return;

        player.playSound(player.getLocation(), org.bukkit.Sound.valueOf(sound.getType()), sound.getVolume(), sound.getPitch());
    }

    @Sync
    @Override
    public void playSound(Sound sound, FPlayer fPlayer, Object location) {
        if (sound == null) return;
        if (!sound.isEnable()) return;
        if (!(location instanceof Location bukkitLocation)) return;

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;
        if (!player.hasPermission(sound.getPermission())) return;


        World world = bukkitLocation.getWorld();
        if (world == null) return;

        world.playSound(bukkitLocation, org.bukkit.Sound.valueOf(sound.getType()), sound.getVolume(), sound.getPitch());
    }

    @Sync
    @Override
    public void kick(FPlayer fPlayer, Component reason) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        player.kickPlayer(LegacyComponentSerializer.legacySection().serialize(reason));
    }

    @Override
    public String getSortedName(@NotNull FPlayer fPlayer) {
        if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_17)) {
            return fPlayer.getName();
        }

        int weight = integrationModule.getGroupWeight(fPlayer);

        String paddedRank = String.format("%010d", Integer.MAX_VALUE - weight);
        String paddedName = String.format("%-16s", fPlayer.getName());
        return paddedRank + paddedName;
    }

    @Override
    public String getSkin(FEntity sender) {
        String replacement = integrationModule.getTextureUrl(sender);
        return replacement == null ? sender.getUuid().toString() : replacement;
    }

    @Override
    public String getAvatarURL(FEntity sender) {
        return WEBSITE_AVATAR_URL.replace("<skin>", getSkin(sender));
    }

    @Override
    public String getBodyURL(FEntity sender) {
        return WEBSITE_BODY_URL.replace("<skin>", getSkin(sender));
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
    public int getPing(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return 0;

        return player.getPing();
    }

    @Override
    public double distance(FPlayer first, FPlayer second) {
        Player firstPlayer = Bukkit.getPlayer(first.getUuid());
        if (firstPlayer == null) return -1.0;

        Player secondPlayer = Bukkit.getPlayer(second.getUuid());
        if (secondPlayer == null) return -1.0;
        if (!firstPlayer.getLocation().getWorld().equals(secondPlayer.getLocation().getWorld())) return -1;

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
    public void loadOnlinePlayers() {
        threadManager.runDatabase(database -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                put(database, player.getUniqueId(), player.getEntityId(), player.getName(), player.getAddress().getHostString());
            }
        });
    }
}
