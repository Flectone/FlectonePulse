package net.flectone.pulse.module.integration.discord.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.discord.model.DiscordMetadata;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MessageCreateListener extends EventListener<MessageCreateEvent> {

    private final FileResolver fileResolver;

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<MessageCreateEvent> execute(MessageCreateEvent event) {
        Message discordMessage = event.getMessage();

        List<String> channel = config().getMessageChannel().get(MessageType.FROM_DISCORD_TO_MINECRAFT.name());
        if (channel == null) return Mono.empty();
        if (!channel.contains(discordMessage.getChannelId().asString())) return Mono.empty();

        Optional<Member> user = event.getMember();
        if (user.isEmpty() || user.get().isBot()) return Mono.empty();

        String message = discordMessage.getContent();
        if (message.isEmpty()) {
            if (discordMessage.getAttachments().isEmpty()) return Mono.empty();

            message = String.join(" ", discordMessage.getAttachments()
                    .stream()
                    .map(Attachment::getUrl)
                    .toList()
            );
        }

        sendMessage(user.get(), message);

        return Mono.empty();
    }

    @Async
    public void sendMessage(Member member, String message) {
        String globalName = member.getGlobalName().orElse("");
        String nickname = member.getNickname().orElse("");
        String displayName = member.getDisplayName();
        String userName = member.getUsername();

        sendMessage(DiscordMetadata.<Localization.Integration.Discord>builder()
                .sender(FPlayer.UNKNOWN)
                .format(localization -> {
                    Localization.Integration.Discord.ChannelEmbed channelEmbed = localization.getMessageChannel().get(MessageType.FROM_DISCORD_TO_MINECRAFT.name());
                    if (channelEmbed == null) return "";

                    return StringUtils.replaceEach(
                            channelEmbed.getContent(),
                            new String[]{"<name>", "<global_name>", "<nickname>", "<display_name>", "<user_name>"},
                            new String[]{globalName, globalName, nickname, displayName, userName}
                    );
                })
                .globalName(globalName)
                .nickname(nickname)
                .displayName(displayName)
                .userName(userName)
                .range(Range.get(Range.Type.PROXY))
                .destination(config().getDestination())
                .message(message)
                .sound(getModuleSound())
                .integration(string -> StringUtils.replaceEach(
                        string,
                        new String[]{"<name>", "<global_name>", "<nickname>", "<display_name>", "<user_name>"},
                        new String[]{globalName, globalName, nickname, displayName, userName}
                ))
                .build()
        );
    }

    @Override
    public void onEnable() {}

    @Override
    public Integration.Discord config() {
        return fileResolver.getIntegration().getDiscord();
    }

    @Override
    public Permission.Integration.Discord permission() {
        return fileResolver.getPermission().getIntegration().getDiscord();
    }

    @Override
    public Localization.Integration.Discord localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getIntegration().getDiscord();
    }
}
