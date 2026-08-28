package net.flectone.pulse.module.command.toponline;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;

import java.util.Optional;

/**
 * The /toponline command, which ranks players by time played.
 * @author TheFaser
 */
public interface ToponlineModule extends ModuleCommand {

    @Override
    Command.Toponline config();

    @Override
    Permission.Command.Toponline permission();

    @Override
    Localization.Command.Toponline localization(FPlayer fPlayer);

    /**
     * Registers the tags that render entries of the ranking.
     *
     * @param messageContext the message being formatted
     * @return the context with the tags added
     */
    MessageContext addTag(MessageContext messageContext);

    /**
     * The player at a place in the ranking.
     *
     * @param rawPosition the place, as written in the message
     * @return the player, or empty if the place is out of range or not a number
     */
    Optional<FPlayer> getPlayerByPosition(String rawPosition);

    /**
     * The player at a place in the ranking.
     *
     * @param position the place, starting at 1
     * @return the player, or empty if the place is out of range
     */
    Optional<FPlayer> getPlayerByPosition(int position);

}
