package net.flectone.pulse.execution.pipeline;

import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.intellij.lang.annotations.Subst;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Turns a raw message into what the client finally sees. It resolves the MiniMessage tags,
 * runs the formatting modules and serializes the result to whatever form the destination needs.
 * @author TheFaser
 */
public interface MessagePipeline {

    /**
     * Renders the message back to MiniMessage text.
     *
     * @param messageContext the message
     * @return the rendered text
     */
    @NonNull String buildStandard(MessageContext messageContext);

    /**
     * Renders the message with all formatting stripped.
     *
     * @param messageContext the message
     * @return the plain text
     */
    @NonNull String buildPlain(MessageContext messageContext);

    /**
     * Renders the message using legacy section-sign color codes.
     *
     * @param messageContext the message
     * @return the legacy text
     */
    @NonNull String buildLegacy(MessageContext messageContext);

    /**
     * Renders a loose string for one reader using legacy color codes.
     *
     * @param fPlayer the reader
     * @param message the raw text
     * @return the legacy text, or empty if it could not be rendered
     */
    Optional<String> buildLegacy(@NonNull FPlayer fPlayer, @NonNull String message);

    /**
     * Renders the message as the JSON form the protocol uses.
     *
     * @param messageContext the message
     * @return the JSON text
     */
    @NonNull String buildJson(MessageContext messageContext);

    /**
     * Renders the message as a component, which is what the other build methods serialize.
     *
     * @param messageContext the message
     * @return the rendered component
     */
    @NonNull Component build(MessageContext messageContext);

    /**
     * A resolver inserting an already rendered component.
     *
     * @param message the component
     * @return the resolver
     */
    TagResolver messageTag(Component message);

    /**
     * A resolver rendering a nested message with its own sender and reader.
     *
     * @param sender who wrote it
     * @param receiver who reads it
     * @param message the raw text
     * @return the resolver
     */
    TagResolver messageTag(FEntity sender, FPlayer receiver, String message);

    /**
     * Renders a nested message with its own sender and reader.
     *
     * @param sender who wrote it
     * @param receiver who reads it
     * @param message the raw text
     * @return the rendered component
     */
    Component messageComponent(FEntity sender, FPlayer receiver, String message);

    /**
     * A resolver rendering another entity's name under a chosen tag and format.
     *
     * @param tag the tag name
     * @param formatTarget the format to render the name with
     * @param receiver who reads it
     * @param target the entity to name, may be null
     * @return the resolver
     */
    TagResolver targetTag(@TagPattern String tag, String formatTarget, FPlayer receiver, @Nullable FEntity target);

    /**
     * A resolver rendering another entity's name under a chosen tag.
     *
     * @param tag the tag name
     * @param receiver who reads it
     * @param target the entity to name, may be null
     * @return the resolver
     */
    TagResolver targetTag(@TagPattern String tag, FPlayer receiver, @Nullable FEntity target);

    /**
     * A resolver rendering another entity's name under the default target tag.
     *
     * @param receiver who reads it
     * @param target the entity to name, may be null
     * @return the resolver
     */
    TagResolver targetTag(FPlayer receiver, @Nullable FEntity target);

    /**
     * A resolver whose value is computed when the tag is met.
     *
     * @param name the tag name
     * @param handler produces the tag from its arguments
     * @return the resolver
     */
    @NonNull TagResolver resolver(@TagPattern @NonNull String name, @NonNull BiFunction<ArgumentQueue, Context, Tag> handler);

    /**
     * A resolver with a fixed tag.
     *
     * @param name the tag name
     * @param tag the tag
     * @return the resolver
     */
    @NonNull TagResolver resolver(@TagPattern @NonNull String name, @NonNull Tag tag);

    /**
     * A resolver that inserts a component.
     *
     * @param name the tag name
     * @param component the component to insert
     * @return the resolver
     */
    @NonNull TagResolver resolver(@TagPattern @NonNull String name, @NonNull Component component);

    /**
     * A resolver serving several tag names with one fixed tag.
     *
     * @param names the tag names
     * @param tag the tag
     * @return the resolver
     */
    @NonNull TagResolver resolver(@NonNull Set<String> names, @NonNull Tag tag);

    /**
     * A resolver serving several tag names with one component.
     *
     * @param names the tag names
     * @param component the component to insert
     * @return the resolver
     */
    @NonNull TagResolver resolver(@NonNull Set<String> names, @NonNull Component component);

    // wait for https://github.com/PaperMC/adventure/issues/1424
    /**
     * A resolver serving several tag names with a computed value.
     *
     * @param names the tag names
     * @param handler produces the tag from its arguments
     * @return the resolver
     */
    @NonNull TagResolver resolver(@NonNull Set<String> names, @NonNull BiFunction<ArgumentQueue, Context, Tag> handler);

    /**
     * Every tag the formatting modules can insert. The lowercase name is the tag written in a message.
     */
    enum ReplacementTag {
        AFK,
        ANIMATION,
        CONDITION,
        MUTE,
        STREAM,
        SERVER,
        SUFFIX,
        PREFIX,
        DELETE,
        DISPLAY_NAME,
        PLAYER,
        NICKNAME,
        CONSTANT,
        REPLACEMENT,
        MENTION,
        TOPONLINE,
        ONLINE,
        PADDING,
        SWEAR,
        QUESTION,
        TRANSLATION,
        WORLD,
        PLAYER_HEAD,
        PLAYER_HEAD_OR,
        SPRITE,
        SPRITE_OR,
        TEXTURE,
        TEXTURE_OR,
        FADING,
        FCOLOR;

        /**
         * A resolver that erases a tag, used when the module behind it is switched off.
         *
         * @param tag the tag name
         * @return the resolver
         */
        public static TagResolver emptyResolver(@TagPattern String tag) {
            return TagResolver.resolver(tag, (_, _) ->
                    Tag.selfClosingInserting(Component.empty())
            );
        }

        /**
         * A tag that renders to nothing.
         *
         * @return the empty tag
         */
        public static Tag emptyTag() {
            return Tag.selfClosingInserting(Component.empty());
        }

        /**
         * The tag as written in a message.
         *
         * @return the lowercase tag name
         */
        @Subst("")
        public String getTagName() {
            return name().toLowerCase();
        }

        /**
         * A resolver that erases this tag.
         *
         * @return the resolver
         */
        public TagResolver emptyResolver() {
            return emptyResolver(getTagName());
        }

    }
}