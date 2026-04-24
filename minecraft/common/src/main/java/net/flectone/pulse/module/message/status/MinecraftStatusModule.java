package net.flectone.pulse.module.message.status;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.common.collect.ImmutableSet;
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
import net.flectone.pulse.module.ModuleSimple;
import net.flectone.pulse.module.message.status.icon.MinecraftIconModule;
import net.flectone.pulse.module.message.status.listener.MinecraftPacketStatusListener;
import net.flectone.pulse.module.message.status.motd.MinecraftMOTDModule;
import net.flectone.pulse.module.message.status.players.MinecraftPlayersModule;
import net.flectone.pulse.module.message.status.version.MinecraftVersionModule;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.MinecraftServerStatusFormatter;
import net.flectone.pulse.platform.provider.MinecraftPacketProvider;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.file.FileFacade;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

@Singleton
public class MinecraftStatusModule extends StatusModule {

    private final MinecraftMOTDModule MOTDModule;
    private final MinecraftIconModule iconModule;
    private final MinecraftPlayersModule playersModule;
    private final MinecraftVersionModule versionModule;
    private final MessagePipeline messagePipeline;
    private final FPlayerService fPlayerService;
    private final ListenerRegistry listenerRegistry;
    private final MinecraftPacketProvider packetProvider;
    private final EventDispatcher eventDispatcher;
    private final ModuleController moduleController;
    private final MinecraftServerStatusFormatter statusUtil;

    @Inject
    public MinecraftStatusModule(FileFacade fileFacade,
                                 MinecraftMOTDModule motdModule,
                                 MinecraftIconModule iconModule,
                                 MinecraftPlayersModule playersModule,
                                 MinecraftVersionModule versionModule,
                                 MessagePipeline messagePipeline,
                                 FPlayerService fPlayerService,
                                 ListenerRegistry listenerRegistry,
                                 MinecraftPacketProvider packetProvider,
                                 EventDispatcher eventDispatcher,
                                 ModuleController moduleController,
                                 MinecraftServerStatusFormatter statusUtil) {
        super(fileFacade);

        this.MOTDModule = motdModule;
        this.iconModule = iconModule;
        this.playersModule = playersModule;
        this.versionModule = versionModule;
        this.messagePipeline = messagePipeline;
        this.fPlayerService = fPlayerService;
        this.listenerRegistry = listenerRegistry;
        this.packetProvider = packetProvider;
        this.eventDispatcher = eventDispatcher;
        this.moduleController = moduleController;
        this.statusUtil = statusUtil;
    }

    @Override
    public ImmutableSet.Builder<@NonNull Class<? extends ModuleSimple>> childrenBuilder() {
        return super.childrenBuilder().add(
                MinecraftMOTDModule.class,
                MinecraftIconModule.class,
                MinecraftPlayersModule.class,
                MinecraftVersionModule.class
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MinecraftPacketStatusListener.class);
    }

    public void update(PacketSendEvent event) {
        User user = event.getUser();

        FPlayer fPlayer = fPlayerService.getFPlayer(user.getAddress().getAddress());
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        fPlayer = fPlayerService.loadColors(fPlayer);

        JsonObject responseJson = new JsonObject();

        responseJson.add("version", getVersionJson(fPlayer));
        responseJson.add("players", getPlayersJson(fPlayer));
        responseJson.add("description", statusUtil.formatDescription(MOTDModule.next(fPlayer, user)));

        String favicon = statusUtil.formatIcon(iconModule.next(fPlayer));
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
        if (moduleController.isEnable(versionModule) && versionModule.config().protocol() != -1) {
            protocol = versionModule.config().protocol();
        }

        jsonObject.addProperty("protocol", protocol);

        return jsonObject;
    }

    private JsonElement getPlayersJson(FPlayer fPlayer) {
        JsonObject playersJson = new JsonObject();

        playersJson.addProperty("max", playersModule.getMaxOnline(fPlayer));
        playersJson.addProperty("online", playersModule.getOnline(fPlayer));

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
