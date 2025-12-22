package net.flectone.pulse.module.command.maintenance;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.maintenance.listener.MaintenancePacketListener;
import net.flectone.pulse.module.command.maintenance.listener.MaintenancePulseListener;
import net.flectone.pulse.module.command.maintenance.model.MaintenanceMetadata;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.IconUtil;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;

import java.io.File;
import java.nio.file.Path;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MaintenanceModule extends AbstractModuleCommand<Localization.Command.Maintenance> {

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final @Named("imagePath") Path iconPath;
    private final PlatformServerAdapter platformServerAdapter;
    private final MessagePipeline messagePipeline;
    private final IconUtil iconUtil;
    private final FLogger fLogger;

    private String icon;

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MaintenancePacketListener.class);
        listenerRegistry.register(MaintenancePulseListener.class);

        File file = iconPath.resolve("maintenance.png").toFile();

        if (!file.exists()) {
            platformServerAdapter.saveResource("images" + File.separator + "maintenance.png");
        }

        icon = iconUtil.convertIcon(file);

        if (config().turnedOn()) {
            kickOnlinePlayers(FPlayer.UNKNOWN);
        }

        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
        );
    }

    @Override
    public ImmutableList.Builder<PermissionSetting> permissionBuilder() {
        return super.permissionBuilder()
                .add(permission().join());
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        boolean turned = !config().turnedOn();

        fileFacade.updateFilePack(filePack -> filePack.withCommand(filePack.command().withMaintenance(filePack.command().maintenance().withTurnedOn(turned))));

        try {
            fileFacade.saveFiles();
        } catch (Exception e) {
            fLogger.warning(e);
            return;
        }

        sendMessage(MaintenanceMetadata.<Localization.Command.Maintenance>builder()
                .sender(fPlayer)
                .format(maintenance -> turned ? maintenance.formatTrue() : maintenance.formatFalse())
                .turned(turned)
                .destination(config().destination())
                .sound(getModuleSound())
                .build()
        );

        if (turned) {
            kickOnlinePlayers(fPlayer);
        }
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_MAINTENANCE;
    }

    @Override
    public Command.Maintenance config() {
        return fileFacade.command().maintenance();
    }

    @Override
    public Permission.Command.Maintenance permission() {
        return fileFacade.permission().command().maintenance();
    }

    @Override
    public Localization.Command.Maintenance localization(FEntity sender) {
        return fileFacade.localization(sender).command().maintenance();
    }

    public void sendStatus(User user) {
        if (!isEnable()) return;
        if (!config().turnedOn()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(user.getAddress().getAddress());
        fPlayerService.loadColors(fPlayer);

        JsonObject responseJson = new JsonObject();

        Localization.Command.Maintenance localizationMaintenance = localization(fPlayer);

        responseJson.add("version", getVersionJson(localizationMaintenance.serverVersion()));
        responseJson.add("players", getPlayersJson());

        responseJson.add("description", messagePipeline.builder(fPlayer, localizationMaintenance.serverDescription()).jsonSerializerBuild());
        responseJson.addProperty("favicon", "data:image/png;base64," + (icon == null ? "" : icon));
        responseJson.addProperty("enforcesSecureChat", false);

        WrapperStatusServerResponse wrapperStatusServerResponse = new WrapperStatusServerResponse(responseJson);
        user.sendPacket(wrapperStatusServerResponse);
    }

    public boolean isAllowed(FPlayer fPlayer) {
        if (!isEnable()) return true;
        if (!config().turnedOn()) return true;

        return permissionChecker.check(fPlayer, permission().join());
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

    private void kickOnlinePlayers(FPlayer fSender) {
        fPlayerService.getOnlineFPlayers()
                .stream()
                .filter(filter -> !permissionChecker.check(filter, permission().join()))
                .forEach(fReceiver -> {
                    Component component = messagePipeline.builder(fSender, fReceiver, localization(fReceiver).kick()).build();
                    fPlayerService.kick(fReceiver, component);
                });
    }
}
