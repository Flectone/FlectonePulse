package net.flectone.pulse.module.message.format.replacement;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.util.constant.ModuleName;
import net.kyori.adventure.text.Component;

import java.util.concurrent.ExecutionException;

/**
 * Rewrites configured fragments of a message, turning links and emotes into rich content.
 * @author TheFaser
 */
public interface ReplacementModule extends ModuleLocalization {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    ModuleName name();

    @Override
    Message.Format.Replacement config();

    @Override
    Permission.Message.Format.Replacement permission();

    @Override
    Localization.Message.Format.Replacement localization(FPlayer fPlayer);

    /**
     * Rewrites the configured fragments of the message.
     *
     * @param messageContext the message being formatted
     * @return the formatted context
     */
    MessageContext format(MessageContext messageContext);

    /**
     * Registers the tags that expand the configured replacements.
     *
     * @param messageContext the message being formatted
     * @return the context with the tags added
     */
    MessageContext addTags(MessageContext messageContext);

    /**
     * Renders a remote image as a component, downloading it on the first request.
     *
     * @param link the image url
     * @return the rendered image
     * @throws ExecutionException if the download or rendering fails
     */
    Component createImageComponent(String link) throws ExecutionException;

}
