package net.flectone.pulse.module.command.stream;

import net.flectone.pulse.config.Command;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.module.ModuleCommand;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.context.CommandContext;

/**
 * The /stream command, which announces that a player has gone live.
 * @author TheFaser
 */
public interface StreamModule extends ModuleCommand {

    @Override
    void onEnable();

    @Override
    void onDisable();

    @Override
    void execute(FPlayer fPlayer, CommandContext<FPlayer> commandContext);

    /**
     * Sets the marker shown next to a streaming player's name.
     *
     * @param fPlayer the player
     * @param prefix the marker, or an empty string to clear it
     */
    void setStreamPrefix(FPlayer fPlayer, String prefix);

    @Override
    ModuleName name();

    @Override
    Command.Stream config();

    @Override
    Permission.Command.Stream permission();

    @Override
    Localization.Command.Stream localization(FPlayer fPlayer);

    /**
     * Registers the tag that renders a player's stream marker.
     *
     * @param messageContext the message being formatted
     * @return the context with the tag added
     */
    MessageContext addTag(MessageContext messageContext);

    /**
     * Renders the stream links as clickable text.
     *
     * @param sender the streaming player
     * @param receiver the reader
     * @param urls the links
     * @return the resolver for the links tag
     */
    TagResolver urlTag(FEntity sender, FPlayer receiver, String urls);

}
