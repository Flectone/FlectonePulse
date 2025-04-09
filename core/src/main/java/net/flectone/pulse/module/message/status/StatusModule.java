package net.flectone.pulse.module.message.status;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.adapter.PlatformServerAdapter;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.status.icon.IconModule;
import net.flectone.pulse.module.message.status.listener.StatusPacketListener;
import net.flectone.pulse.module.message.status.motd.MOTDModule;
import net.flectone.pulse.module.message.status.players.PlayersModule;
import net.flectone.pulse.module.message.status.version.VersionModule;
import net.flectone.pulse.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.pipeline.MessagePipeline;

import java.util.Collection;
import java.util.List;

@Singleton
public class StatusModule extends AbstractModule {

    private final Message.Status message;
    private final Permission.Message.Status permission;

    private final MOTDModule MOTDModule;
    private final IconModule iconModule;
    private final PlayersModule playersModule;
    private final VersionModule versionModule;
    private final MessagePipeline messagePipeline;
    private final PlatformServerAdapter platformServerAdapter;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;
    private final IntegrationModule integrationModule;

    @Inject
    public StatusModule(FileManager fileManager,
                        MOTDModule MOTDModule,
                        IconModule iconModule,
                        PlayersModule playersModule,
                        VersionModule versionModule,
                        MessagePipeline messagePipeline,
                        PlatformServerAdapter platformServerAdapter,
                        FPlayerService fPlayerService,
                        ListenerRegistry listenerRegistry,
                        IntegrationModule integrationModule) {
        this.MOTDModule = MOTDModule;
        this.iconModule = iconModule;
        this.playersModule = playersModule;
        this.versionModule = versionModule;
        this.messagePipeline = messagePipeline;
        this.platformServerAdapter = platformServerAdapter;
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.integrationModule = integrationModule;

        message = fileManager.getMessage().getStatus();
        permission = fileManager.getPermission().getMessage().getStatus();

        addChildren(MOTDModule.class);
        addChildren(IconModule.class);
        addChildren(PlayersModule.class);
        addChildren(VersionModule.class);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        listenerRegistry.register(StatusPacketListener.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void send(User user) {
        FPlayer fPlayer = fPlayerService.getFPlayer(user.getAddress().getAddress());
        if (checkModulePredicates(fPlayer)) return;

        fPlayerService.loadColors(fPlayer);

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
        jsonElement = jsonElement == null ? platformServerAdapter.getMOTD() : jsonElement;
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
                : platformServerAdapter.getMaxPlayers();
        playersJson.addProperty("max", max);

        int online = playersModule.isEnable()
                ? playersModule.getMessage().getOnline() == -69 ? platformServerAdapter.getOnlinePlayerCount() : playersModule.getMessage().getOnline()
                : platformServerAdapter.getOnlinePlayerCount();
        playersJson.addProperty("online", online);

        playersJson.add("sample", getSampleJson(fPlayer));

        return playersJson;
    }

    private JsonElement getSampleJson(FPlayer fPlayer) {
        JsonArray jsonArray = new JsonArray();

        List<Localization.Message.Status.Players.Sample> samples = playersModule.getSamples(fPlayer);
        samples = samples == null ? List.of(new Localization.Message.Status.Players.Sample()) : samples;

        Collection<FPlayer> onlineFPlayers = fPlayerService.getFPlayers().stream()
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
            playerObject.addProperty("name", messagePipeline.builder(fPlayer, sample.getName()).legacySerializerBuild());
            playerObject.addProperty("id", sample.getId() == null ? onlineFPlayers.stream().findAny().orElse(FPlayer.UNKNOWN).getUuid().toString() : sample.getId());
            jsonArray.add(playerObject);
        });

        return jsonArray;
    }
}
