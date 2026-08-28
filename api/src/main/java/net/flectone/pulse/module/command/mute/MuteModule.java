package net.flectone.pulse.module.command.mute;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Moderation;
import net.flectone.pulse.module.ModuleCommand;

/**
 * The /mute command.
 * @author TheFaser
 */
public interface MuteModule extends ModuleCommand {

    @Override
    Command.Mute config();

    @Override
    Permission.Command.Mute permission();

    @Override
    Localization.Command.Mute localization(FPlayer fPlayer);

    /**
     * Registers the tag that renders a player's mute in a message.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

    /**
     * The marker appended to a muted player's name.
     *
     * @param fPlayer the muted player
     * @param fReceiver the reader
     * @return the suffix, or an empty string if the player is not muted
     */
    String getMuteSuffix(FPlayer fPlayer, FPlayer fReceiver);

    /**
     * Tells the muted player they have been silenced.
     *
     * @param fModerator the moderator
     * @param fReceiver the muted player
     * @param mute the mute that was issued
     */
    void sendForTarget(FEntity fModerator, FPlayer fReceiver, Moderation mute);

}
