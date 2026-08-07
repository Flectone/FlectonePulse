package net.flectone.pulse.module.message.chat;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.chat.model.Chat;
import net.flectone.pulse.util.constant.ModuleName;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Handles ordinary chat, deciding which chat a message goes to, who hears it and how it is formatted.
 * @author TheFaser
 */
public interface ChatModule extends ModuleLocalization {

    @Override
    ModuleName name();

    @Override
    Message.Chat config();

    @Override
    Permission.Message.Chat permission();

    @Override
    Localization.Message.Chat localization(FPlayer fPlayer);

    /**
     * Handles a chat message from the platform, deciding whether it goes through and where.
     *
     * @param fPlayer the speaker
     * @param rawString the text as typed
     * @param cancelEvent called to suppress the platform's own handling
     * @param successEvent called with the formatted text and whether the plugin already delivered it
     */
    void handleChatEvent(FPlayer fPlayer, String rawString, Runnable cancelEvent, BiConsumer<String, Boolean> successEvent);

    /**
     * Delivers a chat message that has already passed the checks.
     *
     * @param fPlayer the speaker
     * @param rawString the text as typed
     * @param playerMessage the formatted text
     * @param playerChat the chat it goes to
     */
    void sendMessage(FPlayer fPlayer, String rawString, String playerMessage, Chat playerChat);

    /**
     * Which players may read a given chat.
     *
     * @param chatName the chat
     * @return the reader filter
     */
    Predicate<FPlayer> permissionFilter(String chatName);

}
