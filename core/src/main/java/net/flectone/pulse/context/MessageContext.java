package net.flectone.pulse.context;

import lombok.Getter;
import lombok.Setter;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Setter
@Getter
public class MessageContext {

    private final Map<MessageFlag, Boolean> flags = new EnumMap<>(MessageFlag.class);
    private final Set<TagResolver> tagResolvers = new HashSet<>();

    private final FEntity sender;
    private final FEntity receiver;
    private final UUID processId;
    private String message;
    private String messageToTranslate;

    public MessageContext(FEntity sender, FEntity receiver, String message) {
        this.processId = UUID.randomUUID();
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    public void addTagResolvers(TagResolver... resolvers) {
        if (resolvers == null || resolvers.length == 0) return;

        tagResolvers.addAll(Arrays.asList(resolvers));
    }

    public void addReplacementTag(TagResolver tagResolver) {
        tagResolvers.add(tagResolver);
    }

    public void addReplacementTag(MessagePipeline.ReplacementTag replacementTag, BiFunction<ArgumentQueue, Context, Tag> handler) {
        addReplacementTag(TagResolver.resolver(replacementTag.getTagName(), handler));
    }

    public void addReplacementTag(Set<MessagePipeline.ReplacementTag> replacementTags, BiFunction<ArgumentQueue, Context, Tag> handler) {
        Set<String> tags = replacementTags.stream()
                .map(MessagePipeline.ReplacementTag::getTagName)
                .collect(Collectors.toSet());

        addReplacementTag(TagResolver.resolver(tags, handler));
    }

    public boolean isFlag(MessageFlag flag) {
        return flags.getOrDefault(flag, flag.getDefaultValue());
    }

    public void setFlag(MessageFlag flag, boolean value) {
        flags.put(flag, value);
    }
}
