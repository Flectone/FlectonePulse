package net.flectone.pulse.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TagResolverUtil {

    public static TagResolver emptyTagResolver(@TagPattern String tag) {
        return TagResolver.resolver(tag, (argumentQueue, context) ->
                Tag.selfClosingInserting(Component.empty())
        );
    }

}
