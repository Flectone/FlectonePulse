package net.flectone.pulse.module.command.maintenance;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerServerData;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.command.maintenance.listener.MaintenancePacketListener;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.controller.ModuleCommandController;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.processing.converter.IconConvertor;
import net.flectone.pulse.platform.formatter.ServerStatusFormatter;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

import java.nio.file.Path;

@Singleton
public class MinecraftMaintenanceModule extends MaintenanceModule {

    private final FPlayerService fPlayerService;
    private final ModuleController moduleController;
    private final ListenerRegistry listenerRegistry;
    private final ServerStatusFormatter statusUtil;

    @Inject
    public MinecraftMaintenanceModule(FileFacade fileFacade,
                                      PermissionChecker permissionChecker,
                                      ListenerRegistry listenerRegistry,
                                      @Named("imagePath") Path iconPath,
                                      PlatformServerAdapter platformServerAdapter,
                                      PlatformPlayerAdapter platformPlayerAdapter,
                                      FPlayerService fPlayerService,
                                      MessagePipeline messagePipeline,
                                      MessageDispatcher messageDispatcher,
                                      ModuleController moduleController,
                                      ModuleCommandController commandModuleController,
                                      IconConvertor iconUtil,
                                      ServerStatusFormatter statusUtil,
                                      FLogger fLogger) {
        super(fileFacade, permissionChecker, listenerRegistry, iconPath, platformServerAdapter, platformPlayerAdapter, fPlayerService, messagePipeline, messageDispatcher, moduleController, commandModuleController, iconUtil, fLogger);

        this.fPlayerService = fPlayerService;
        this.moduleController = moduleController;
        this.listenerRegistry = listenerRegistry;
        this.statusUtil = statusUtil;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MaintenancePacketListener.class);
    }

    public void updateServerData(PacketSendEvent event) {
        if (!moduleController.isEnable(this)) return;
        if (!config().turnedOn()) return;

        User user = event.getUser();
        FPlayer fPlayer = fPlayerService.getFPlayer(user.getAddress().getAddress());

        event.markForReEncode(true);

        WrapperPlayServerServerData wrapperPlayServerServerData = new WrapperPlayServerServerData(event);
        wrapperPlayServerServerData.setIcon(statusUtil.formatIcon(icon));
        wrapperPlayServerServerData.setMOTD(statusUtil.createMOTD(fPlayer, user, localization(fPlayer).serverDescription()));
    }

    public void sendStatus(User user) {
        if (!moduleController.isEnable(this)) return;
        if (!config().turnedOn()) return;

        FPlayer fPlayer = fPlayerService.loadColors(fPlayerService.getFPlayer(user.getAddress().getAddress()));

        JsonObject responseJson = new JsonObject();

        Localization.Command.Maintenance localizationMaintenance = localization(fPlayer);

        responseJson.add("version", getVersionJson(localizationMaintenance.serverVersion()));
        responseJson.add("players", getPlayersJson());
        responseJson.add("description", statusUtil.formatDescription(fPlayer, user, localizationMaintenance.serverDescription()));

        String favicon = statusUtil.formatIcon(icon);
        if (favicon != null) {
            responseJson.addProperty("favicon", favicon);
        }

        responseJson.addProperty("enforcesSecureChat", false);

        WrapperStatusServerResponse wrapperStatusServerResponse = new WrapperStatusServerResponse(responseJson);
        user.sendPacket(wrapperStatusServerResponse);
    }

    private JsonElement getVersionJson(String message) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", message);
        jsonObject.addProperty("protocol", -1);

        return jsonObject;
    }

    private JsonElement getPlayersJson() {
        JsonObject playersJson = new JsonObject();

        playersJson.addProperty("max", -1);
        playersJson.addProperty("online", -1);

        playersJson.add("sample", new JsonArray());

        return playersJson;
    }

}
