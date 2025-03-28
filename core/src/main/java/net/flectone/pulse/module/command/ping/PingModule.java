package net.flectone.pulse.module.command.ping;

import lombok.Getter;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.CommandUtil;

import java.util.Optional;

public abstract class PingModule extends AbstractModuleCommand<Localization.Command.Ping> {

    @Getter private final Command.Ping command;
    @Getter private final Permission.Command.Ping permission;

    private final FPlayerService fPlayerService;
    private final CommandUtil commandUtil;
    private final IntegrationModule integrationModule;

    public PingModule(FileManager fileManager,
                      FPlayerService fPlayerService,
                      CommandUtil commandUtil,
                      IntegrationModule integrationModule) {
        super(localization -> localization.getCommand().getPing(), null);

        this.fPlayerService = fPlayerService;
        this.commandUtil = commandUtil;
        this.integrationModule = integrationModule;

        command = fileManager.getCommand().getPing();
        permission = fileManager.getPermission().getCommand().getPing();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void onCommand(FPlayer fPlayer, Object arguments) {
        if (checkModulePredicates(fPlayer)) return;

        Optional<Object> target = commandUtil.getOptional(0, arguments);

        FPlayer fTarget = target.map(object -> fPlayerService.getFPlayer(String.valueOf(object))).orElse(fPlayer);
        if (fTarget.isUnknown() || (!fPlayer.equals(fTarget) && integrationModule.isVanished(fTarget))) {
            builder(fPlayer)
                    .format(Localization.Command.Ping::getNullPlayer)
                    .sendBuilt();
            return;
        }

        builder(fTarget)
                .receiver(fPlayer)
                .destination(command.getDestination())
                .format(Localization.Command.Ping::getFormat)
                .sound(getSound())
                .sendBuilt();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        getCommand().getAliases().forEach(commandUtil::unregister);

        createCommand();
    }

    @Override
    public boolean isConfigEnable() {
        return command.isEnable();
    }
}
