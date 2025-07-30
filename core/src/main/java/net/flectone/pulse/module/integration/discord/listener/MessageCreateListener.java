package net.flectone.pulse.module.integration.discord.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.model.Range;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.constant.MessageType;
import reactor.core.publisher.Mono;

@Getter
@Singleton
public class MessageCreateListener extends EventListener<MessageCreateEvent> {

    private final Integration.Discord integration;

    @Inject
    public MessageCreateListener(FileResolver fileResolver) {
        integration = fileResolver.getIntegration().getDiscord();
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<MessageCreateEvent> execute(MessageCreateEvent event) {
        Message discordMessage = event.getMessage();

        String channel = integration.getMessageChannel().get(MessageType.FROM_DISCORD_TO_MINECRAFT);
        if (channel == null) return Mono.empty();
        if (!channel.equals(discordMessage.getChannelId().asString())) return Mono.empty();

        String nickname = event.getMember()
                .filter(member -> !member.isBot())
                .flatMap(User::getGlobalName)
                .orElse(null);
        if (nickname == null) return Mono.empty();

        String message = discordMessage.getContent();
        if (message.isEmpty()) {
            if (discordMessage.getAttachments().isEmpty()) return Mono.empty();

            message = String.join(" ", discordMessage.getAttachments()
                    .stream()
                    .map(Attachment::getUrl)
                    .toList()
            );
        }

        sendMessage(nickname, discordMessage.getContent(), message);

        return Mono.empty();
    }

    @Async
    public void sendMessage(String nickname, String rawMessage, String message) {
        builder(FPlayer.UNKNOWN)
                .range(Range.get(Range.Type.PROXY))
                .destination(integration.getDestination())
                .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.DISCORD))
                .tag(MessageType.FROM_DISCORD_TO_MINECRAFT)
                .format(s -> s.getForMinecraft().replace("<name>", nickname))
                .message(message)
                .proxy(output -> {
                    output.writeUTF(nickname);
                    output.writeUTF(rawMessage);
                })
                .integration()
                .sound(getSound())
                .sendBuilt();
    }

    @Override
    public void onEnable() {}

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }
}
