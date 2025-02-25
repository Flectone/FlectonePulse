package net.flectone.pulse.module.message.status;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.database.dao.ColorsDAO;
import net.flectone.pulse.database.dao.FPlayerDAO;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.logging.FLogger;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.status.icon.IconModule;
import net.flectone.pulse.module.message.status.listener.StatusPacketListener;
import net.flectone.pulse.module.message.status.motd.MOTDModule;
import net.flectone.pulse.module.message.status.players.PlayersModule;
import net.flectone.pulse.module.message.status.version.VersionModule;
import net.flectone.pulse.util.ComponentUtil;
import net.flectone.pulse.util.ServerUtil;

import java.util.Collection;
import java.util.List;

@Singleton
public class StatusModule extends AbstractModule {

    private final Message.Status message;
    private final Permission.Message.Status permission;

    private final FPlayerDAO fPlayerDAO;
    private final ColorsDAO colorsDAO;
    private final MOTDModule MOTDModule;
    private final IconModule iconModule;
    private final PlayersModule playersModule;
    private final VersionModule versionModule;
    private final ComponentUtil componentUtil;
    private final ServerUtil bukkitUtil;
    private final FPlayerManager fPlayerManager;
    private final ListenerRegistry listenerRegistry;
    private final IntegrationModule integrationModule;

    @Inject
    public StatusModule(FileManager fileManager,
                        FPlayerDAO fPlayerDAO,
                        ColorsDAO colorsDAO,
                        MOTDModule MOTDModule,
                        IconModule iconModule,
                        PlayersModule playersModule,
                        VersionModule versionModule,
                        FLogger fLogger,
                        ComponentUtil componentUtil,
                        ServerUtil bukkitUtil,
                        FPlayerManager fPlayerManager,
                        ListenerRegistry listenerRegistry,
                        IntegrationModule integrationModule) {
        this.fPlayerDAO = fPlayerDAO;
        this.colorsDAO = colorsDAO;
        this.MOTDModule = MOTDModule;
        this.iconModule = iconModule;
        this.playersModule = playersModule;
        this.versionModule = versionModule;
        this.componentUtil = componentUtil;
        this.bukkitUtil = bukkitUtil;
        this.fPlayerManager = fPlayerManager;
        this.listenerRegistry = listenerRegistry;
        this.integrationModule = integrationModule;

        message = fileManager.getMessage().getStatus();
        permission = fileManager.getPermission().getMessage().getStatus();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        addChildren(MOTDModule.class);
        addChildren(IconModule.class);
        addChildren(PlayersModule.class);
        addChildren(VersionModule.class);

        listenerRegistry.register(StatusPacketListener.class);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public void send(User user) {
        FPlayer fPlayer = fPlayerDAO.getFPlayer(user.getAddress().getAddress());
        colorsDAO.load(fPlayer);

        if (checkModulePredicates(fPlayer)) return;

        JsonObject responseJson = new JsonObject();

        responseJson.add("version", getVersionJson(fPlayer));
        responseJson.add("players", getPlayersJson(fPlayer));
        responseJson.add("description", getDescriptionJson(fPlayer));

        String favicon = getFavicon(fPlayer);
        if (favicon != null) {
            responseJson.addProperty("favicon", favicon);
        }

        responseJson.addProperty("enforcesSecureChat", false);

        user.sendPacket(new WrapperStatusServerResponse(responseJson));
    }

    private JsonElement getVersionJson(FPlayer fPlayer) {
        String version = versionModule.get(fPlayer);
        if (version == null) {
            version = String.valueOf(PacketEvents.getAPI().getServerManager().getVersion().getReleaseName());
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", version);

        int protocol = PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion();
        if (versionModule.isEnable() && versionModule.getMessage().getProtocol() != -1) {
            protocol = versionModule.getMessage().getProtocol();
        }

        jsonObject.addProperty("protocol", protocol);

        return jsonObject;
    }

    private JsonElement getDescriptionJson(FPlayer fPlayer) {
        JsonElement jsonElement = MOTDModule.next(fPlayer);
        jsonElement = jsonElement == null ? bukkitUtil.getMOTD() : jsonElement;
        return jsonElement;
    }

    private String getFavicon(FPlayer fPlayer) {
        String icon = iconModule.next(fPlayer);
        return icon == null ? null : "data:image/png;base64," + icon;
    }

    private JsonElement getPlayersJson(FPlayer fPlayer) {
        JsonObject playersJson = new JsonObject();

        int max = playersModule.isEnable()
                ? playersModule.getMessage().getMax()
                : bukkitUtil.getMax();
        playersJson.addProperty("max", max);

        int online = playersModule.isEnable()
                ? playersModule.getMessage().getOnline() == -69 ? bukkitUtil.getOnlineCount() : playersModule.getMessage().getOnline()
                : bukkitUtil.getOnlineCount();
        playersJson.addProperty("online", online);

        playersJson.add("sample", getSampleJson(fPlayer));

        return playersJson;
    }

    private JsonElement getSampleJson(FPlayer fPlayer) {
        JsonArray jsonArray = new JsonArray();

        List<Localization.Message.Status.Players.Sample> samples = playersModule.getSamples(fPlayer);
        samples = samples == null ? List.of(new Localization.Message.Status.Players.Sample()) : samples;

        Collection<FPlayer> onlineFPlayers = fPlayerManager.getFPlayers().stream()
                .filter(filter -> !integrationModule.isVanished(filter))
                .toList();

        samples.forEach(sample -> {
            if ("<players>".equalsIgnoreCase(sample.getName())) {
                onlineFPlayers.forEach(player -> {
                    JsonObject playerObject = new JsonObject();
                    playerObject.addProperty("name", player.getName());
                    playerObject.addProperty("id", player.getUuid().toString());
                    jsonArray.add(playerObject);
                });

                return;
            }

            JsonObject playerObject = new JsonObject();
            playerObject.addProperty("name", componentUtil.builder(fPlayer, sample.getName()).legacySerialize());
            playerObject.addProperty("id", sample.getId() == null ? onlineFPlayers.stream().findAny().orElse(FPlayer.UNKNOWN).getUuid().toString() : sample.getId());
            jsonArray.add(playerObject);
        });

        return jsonArray;
    }
}
