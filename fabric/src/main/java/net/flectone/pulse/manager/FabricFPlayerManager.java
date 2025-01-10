package net.flectone.pulse.manager;

import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.Database;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Moderation;
import net.flectone.pulse.module.message.brand.BrandModule;
import net.flectone.pulse.module.message.format.world.WorldModule;
import net.flectone.pulse.module.message.objective.ObjectiveMode;
import net.flectone.pulse.module.message.tab.footer.FooterModule;
import net.flectone.pulse.module.message.tab.header.HeaderModule;
import net.flectone.pulse.module.message.tab.playerlist.PlayerlistnameModule;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

@Singleton
public class FabricFPlayerManager extends FPlayerManager {

    private final ThreadManager threadManager;
    private final MinecraftServer minecraftServer;

    @Inject
    private WorldModule worldModule;

//    @Inject
//    private StreamModule streamModule;
//
//    @Inject
//    private ScoreboardModule scoreboardModule;

    @Inject
    private PlayerlistnameModule playerListNameModule;

    @Inject
    private FooterModule footerModule;

    @Inject
    private HeaderModule headerModule;

    @Inject
    private BrandModule brandModule;

//    @Inject
//    private IntegrationModule integrationModule;

    @Inject
    public FabricFPlayerManager(FileManager fileManager,
                                ThreadManager threadManager,
                                MinecraftServer minecraftServer) {
        super(fileManager);

        this.threadManager = threadManager;
        this.minecraftServer = minecraftServer;
    }

    @Override
    public @NotNull FPlayer get(Object player) {
        if (!(player instanceof ServerPlayerEntity fabricPlayer)) return FPlayer.UNKNOWN;
        if (fabricPlayer.isDisconnected()) return FPlayer.UNKNOWN;

        return get(fabricPlayer.getUuid());
    }

    @Override
    public @NotNull FPlayer getOnline(String playerName) {
        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(playerName);
        if (player == null) return FPlayer.UNKNOWN;

        return get(player);
    }

    @Override
    public int getEntityId(FPlayer fPlayer) {
        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(fPlayer.getUuid());
        if (player == null) return 0;

        return player.getEntityId();
    }

    @Override
    public FPlayer convert(Object sender) {
        if (!(sender instanceof ServerPlayerEntity commandSender)) return FPlayer.UNKNOWN;

        return get(commandSender);
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
//        afkModule.remove("", fPlayer);
//        streamModule.setStreamPrefix(fPlayer, fPlayer.is(FPlayer.Setting.STREAM));
//        nameModule.add(fPlayer);
//        belowNameModule.add(fPlayer);
//        tabnameModule.add(fPlayer);
        playerListNameModule.update();
//        scoreboardModule.send(fPlayer);
        footerModule.send(fPlayer);
        headerModule.send(fPlayer);
        brandModule.send(fPlayer);

        return fPlayer;
    }

    @Override
    public void remove(Database database, FPlayer fPlayer) throws SQLException {
        fPlayer.setOnline(false);

//        afkModule.remove("quit", fPlayer);

        database.updateFPlayer(fPlayer);
//
//        nameModule.remove(fPlayer);
//        belowNameModule.remove(fPlayer);
//        tabnameModule.remove(fPlayer);
    }

    @Override
    public String getIP(UUID uuid) {
        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(uuid);
        if (player == null) return null;

        return player.getIp();
    }

    @Override
    public String getWorldName(FPlayer fPlayer) {
        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(fPlayer.getUuid());
        if (player == null) return "";

        return player.getEntityWorld().getRegistryKey().getValue().getNamespace();
    }

    @Override
    public String getWorldEnvironment(FPlayer fPlayer) {
        // TODO
        return getWorldName(fPlayer);
    }

    @Override
    public Object getItem(@NotNull UUID uuid) {
        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(uuid);
        if (player == null) return null;
        // TODO
        return player.getActiveItem();
    }

    @Override
    public Component getPlayerListHeader(FPlayer fPlayer) {
        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(fPlayer.getUuid());
        if (player == null) return Component.empty();
        // TODO
        return Component.empty();
    }

    @Override
    public Component getPlayerListFooter(FPlayer fPlayer) {
        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(fPlayer.getUuid());
        if (player == null) return Component.empty();
        // TODO
        return Component.empty();
    }

    @Override
    public int getObjectiveScore(UUID uuid, ObjectiveMode objectiveValueType) {
        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(uuid);
        if (player == null) return 0;
        if (objectiveValueType == null) return 0;

        return switch (objectiveValueType) {
            case HEALTH -> (int) Math.round(player.getHealth() * 10.0)/10;
            case LEVEL -> player.getNextLevelExperience() - 1;
            case FOOD -> player.getHungerManager().getFoodLevel();
            case PING -> 0; // TODO
            case ARMOR -> (int) Math.round(player.getArmor() * 10.0)/10;
            case ATTACK -> {
                double damage = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                yield (int) Math.round(damage * 10.0)/10;
            }
        };
    }

    @Override
    public int getPing(FPlayer fPlayer) {
        // TODO
        return 0;
    }

    @Override
    public double distance(FPlayer first, FPlayer second) {
        ServerPlayerEntity firstPlayer = minecraftServer.getPlayerManager().getPlayer(first.getUuid());
        if (firstPlayer == null) return -1.0;

        ServerPlayerEntity secondPlayer = minecraftServer.getPlayerManager().getPlayer(second.getUuid());
        if (secondPlayer == null) return -1.0;
        if (!firstPlayer.getEntityWorld().equals(secondPlayer.getEntityWorld())) return -1.0;

        return firstPlayer.getPos().distanceTo(secondPlayer.getPos());
    }

    @Override
    public GameMode getGamemode(FPlayer fPlayer) {
        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(fPlayer.getUuid());
        if (player == null) return GameMode.SURVIVAL;
        if (player.isCreative()) return GameMode.CREATIVE;
        if (player.isSpectator()) return GameMode.SPECTATOR;

        return GameMode.SURVIVAL;
    }

    @Override
    public boolean hasPlayedBefore(FPlayer fPlayer) {
        ServerPlayerEntity player = minecraftServer.getPlayerManager().getPlayer(fPlayer.getUuid());
        if (player == null) return false;
        // TODO
        return false;
    }

    @Override
    public void loadOnlinePlayers() {
        threadManager.runDatabase(database -> {
            if (minecraftServer == null) return;
            if (minecraftServer.getPlayerManager() == null) return;

            for (ServerPlayerEntity player : minecraftServer.getPlayerManager().getPlayerList()) {
                put(database, player.getUuid(), player.getEntityId(), player.getName().getString(), player.getIp());
            }
        });
    }
}
