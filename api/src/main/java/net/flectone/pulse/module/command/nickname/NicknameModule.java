package net.flectone.pulse.module.command.nickname;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;
import net.flectone.pulse.util.constant.ModuleName;
import org.incendo.cloud.context.CommandContext;

import java.util.UUID;

/**
 * The /nickname command, which changes the name a player is shown under.
 * @author TheFaser
 */
public interface NicknameModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    /**
     * Runs the form of the command that changes someone else's nickname.
     *
     * @param fPlayer who ran it
     * @param commandContext the parsed arguments
     */
    void executeOther(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    @Override
    ModuleName name();

    @Override
    Command.Nickname config();

    @Override
    Permission.Command.Nickname permission();

    @Override
    Localization.Command.Nickname localization(FPlayer fPlayer);

    /**
     * Changes a player's nickname and announces it.
     *
     * @param fPlayer who made the change
     * @param fTarget whose nickname changed
     * @param nickname the new nickname
     */
    void changeName(FPlayer fPlayer, FPlayer fTarget, String nickname);

    /**
     * Announces a nickname change.
     *
     * @param fPlayer whose nickname changed
     * @param nickname the new nickname
     * @param metadataUUID the shared message id
     */
    void sendMessageWithUpdatedNickname(FEntity fPlayer, String nickname, UUID metadataUUID);

    /**
     * Registers the tag that renders a player's nickname.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

}
