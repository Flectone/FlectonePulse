package net.flectone.pulse.module.integration.twitch.sender;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.twitch.TwitchModule;
import net.flectone.pulse.module.integration.twitch.model.TwitchClient;
import net.flectone.pulse.module.integration.twitch.model.TwitchMetadata;
import net.flectone.pulse.module.integration.twitch.provider.TwitchClientProvider;
import net.flectone.pulse.util.constant.MessageFlag;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.UnaryOperator;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwitchSender {

    private final TwitchModule twitchModule;
    private final MessageDispatcher messageDispatcher;
    private final MessagePipeline messagePipeline;
    private final TwitchClientProvider twitchClientProvider;

    public void sendMessage(@NonNull String nickname,
                            @NonNull String channel,
                            @NonNull String message,
                            @Nullable Pair<String, String> reply) {
        TwitchClient twitchClient = twitchClientProvider.get();
        if (twitchClient == null) return;

        messageDispatcher.dispatch(twitchModule, TwitchMetadata.<Localization.Integration.Twitch>builder()
                .base(EventMetadata.<Localization.Integration.Twitch>builder()
                        .sender(twitchClient.sender())
                        .format(localization -> StringUtils.replaceEach(
                                StringUtils.defaultString(localization.messageChannel().get(twitchModule.name().name())),
                                new String[]{"<name>", "<channel>"},
                                new String[]{nickname, channel}
                        ))
                        .message(message)
                        .range(Range.get(Range.Type.PROXY))
                        .destination(twitchModule.config().destination())
                        .sound(twitchModule.soundOrThrow())
                        .tagResolvers(fResolver -> new TagResolver[]{messagePipeline.resolver("reply", (_, _) -> {
                            if (reply == null) return MessagePipeline.ReplacementTag.emptyTag();

                            return Tag.inserting(messagePipeline.build(MessageContext.builder()
                                    .message(twitchModule.localization(fResolver).formatReply())
                                    .tagResolvers(
                                            messagePipeline.resolver("reply_user", Tag.preProcessParsed(StringUtils.defaultString(reply.getLeft()))),
                                            messagePipeline.resolver("reply_message", (_, _) -> Tag.selfClosingInserting(messagePipeline.build(MessageContext.builder()
                                                    .sender(twitchClient.sender())
                                                    .receiver(fResolver)
                                                    .message(reply.getRight())
                                                    .flag(MessageFlag.PLAYER_MESSAGE, true)
                                                    .build()
                                            )))
                                    )
                                    .build()
                            ));
                        })})
                        .integration(IntegrationMetadata.builder()
                                .format(string -> StringUtils.replaceEach(
                                        string,
                                        new String[]{"<name>", "<channel>"},
                                        new String[]{nickname, channel}
                                ))
                                .messageNames(List.of(twitchModule.name().name() + "_" + channel.toUpperCase()))
                                .build()
                        )
                        .build()
                )
                .nickname(nickname)
                .channel(channel)
                .build()
        );
    }

    public void sendMessage(@NonNull String channel,
                            @NonNull String message) {
        TwitchClient twitchClient = twitchClientProvider.get();
        if (twitchClient == null) return;

        twitchClient.client().getChat().sendMessage(channel, message);
    }

    public void sendMessage(@NonNull FEntity sender,
                            @NonNull String messageName,
                            @NonNull UnaryOperator<String> twitchString) {
        List<String> channels = twitchModule.config().messageChannel().get(messageName);
        if (channels == null) return;
        if (channels.isEmpty()) return;

        String message = twitchModule.localization().messageChannel().getOrDefault(messageName, "<final_message>");
        if (StringUtils.isEmpty(message)) return;

        message = twitchString.apply(message);
        if (StringUtils.isEmpty(message)) return;

        for (String channel : channels) {
            sendMessage(channel, message);
        }
    }

}
