package net.flectone.pulse.module.message.vanilla;

import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.vanilla.model.ParsedComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Re-renders the server's own messages so they follow the plugin's formatting.
 * @author TheFaser
 */
public interface VanillaModule extends ModuleLocalization {

    /**
     * The tag prefix under which each vanilla message argument is exposed.
     */
    String ARGUMENT = "argument";

    @Override
    Message.Vanilla config();

    @Override
    Permission.Message.Vanilla permission();

    @Override
    Localization.Message.Vanilla localization(FPlayer fPlayer);

    /**
     * Registers the tag that renders the arguments of a vanilla message.
     *
     * @param fResolver the reader
     * @param parsedComponent the parsed vanilla message
     * @return the resolver
     */
    TagResolver argumentTag(FPlayer fResolver, ParsedComponent parsedComponent);

}
