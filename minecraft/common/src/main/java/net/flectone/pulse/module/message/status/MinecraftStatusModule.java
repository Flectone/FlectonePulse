package net.flectone.pulse.module.message.status;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.EventDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.StatusResponseEvent;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.AbstractModule;
import net.flectone.pulse.module.message.status.icon.IconModule;
import net.flectone.pulse.module.message.status.listener.StatusPacketListener;
import net.flectone.pulse.module.message.status.motd.MOTDModule;
import net.flectone.pulse.module.message.status.players.PlayersModule;
import net.flectone.pulse.module.message.status.version.VersionModule;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.provider.PacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;

@Singleton
public class MinecraftStatusModule extends StatusModule {

    private final MOTDModule MOTDModule;
    private final IconModule iconModule;
    private final PlayersModule playersModule;
    private final VersionModule versionModule;
    private final MessagePipeline messagePipeline;
    private final PlatformServerAdapter platformServerAdapter;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;
    private final PacketProvider packetProvider;
    private final EventDispatcher eventDispatcher;

    @Inject
    public MinecraftStatusModule(FileFacade fileFacade,
                                 MOTDModule motdModule,
                                 IconModule iconModule,
                                 PlayersModule playersModule,
                                 VersionModule versionModule,
                                 MessagePipeline messagePipeline,
                                 PlatformServerAdapter platformServerAdapter,
                                 FPlayerService fPlayerService,
                                 ListenerRegistry listenerRegistry,
                                 PacketProvider packetProvider,
                                 EventDispatcher eventDispatcher) {
        super(fileFacade);

        this.MOTDModule = motdModule;
        this.iconModule = iconModule;
        this.playersModule = playersModule;
        this.versionModule = versionModule;
        this.messagePipeline = messagePipeline;
        this.platformServerAdapter = platformServerAdapter;
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.packetProvider = packetProvider;
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public ImmutableList.Builder<@NonNull Class<? extends AbstractModule>> childrenBuilder() {
        return super.childrenBuilder().add(
                MOTDModule.class,
                IconModule.class,
                PlayersModule.class,
                VersionModule.class
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(StatusPacketListener.class);
    }

    public void update(PacketSendEvent event) {
        InetAddress inetAddress = event.getUser().getAddress().getAddress();
        FPlayer fPlayer = fPlayerService.getFPlayer(inetAddress);
        if (isModuleDisabledFor(fPlayer)) return;

        fPlayer = fPlayerService.loadColors(fPlayer);

        JsonObject responseJson = new JsonObject();

        responseJson.add("version", getVersionJson(fPlayer));
        responseJson.add("players", getPlayersJson(fPlayer));
        responseJson.add("description", getDescriptionJson(fPlayer));

        String favicon = getFavicon(fPlayer);
        if (favicon != null) {
            responseJson.addProperty("favicon", favicon);
        }

        responseJson.addProperty("enforcesSecureChat", false);

        StatusResponseEvent responseEvent = eventDispatcher.dispatch(new StatusResponseEvent(responseJson));
        if (responseEvent.cancelled()) return;

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
        if (versionModule.isEnable() && versionModule.config().protocol() != -1) {
            protocol = versionModule.config().protocol();
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
                ? playersModule.config().max()
                : platformServerAdapter.getMaxPlayers();
        playersJson.addProperty("max", max);

        int online = playersModule.isEnable()
                ? playersModule.config().online() == -69 ? platformServerAdapter.getOnlinePlayerCount() : playersModule.config().online()
                : platformServerAdapter.getOnlinePlayerCount();
        playersJson.addProperty("online", online);

        playersJson.add("sample", getSampleJson(fPlayer));

        return playersJson;
    }

    private JsonElement getSampleJson(FPlayer fPlayer) {
        JsonArray jsonArray = new JsonArray();

        List<Localization.Message.Status.Players.Sample> samples = playersModule.getSamples(fPlayer);
        samples = samples == null ? List.of(new Localization.Message.Status.Players.Sample("<players>", null)) : samples;

        Collection<FPlayer> onlineFPlayers = fPlayerService.getVisibleFPlayersFor(fPlayer);
        samples.forEach(sample -> {
            if ("<players>".equalsIgnoreCase(sample.name())) {
                onlineFPlayers.forEach(player -> {
                    JsonObject playerObject = new JsonObject();
                    playerObject.addProperty("name", player.name());
                    playerObject.addProperty("id", player.uuid().toString());
                    jsonArray.add(playerObject);
                });

                return;
            }

            JsonObject playerObject = new JsonObject();

            MessageContext sampleContext = messagePipeline.createContext(fPlayer, sample.name());
            playerObject.addProperty("name", messagePipeline.buildLegacy(sampleContext));
            playerObject.addProperty("id", sample.id() == null ? onlineFPlayers.stream().findAny().orElse(FPlayer.UNKNOWN).uuid().toString() : sample.id());

            jsonArray.add(playerObject);
        });

        return jsonArray;
    }
}
