package net.flectone.pulse.module.command.maintenance;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.IconUtil;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;

import java.nio.file.Path;

@Singleton
public class MinecraftMaintenanceModule extends MaintenanceModule {

    private final FPlayerService fPlayerService;
    private final MessagePipeline messagePipeline;

    @Inject
    public MinecraftMaintenanceModule(FileFacade fileFacade,
                                      PermissionChecker permissionChecker,
                                      ListenerRegistry listenerRegistry,
                                      @Named("imagePath") Path iconPath,
                                      PlatformServerAdapter platformServerAdapter,
                                      PlatformPlayerAdapter platformPlayerAdapter,
                                      FPlayerService fPlayerService,
                                      MessagePipeline messagePipeline,
                                      IconUtil iconUtil,
                                      FLogger fLogger) {
        super(fileFacade, permissionChecker, listenerRegistry, iconPath, platformServerAdapter, platformPlayerAdapter, fPlayerService, messagePipeline, iconUtil, fLogger);

        this.fPlayerService = fPlayerService;
        this.messagePipeline = messagePipeline;
    }

    public void sendStatus(Object player) {
        if (!isEnable()) return;
        if (!config().turnedOn()) return;
        if (!(player instanceof User user)) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(user.getAddress().getAddress());
        fPlayerService.loadColors(fPlayer);

        JsonObject responseJson = new JsonObject();

        Localization.Command.Maintenance localizationMaintenance = localization(fPlayer);

        responseJson.add("version", getVersionJson(localizationMaintenance.serverVersion()));
        responseJson.add("players", getPlayersJson());

        MessageContext context = messagePipeline.createContext(fPlayer, localizationMaintenance.serverDescription());
        responseJson.add("description", messagePipeline.buildJson(context));

        responseJson.addProperty("favicon", "data:image/png;base64," + (icon == null ? "" : icon));
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
