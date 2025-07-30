package net.flectone.pulse.pipeline;

import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.util.logging.FLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class MessagePipeline {

    private final Set<TagResolver> disabledTags = new HashSet<>();

    private final FLogger fLogger;
    private final MiniMessage miniMessage;
    private final MessageProcessRegistry messageProcessRegistry;

    @Inject
    public MessagePipeline(FLogger fLogger,
                           MiniMessage miniMessage,
                           MessageProcessRegistry messageProcessRegistry) {
        this.fLogger = fLogger;
        this.miniMessage = miniMessage;
        this.messageProcessRegistry = messageProcessRegistry;
    }

    public void reload() {
        disabledTags.clear();

        Builder builder = builder("Test message for initializing disabled tags");
        builder.translate("Test message for initializing TranslateModule tags", true);
        builder.applyProcessors();

        Set<TagResolver> tagResolvers = builder.context.getTagResolvers();

        Arrays.stream(ReplacementTag.values())
                .filter(tag -> tagResolvers
                        .stream()
                        .filter(tagResolver -> !tagResolver.equals(StandardTags.translatable()))
                        .noneMatch(tagResolver -> tagResolver.has(tag.getTagName()))
                )
                .forEach(tag -> disabledTags.add(tag.empty()));
    }

    public Builder builder(@NotNull String message) {
        return builder(FPlayer.UNKNOWN, message);
    }

    public Builder builder(@NotNull FEntity sender, @NotNull String message) {
        return new Builder(sender, sender, message);
    }

    public Builder builder(@NotNull FEntity sender, @NotNull FEntity receiver, @NotNull String message) {
        return new Builder(sender, receiver, message);
    }

    public class Builder {

        private final MessageContext context;

        public Builder(FEntity sender, FEntity receiver, String message) {
            this.context = new MessageContext(sender, receiver, message);
        }

        public Builder caps(boolean caps) {
            context.setCaps(caps);
            return this;
        }

        public Builder flood(boolean flood) {
            context.setFlood(flood);
            return this;
        }

        public Builder colors(boolean colors) {
            context.setColors(colors);
            return this;
        }

        public Builder image(boolean image) {
            context.setImage(image);
            return this;
        }

        public Builder url(boolean url) {
            context.setUrl(url);
            return this;
        }

        public Builder formatting(boolean formating) {
            context.setFormatting(formating);
            return this;
        }

        public Builder swear(boolean swear) {
            context.setSwear(swear);
            return this;
        }

        public Builder emoji(boolean emoji) {
            context.setEmoji(emoji);
            return this;
        }

        public Builder fixation(boolean fixation) {
            context.setFixation(fixation);
            return this;
        }

        public Builder question(boolean question) {
            context.setQuestion(question);
            return this;
        }

        public Builder spoiler(boolean spoiler) {
            context.setSpoiler(spoiler);
            return this;
        }

        public Builder translate(boolean translate) {
            context.setTranslate(translate);
            return this;
        }

        public Builder translate(String messageToTranslate, boolean translate) {
            context.setMessageToTranslate(messageToTranslate);
            context.setTranslate(translate);
            return this;
        }

        public Builder translateItem(boolean translateItem) {
            context.setTranslateItem(translateItem);
            return this;
        }

        public Builder userMessage(boolean userMessage) {
            context.setUserMessage(userMessage);
            return this;
        }

        public Builder mention(boolean mention) {
            context.setMention(mention);
            return this;
        }

        public Builder player(boolean player) {
            context.setPlayer(player);
            return this;
        }

        public Builder interactiveChat(boolean interactiveChat) {
            context.setInteractiveChat(interactiveChat);
            return this;
        }

        public Builder tagResolvers(TagResolver... tagResolvers) {
            context.addTagResolvers(tagResolvers);
            return this;
        }

        public void applyProcessors() {
            messageProcessRegistry.getProcessors().forEach((priority, processors) ->
                    processors.forEach(processor -> processor.process(context))
            );
        }

        public Component build() {
            applyProcessors();

            // replace disabled tags
            disabledTags.forEach(tagResolver -> {
                context.addTagResolvers(tagResolver);
            });

            try {
                return miniMessage.deserialize(
                        context.getMessage().replace("§", "&"),
                        context.getTagResolvers().toArray(new TagResolver[0])
                );
            } catch (Exception e) {
                fLogger.warning(e);
            }

            return Component.empty();
        }

        public String defaultSerializerBuild() {
            return MiniMessage.miniMessage().serialize(build());
        }

        public String plainSerializerBuild() {
            return PlainTextComponentSerializer.plainText().serialize(build());
        }

        public String legacySerializerBuild() {
            return LegacyComponentSerializer.legacySection().serialize(build());
        }

        public JsonElement jsonSerializerBuild() {
            return GsonComponentSerializer.gson().serializeToTree(build());
        }
    }

    public enum ReplacementTag {

        AFK_SUFFIX,
        STREAM_PREFIX,
        VAULT_SUFFIX,
        VAULT_PREFIX,
        DISPLAY_NAME,
        PLAYER,
        CONSTANT,
        PING,
        TPS,
        ONLINE,
        COORDS,
        STATS,
        SKIN,
        ITEM,
        URL,
        FCOLOR,
        EMOJI,
        IMAGE,
        MENTION,
        SWEAR,
        QUESTION,
        SPOILER,
        TRANSLATE,
        TRANSLATETO,
        WORLD_PREFIX;

        @Subst("")
        public String getTagName() {
            return name().toLowerCase();
        }

        public TagResolver empty() {
            return ReplacementTag.empty(getTagName());
        }

        public static TagResolver empty(@TagPattern String tag) {
            return TagResolver.resolver(tag, (argumentQueue, context) ->
                    Tag.selfClosingInserting(Component.empty())
            );
        }

    }
}
