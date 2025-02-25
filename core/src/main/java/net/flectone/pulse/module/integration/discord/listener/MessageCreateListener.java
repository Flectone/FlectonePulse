package net.flectone.pulse.module.integration.discord.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import lombok.Getter;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;
import reactor.core.publisher.Mono;

@Singleton
public class MessageCreateListener extends EventListener<MessageCreateEvent> {

    @Getter private final Integration.Discord integration;

    private final TaskScheduler taskScheduler;

    @Inject
    public MessageCreateListener(FileManager fileManager,
                                 TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;

        integration = fileManager.getIntegration().getDiscord();
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<?> execute(MessageCreateEvent event) {
        Message discordMessage = event.getMessage();

        String channel = integration.getMessageChannel().get(MessageTag.FROM_DISCORD_TO_MINECRAFT);
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

        String finalMessage = message;
        taskScheduler.runAsync(() -> builder(FPlayer.UNKNOWN)
                .range(Range.PROXY)
                .destination(integration.getDestination())
                .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.DISCORD))
                .tag(MessageTag.FROM_DISCORD_TO_MINECRAFT)
                .format(s -> s.getForMinecraft().replace("<name>", nickname))
                .message(finalMessage)
                .proxy(output -> {
                    output.writeUTF(nickname);
                    output.writeUTF(discordMessage.getContent());
                })
                .integration()
                .sound(getSound())
                .sendBuilt()
        );


        return Mono.empty();
    }

    @Override
    public void reload() {}

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }
}
