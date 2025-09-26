package net.flectone.pulse.module.command.maintenance;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.maintenance.listener.MaintenancePacketListener;
import net.flectone.pulse.module.command.maintenance.listener.MaintenancePulseListener;
import net.flectone.pulse.module.command.maintenance.model.MaintenanceMetadata;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.IconUtil;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Singleton
public class MaintenanceModule extends AbstractModuleCommand<Localization.Command.Maintenance> {

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final Path iconPath;
    private final PlatformServerAdapter platformServerAdapter;
    private final MessagePipeline messagePipeline;
    private final IconUtil iconUtil;
    private final FLogger fLogger;

    private String icon;

    @Inject
    public MaintenanceModule(FileResolver fileResolver,
                             FPlayerService fPlayerService,
                             PermissionChecker permissionChecker,
                             ListenerRegistry listenerRegistry,
                             @Named("projectPath") Path projectPath,
                             PlatformServerAdapter platformServerAdapter,
                             MessagePipeline messagePipeline,
                             IconUtil iconUtil,
                             FLogger fLogger) {
        super(MessageType.COMMAND_MAINTENANCE);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.listenerRegistry = listenerRegistry;
        this.iconPath = projectPath.resolve("images");
        this.platformServerAdapter = platformServerAdapter;
        this.messagePipeline = messagePipeline;
        this.iconUtil = iconUtil;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission());

        createCooldown(config().getCooldown(), permission().getCooldownBypass());
        createSound(config().getSound(), permission().getSound());

        registerPermission(permission().getJoin());

        listenerRegistry.register(MaintenancePacketListener.class);
        listenerRegistry.register(MaintenancePulseListener.class);

        File file = new File(iconPath.toString() + File.separator + "maintenance.png");

        if (!file.exists()) {
            platformServerAdapter.saveResource("images" + File.separator + "maintenance.png");
        }

        icon = iconUtil.convertIcon(file);

        if (config().isTurnedOn()) {
            kickOnlinePlayers(FPlayer.UNKNOWN);
        }

        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
        );
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        boolean turned = !config().isTurnedOn();

        config().setTurnedOn(turned);

        try {
            fileResolver.save();
        } catch (IOException e) {
            fLogger.warning(e);
            return;
        }

        sendMessage(MaintenanceMetadata.<Localization.Command.Maintenance>builder()
                .sender(fPlayer)
                .format(maintenance -> turned ? maintenance.getFormatTrue() : maintenance.getFormatFalse())
                .turned(turned)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .build()
        );

        if (turned) {
            kickOnlinePlayers(fPlayer);
        }
    }

    @Override
    public Command.Maintenance config() {
        return fileResolver.getCommand().getMaintenance();
    }

    @Override
    public Permission.Command.Maintenance permission() {
        return fileResolver.getPermission().getCommand().getMaintenance();
    }

    @Override
    public Localization.Command.Maintenance localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand().getMaintenance();
    }

    public void sendStatus(User user) {
        if (!isEnable()) return;
        if (!config().isTurnedOn()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(user.getAddress().getAddress());
        fPlayerService.loadColors(fPlayer);

        JsonObject responseJson = new JsonObject();

        Localization.Command.Maintenance localizationMaintenance = localization(fPlayer);

        responseJson.add("version", getVersionJson(localizationMaintenance.getServerVersion()));
        responseJson.add("players", getPlayersJson());

        responseJson.add("description", messagePipeline.builder(fPlayer, localizationMaintenance.getServerDescription()).jsonSerializerBuild());
        responseJson.addProperty("favicon", "data:image/png;base64," + (icon == null ? "" : icon));
        responseJson.addProperty("enforcesSecureChat", false);

        WrapperStatusServerResponse wrapperStatusServerResponse = new WrapperStatusServerResponse(responseJson);
        user.sendPacket(wrapperStatusServerResponse);
    }

    public boolean isAllowed(FPlayer fPlayer) {
        if (!isEnable()) return true;
        if (!config().isTurnedOn()) return true;

        return permissionChecker.check(fPlayer, permission().getJoin());
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
                .filter(filter -> !permissionChecker.check(filter, permission().getJoin()))
                .forEach(fReceiver -> {
                    Component component = messagePipeline.builder(fSender, fReceiver, localization(fReceiver).getKick()).build();
                    fPlayerService.kick(fReceiver, component);
                });
    }
}
