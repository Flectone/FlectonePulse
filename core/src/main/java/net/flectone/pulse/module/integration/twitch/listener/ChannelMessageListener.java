package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.processing.resolver.FileResolver;

import java.util.List;

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
        List<String> channel = integration.getMessageChannel().get(MessageType.FROM_TWITCH_TO_MINECRAFT);
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
                .range(Range.get(Range.Type.PROXY))
                .destination(integration.getDestination())
                .filter(fPlayer -> fPlayer.isSetting(FPlayer.Setting.TWITCH))
                .tag(MessageType.FROM_TWITCH_TO_MINECRAFT)
                .format(s -> s.getForMinecraft()
                        .replace("<name>", nickname)
                        .replace("<channel>", channel))
                .message(message)
                .proxy(output -> {
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
