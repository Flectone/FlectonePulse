package net.flectone.pulse.module.command.maintenance;

import com.google.common.collect.ImmutableList;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.setting.PermissionSetting;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.command.maintenance.listener.MaintenancePulseListener;
import net.flectone.pulse.module.command.maintenance.model.MaintenanceMetadata;
import net.flectone.pulse.platform.adapter.PlatformServerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.util.IconUtil;
import net.flectone.pulse.util.checker.PermissionChecker;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.file.FileFacade;
import net.flectone.pulse.util.logging.FLogger;
import org.incendo.cloud.context.CommandContext;

import java.io.File;
import java.nio.file.Path;

public abstract class MaintenanceModule extends AbstractModuleCommand<Localization.Command.Maintenance> {

    private final FileFacade fileFacade;
    private final PermissionChecker permissionChecker;
    private final ListenerRegistry listenerRegistry;
    private final Path iconPath;
    private final PlatformServerAdapter platformServerAdapter;
    private final IconUtil iconUtil;
    private final FLogger fLogger;

    protected String icon;

    protected MaintenanceModule(FileFacade fileFacade,
                                PermissionChecker permissionChecker,
                                ListenerRegistry listenerRegistry,
                                Path iconPath,
                                PlatformServerAdapter platformServerAdapter,
                                IconUtil iconUtil,
                                FLogger fLogger) {
        this.fileFacade = fileFacade;
        this.permissionChecker = permissionChecker;
        this.listenerRegistry = listenerRegistry;
        this.iconPath = iconPath;
        this.platformServerAdapter = platformServerAdapter;
        this.iconUtil = iconUtil;
        this.fLogger = fLogger;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        listenerRegistry.register(MaintenancePulseListener.class);

        File file = iconPath.resolve("maintenance.png").toFile();

        if (!file.exists()) {
            platformServerAdapter.saveResource("images/maintenance.png");
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
                .sound(soundOrThrow())
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

    public boolean isAllowed(FPlayer fPlayer) {
        if (!isEnable()) return true;
        if (!config().turnedOn()) return true;

        return permissionChecker.check(fPlayer, permission().join());
    }

    public abstract void sendStatus(Object player);

    protected abstract void kickOnlinePlayers(FPlayer fSender);

}
