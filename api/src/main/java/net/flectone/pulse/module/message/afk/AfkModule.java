package net.flectone.pulse.module.message.afk;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;
import org.jspecify.annotations.NonNull;

/**
 * Marks players away automatically once they stop moving, and announces when they come back.
 * @author TheFaser
 */
public interface AfkModule extends ModuleLocalization {

    @Override
    ModuleName name();

    @Override
    Message.Afk config();

    @Override
    Permission.Message.Afk permission();

    @Override
    Localization.Message.Afk localization(FPlayer fPlayer);

    /**
     * Registers the tag that renders a player's away marker.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

    /**
     * Brings everyone back from afk, used on reload and shutdown.
     *
     * @param action what caused it, for the announcement
     */
    void removeAllAfkPlayers(String action);

    /**
     * Brings one player back from afk and announces it.
     *
     * @param action what caused it
     * @param fPlayer the player
     */
    void removeAfk(@NonNull String action, @NonNull FPlayer fPlayer);

    /**
     * Marks a player away and announces it.
     *
     * @param fPlayer the player
     */
    void addAfk(FPlayer fPlayer);

    /**
     * How long a player has been away.
     *
     * @param fPlayer the player
     * @return the duration in milliseconds, or 0 if they are not away
     */
    int getAfkDuration(FPlayer fPlayer);

    @NonNull String getAfkDurationFormatted(FPlayer fPlayer, FPlayer fReceiver);

    /**
     * The marker appended to an away player's name.
     *
     * @param fPlayer the away player
     * @param fResolver the reader
     * @param isAfk whether the player is away
     * @return the marker, or an empty string
     */
    String localizationFormat(FEntity fPlayer, FPlayer fResolver, boolean isAfk);

}
