package net.flectone.pulse.module.command.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.AbstractModuleCommand;
import net.flectone.pulse.platform.sender.SoundPlayer;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import org.incendo.cloud.context.CommandContext;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AfkModule extends AbstractModuleCommand<Localization.Command> {

    private final FileFacade fileFacade;
    private final net.flectone.pulse.module.message.afk.AfkModule afkMessageModule;
    private final SoundPlayer soundPlayer;

    @Override
    public void onEnable() {
        super.onEnable();

        registerCommand(commandBuilder -> commandBuilder
                .permission(permission().name())
        );
    }

    @Override
    public MessageType messageType() {
        return MessageType.COMMAND_AFK;
    }

    @Override
    public Command.Afk config() {
        return fileFacade.command().afk();
    }

    @Override
    public Permission.Command.Afk permission() {
        return fileFacade.permission().command().afk();
    }

    @Override
    public Localization.Command localization(FEntity sender) {
        return fileFacade.localization(sender).command();
    }

    @Override
    public void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext) {
        if (isModuleDisabledFor(fPlayer, true)) return;

        if (fPlayer.getSetting(SettingText.AFK_SUFFIX) != null) {
            afkMessageModule.remove("afk", fPlayer);
        } else {
            afkMessageModule.setAfkSuffix(fPlayer);
            afkMessageModule.sendAfkMessage(fPlayer.uuid(), true);
        }

        soundPlayer.play(soundOrThrow(), fPlayer);
    }
}