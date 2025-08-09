package net.flectone.pulse.module.command.maintenance;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.status.server.WrapperStatusServerResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.maintenance.listener.MaintenancePacketListener;
import net.flectone.pulse.module.command.maintenance.listener.MaintenancePulseListener;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.IconUtil;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;

import java.io.File;
import java.nio.file.Path;

@Singleton
public class MaintenanceModule extends AbstractModuleCommand<Localization.Command.Maintenance> {

    private final Command.Maintenance command;
    private final Permission.Command.Maintenance permission;
    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final Path iconPath;
    private final PlatformServerAdapter platformServerAdapter;
    private final MessagePipeline messagePipeline;
    private final IconUtil iconUtil;

    private String icon;

    @Inject
    public MaintenanceModule(FileResolver fileResolver,
                             FPlayerService fPlayerService,
                             PermissionChecker permissionChecker,
                             ListenerRegistry listenerRegistry,
                             @Named("projectPath") Path projectPath,
                             PlatformServerAdapter platformServerAdapter,
                             MessagePipeline messagePipeline,
                             IconUtil iconUtil) {
        super(module -> module.getCommand().getMaintenance(), Command::getMaintenance);

        this.command = fileResolver.getCommand().getMaintenance();
        this.permission = fileResolver.getPermission().getCommand().getMaintenance();
        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.permissionChecker = permissionChecker;
        this.listenerRegistry = listenerRegistry;
        this.iconPath = projectPath.resolve("images");
        this.platformServerAdapter = platformServerAdapter;
        this.messagePipeline = messagePipeline;
        this.iconUtil = iconUtil;
    }

    @Override
    public void onEnable() {
        // if FPlayer.UNKNOWN (all-permissions) fails check (method will return true),
        // a maintenance plugin is intercepting this command
        if (isModuleDisabledFor(FPlayer.UNKNOWN)) return;

        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerPermission(permission.getJoin());

        listenerRegistry.register(MaintenancePacketListener.class);
        listenerRegistry.register(MaintenancePulseListener.class);

        File file = new File(iconPath.toString() + File.separator + "maintenance.png");

        if (!file.exists()) {
            platformServerAdapter.saveResource("images" + File.separator + "maintenance.png");
        }

        icon = iconUtil.convertIcon(file);

        if (command.isTurnedOn()) {
            kickOnlinePlayers(FPlayer.UNKNOWN);
        }

        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
        );

        addPredicate(this::checkCooldown);
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer)) return;

        boolean turned = !command.isTurnedOn();

        command.setTurnedOn(turned);
        fileResolver.save();

        builder(fPlayer)
                .destination(command.getDestination())
                .format(s -> turned ? s.getFormatTrue() : s.getFormatFalse())
                .sendBuilt();

        if (turned) {
            kickOnlinePlayers(fPlayer);
        }
    }

    public void sendStatus(User user) {
        if (!isEnable()) return;
        if (!command.isTurnedOn()) return;

        FPlayer fPlayer = fPlayerService.getFPlayer(user.getAddress().getAddress());
        fPlayerService.loadColors(fPlayer);

        JsonObject responseJson = new JsonObject();

        Localization.Command.Maintenance localizationMaintenance = resolveLocalization(fPlayer);

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
        if (!command.isTurnedOn()) return true;

        return permissionChecker.check(fPlayer, permission.getJoin());
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
        fPlayerService.getFPlayers()
                .stream()
                .filter(filter -> !permissionChecker.check(filter, permission.getJoin()))
                .forEach(fReceiver -> {
                    Component component = messagePipeline.builder(fSender, fReceiver, resolveLocalization(fReceiver).getKick()).build();
                    fPlayerService.kick(fReceiver, component);
                });
    }
}
