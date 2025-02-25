package net.flectone.pulse.manager;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.flectone.pulse.database.dao.ColorsDAO;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.database.dao.IgnoreDAO;
import net.flectone.pulse.database.dao.ModerationDAO;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.command.stream.StreamModule;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.module.message.afk.BukkitAfkModule;
import net.flectone.pulse.module.message.format.name.BukkitNameModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.flectone.pulse.module.message.objective.belowname.BelownameModule;
import net.flectone.pulse.module.message.objective.tabname.TabnameModule;
import net.flectone.pulse.module.message.scoreboard.ScoreboardModule;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.flectone.pulse.util.ComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Singleton
public class BukkitFPlayerManager extends FPlayerManager {

    private final ColorsDAO colorsDAO;
    private final FPlayerDAO fPlayerDAO;
    private final IgnoreDAO ignoreDAO;
    private final ModerationDAO moderationDAO;

    @Inject private WorldModule worldModule;
    @Inject private BukkitAfkModule afkModule;
    @Inject private StreamModule streamModule;
    @Inject private BukkitNameModule nameModule;
    @Inject private BelownameModule belowNameModule;
    @Inject private TabnameModule tabnameModule;
    @Inject private ScoreboardModule scoreboardModule;
    @Inject private PlayerlistnameModule playerListNameModule;
    @Inject private FooterModule footerModule;
    @Inject private HeaderModule headerModule;
    @Inject private BrandModule brandModule;
    @Inject private ComponentUtil componentUtil;

    @Inject
    public BukkitFPlayerManager(FileManager fileManager,
                                ColorsDAO colorsDAO,
                                FPlayerDAO fPlayerDAO,
                                IgnoreDAO ignoreDAO,
                                ModerationDAO moderationDAO) {
        super(fileManager);

        this.colorsDAO = colorsDAO;
        this.fPlayerDAO = fPlayerDAO;
        this.ignoreDAO = ignoreDAO;
        this.moderationDAO = moderationDAO;
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
    public FPlayer convertToFPlayer(Object sender) {
        if (!(sender instanceof CommandSender commandSender)) return FPlayer.UNKNOWN;

        return commandSender instanceof Player player
                ? get(player)
                : commandSender instanceof ConsoleCommandSender
                        ? get(FPlayer.UNKNOWN.getUuid())
                        : new FPlayer(commandSender.getName());
    }

    @Override
    public Object convertToPlayer(FPlayer fPlayer) {
        return Bukkit.getPlayer(fPlayer.getUuid());
    }

    @Override
    public FPlayer put(UUID uuid, int entityId, String name, String ip) {
        fPlayerDAO.insertPlayer(uuid, name);

        FPlayer fPlayer = fPlayerDAO.getFPlayer(uuid);
        fPlayer.setOnline(true);
        colorsDAO.setFPlayerColors(fPlayer);
        ignoreDAO.setIgnores(fPlayer);

        fPlayer.updateMutes(moderationDAO.getModerations(fPlayer, Moderation.Type.MUTE));

        fPlayer.setIp(ip);
        fPlayer.setCurrentName(name);
        fPlayer.setEntityId(entityId);

        put(fPlayer);

        fPlayerDAO.updateFPlayer(fPlayer);

        worldModule.update(fPlayer);
        afkModule.remove("", fPlayer);
        streamModule.setStreamPrefix(fPlayer, fPlayer.is(FPlayer.Setting.STREAM));
        nameModule.add(fPlayer);
        belowNameModule.add(fPlayer);
        tabnameModule.add(fPlayer);
        playerListNameModule.update();
        scoreboardModule.send(fPlayer);
        footerModule.send(fPlayer);
        headerModule.send(fPlayer);
        brandModule.send(fPlayer);

        return fPlayer;
    }

    @Override
    public void remove(FPlayer fPlayer) {
        fPlayer.setOnline(false);

        afkModule.remove("quit", fPlayer);

        fPlayerDAO.updateFPlayer(fPlayer);

        nameModule.remove(fPlayer);
        belowNameModule.remove(fPlayer);
        tabnameModule.remove(fPlayer);
    }

    @Override
    public boolean hasPlayedBefore(FPlayer fPlayer) {
        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return false;

        return player.hasPlayedBefore();
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
    public Component getPlayerListHeader(FPlayer fPlayer) {
        String header = headerModule.getCurrentMessage(fPlayer);
        if (header != null) {
            return componentUtil.builder(fPlayer, header).build();
        }

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Component.empty();

        header = player.getPlayerListHeader();
        if (header == null) return Component.empty();

        return LegacyComponentSerializer.legacySection().deserialize(header);
    }

    @Override
    public Component getPlayerListFooter(FPlayer fPlayer) {
        String footer = footerModule.getCurrentMessage(fPlayer);
        if (footer != null) {
            return componentUtil.builder(fPlayer, footer).build();
        }

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return Component.empty();

        footer = player.getPlayerListFooter();
        if (footer == null) return Component.empty();

        return LegacyComponentSerializer.legacySection().deserialize(footer);
    }

    @Override
    public void loadOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            put(player.getUniqueId(), player.getEntityId(), player.getName(), player.getAddress().getHostString());
        }
    }
}
