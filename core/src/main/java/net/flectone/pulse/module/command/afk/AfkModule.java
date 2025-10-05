package net.flectone.pulse.module.command.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import org.incendo.cloud.context.CommandContext;

@Singleton
public class AfkModule extends AbstractModuleCommand<Localization.Command> {

    private final FileResolver fileResolver;
    private final net.flectone.pulse.module.message.afk.AfkModule afkMessageModule;
    private final SoundPlayer soundPlayer;

    @Inject
    public AfkModule(FileResolver fileResolver,
                     net.flectone.pulse.module.message.afk.AfkModule afkMessageModule,
                     SoundPlayer soundPlayer) {
        super(MessageType.COMMAND_AFK);

        this.fileResolver = fileResolver;
        this.afkMessageModule = afkMessageModule;
        this.soundPlayer = soundPlayer;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().getName())
        );
    }

    @Override
    public Command.Afk config() {
        return fileResolver.getCommand().getAfk();
    }

    @Override
    public Permission.Command.Afk permission() {
        return fileResolver.getPermission().getCommand().getAfk();
    }

    @Override
    public Localization.Command localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getCommand();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        if (fPlayer.getSetting(SettingText.AFK_SUFFIX) != null) {
            afkMessageModule.remove("afk", fPlayer);
        } else {
            afkMessageModule.setAfk(fPlayer);
        }

        soundPlayer.play(getModuleSound(), fPlayer);
    }
}