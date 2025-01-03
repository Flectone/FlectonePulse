package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.file.Integration;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;

import java.util.List;

@Singleton
public class ChannelMessageListener extends EventListener<ChannelMessageEvent> {

    @Getter
    private final Integration.Twitch integration;

    private final ThreadManager threadManager;

    @Inject
    public ChannelMessageListener(FileManager fileManager,
                                  ThreadManager threadManager) {
        this.threadManager = threadManager;

        integration = fileManager.getIntegration().getTwitch();
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
        threadManager.runAsync(() -> builder(FPlayer.UNKNOWN)
                .range(Range.PROXY)
                .destination(integration.getDestination())
                .filter(fPlayer -> fPlayer.is(FPlayer.Setting.TWITCH))
                .tag(MessageTag.FROM_TWITCH_TO_MINECRAFT)
                .format(s -> s.getForMinecraft()
                        .replace("<name>", nickname)
                        .replace("<channel>", channelName))
                .message(message)
                .proxy((output) -> {
                    output.writeUTF(nickname);
                    output.writeUTF(channelName);
                    output.writeUTF(message);
                })
                .integration()
                .sendBuilt());
    }

    @Override
    public void reload() {}

    @Override
    public boolean isConfigEnable() {
        return integration.isEnable();
    }
}
