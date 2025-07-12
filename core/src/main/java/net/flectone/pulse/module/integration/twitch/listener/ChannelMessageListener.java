package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Integration;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;

import java.util.List;

@Getter
@Singleton
public class ChannelMessageListener extends EventListener<ChannelMessageEvent> {

    private final Integration.Twitch integration;

    @Inject
    public ChannelMessageListener(FileResolver fileResolver) {
        this.integration = fileResolver.getIntegration().getTwitch();
    }

    public Class<ChannelMessageEvent> getEventType() {
        return ChannelMessageEvent.class;
    }

    public void execute(ChannelMessageEvent event) {
        List<String> channel = integration.getMessageChannel().get(MessageTag.FROM_TWITCH_TO_MINECRAFT);
        if (channel == null || channel.isEmpty()) return;

        String channelName = event.getChannel().getName();
        if (!channel.contains(channelName)) return;

        String nickname = event.getUser().getName();
        String message = event.getMessage();

        sendMessage(nickname, channelName, message);
    }

    @Async
    public void sendMessage(String nickname, String channel, String message) {
        builder(FPlayer.UNKNOWN)
                .range(Range.PROXY)
                .destination(integration.getDestination())
                .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.TWITCH))
                .tag(MessageTag.FROM_TWITCH_TO_MINECRAFT)
                .format(s -> s.getForMinecraft()
                        .replace("<name>", nickname)
                        .replace("<channel>", channel))
                .message(message)
                .proxy((output) -> {
                    output.writeUTF(nickname);
                    output.writeUTF(channel);
                    output.writeUTF(message);
                })
                .integration()
                .sendBuilt();
    }

    @Override
    public void onEnable() {}

    @Override
    protected boolean isConfigEnable() {
        return integration.isEnable();
    }
}
