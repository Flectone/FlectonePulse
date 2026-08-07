package net.flectone.pulse.module.message.format.translate;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.event.message.context.StringMessageContext;
import net.flectone.pulse.module.ModuleLocalization;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Lets a reader translate someone else's message by clicking it.
 * @author TheFaser
 */
public interface TranslateModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    ModuleName name();

    @Override
    Message.Format.Translate config();

    @Override
    Permission.Message.Format.Translate permission();

    @Override
    Localization.Message.Format.Translate localization(FPlayer fPlayer);

    /**
     * Stores a message so a reader can translate it later by clicking.
     *
     * @param message the text
     * @return the id the translate button carries
     */
    UUID saveMessage(String message);

    /**
     * Registers the tag that draws the translate button.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(StringMessageContext messageContext);

    /**
     * The stored text behind a translate button.
     *
     * @param stringUUID the message id, as written in the button
     * @return the text, or an empty string if it has expired
     */
    @Nullable
    String getMessage(String stringUUID);

    /**
     * The stored text behind a translate button.
     *
     * @param uuid the message id
     * @return the text, or an empty string if it has expired
     */
    @Nullable
    String getMessage(UUID uuid);

}
