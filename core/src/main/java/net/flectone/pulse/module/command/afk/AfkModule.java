package net.flectone.pulse.module.command.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.processing.resolver.FileResolver;
import org.incendo.cloud.context.CommandContext;

@Singleton
public class AfkModule extends AbstractModuleCommand<Localization.Command> {

    private final Command.Afk command;
    private final Permission.Command.Afk permission;
    private final net.flectone.pulse.module.message.afk.AfkModule afkMessageModule;

    @Inject
    public AfkModule(FileResolver fileResolver,
                     net.flectone.pulse.module.message.afk.AfkModule afkMessageModule) {
        super(Localization::getCommand, Command::getAfk,fPlayer -> fPlayer.isSetting(FPlayer.Setting.AFK));

        this.command = fileResolver.getCommand().getAfk();
        this.permission = fileResolver.getPermission().getCommand().getAfk();
        this.afkMessageModule = afkMessageModule;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        createCooldown(command.getCooldown(), permission.getCooldownBypass());
        createSound(command.getSound(), permission.getSound());

        registerCommand(commandBuilder -> commandBuilder
                .permission(permission.getName())
        );

        addPredicate(this::checkCooldown);
        addPredicate(fPlayer -> !afkMessageModule.isEnable());
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (checkModulePredicates(fPlayer)) return;

        if (fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX)) {
            afkMessageModule.remove("afk", fPlayer);
        } else {
            afkMessageModule.setAfk(fPlayer);
        }

        playSound(fPlayer);
    }
}