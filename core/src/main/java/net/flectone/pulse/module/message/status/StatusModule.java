package net.flectone.pulse.module.message.status;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.status.icon.IconModule;
import net.flectone.pulse.module.message.status.listener.StatusPacketListener;
import net.flectone.pulse.module.message.status.motd.MOTDModule;
import net.flectone.pulse.module.message.status.players.PlayersModule;
import net.flectone.pulse.module.message.status.version.VersionModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;

import java.net.InetAddress;
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
    private final PacketProvider packetProvider;

    @Inject
    public StatusModule(FileResolver fileResolver,
                        MOTDModule MOTDModule,
                        IconModule iconModule,
                        PlayersModule playersModule,
                        VersionModule versionModule,
                        MessagePipeline messagePipeline,
                        PlatformServerAdapter platformServerAdapter,
                        FPlayerService fPlayerService,
                        ListenerRegistry listenerRegistry,
                        IntegrationModule integrationModule,
                        PacketProvider packetProvider) {
        this.message = fileResolver.getMessage().getStatus();
        this.permission = fileResolver.getPermission().getMessage().getStatus();
        this.MOTDModule = MOTDModule;
        this.iconModule = iconModule;
        this.playersModule = playersModule;
        this.versionModule = versionModule;
        this.messagePipeline = messagePipeline;
        this.platformServerAdapter = platformServerAdapter;
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.integrationModule = integrationModule;
        this.packetProvider = packetProvider;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        listenerRegistry.register(StatusPacketListener.class);

        addChildren(MOTDModule.class);
        addChildren(IconModule.class);
        addChildren(PlayersModule.class);
        addChildren(VersionModule.class);
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    public void update(PacketSendEvent event) {
        InetAddress inetAddress = event.getUser().getAddress().getAddress();
        FPlayer fPlayer = fPlayerService.getFPlayer(inetAddress);
        if (isModuleDisabledFor(fPlayer)) return;

        fPlayerService.loadSettings(fPlayer);
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

        event.markForReEncode(true);

        WrapperStatusServerResponse wrapperStatusServerResponse = new WrapperStatusServerResponse(event);
        wrapperStatusServerResponse.setComponent(responseJson);
    }

    private JsonElement getVersionJson(FPlayer fPlayer) {
        String version = versionModule.get(fPlayer);
        if (version == null) {
            version = packetProvider.getServerVersion().getReleaseName();
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", version);

        int protocol = packetProvider.getServerVersion().getProtocolVersion();
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

        Collection<FPlayer> onlineFPlayers = fPlayerService.getVisibleFPlayersFor(fPlayer);
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
