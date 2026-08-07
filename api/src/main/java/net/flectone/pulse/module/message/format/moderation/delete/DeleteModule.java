package net.flectone.pulse.module.message.format.moderation.delete;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * Lets a moderator withdraw a message that was already delivered, and keeps the history needed to do so.
 * @author TheFaser
 */
public interface DeleteModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    ModuleName name();

    @Override
    Message.Format.Moderation.Delete config();

    @Override
    Permission.Message.Format.Moderation.Delete permission();

    @Override
    Localization.Message.Format.Moderation.Delete localization(FPlayer fPlayer);

    /**
     * Forgets every message held for a player, so none of them can be withdrawn any more.
     *
     * @param fPlayer the player
     */
    void clearHistory(FPlayer fPlayer);

    /**
     * Registers the tag that draws the withdraw button.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

    /**
     * Remembers a delivered message so it can be withdrawn later.
     *
     * @param receiver who received it
     * @param messageUUID the message id
     * @param component the rendered message
     * @param needToCache whether to keep the rendered form, which is only needed on some platforms
     */
    void save(FPlayer receiver, UUID messageUUID, Component component, boolean needToCache);

    /**
     * Whether a rendered message is one this module is holding.
     *
     * @param component the rendered message
     * @return true if it is held
     */
    boolean isCached(Component component);

    /**
     * Forgets a held message.
     *
     * @param component the rendered message
     */
    void removeCache(Component component);

    /**
     * Withdraws a message from everyone who received it.
     *
     * @param sender who wrote it
     * @param messageUUID the message id
     * @return true if anything was withdrawn
     */
    boolean remove(FEntity sender, UUID messageUUID);

    /**
     * Redraws a reader's chat after something was withdrawn.
     *
     * @param receiver the reader
     */
    void sendUpdate(UUID receiver);

}
