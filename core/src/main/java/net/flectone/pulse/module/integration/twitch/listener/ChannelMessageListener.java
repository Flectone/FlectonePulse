package net.flectone.pulse.module.integration.twitch.listener;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Integration;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.integration.twitch.model.TwitchMetadata;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.util.constant.MessageType;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Singleton
public class ChannelMessageListener extends EventListener<ChannelMessageEvent> {

    private final FileResolver fileResolver;

    @Inject
    public ChannelMessageListener(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    public Class<ChannelMessageEvent> getEventType() {
        return ChannelMessageEvent.class;
    }

    public void execute(ChannelMessageEvent event) {
        List<String> channel = config().getMessageChannel().get(MessageType.FROM_TWITCH_TO_MINECRAFT.name());
        if (channel == null || channel.isEmpty()) return;

        String channelName = event.getChannel().getName();
        if (!channel.contains(channelName)) return;

        String nickname = event.getUser().getName();
        String message = event.getMessage();

        sendMessage(nickname, channelName, message);
    }

    @Async
    public void sendMessage(String nickname, String channel, String message) {
        sendMessage(TwitchMetadata.<Localization.Integration.Twitch>builder()
                .sender(FPlayer.UNKNOWN)
                .format(s -> StringUtils.replaceEach(
                        s.getForMinecraft(),
                        new String[]{"<name>", "<channel>"},
                        new String[]{String.valueOf(nickname), String.valueOf(channel)}
                ))
                .nickname(nickname)
                .channel(channel)
                .message(message)
                .range(Range.get(Range.Type.PROXY))
                .destination(config().getDestination())
                .sound(getModuleSound())
                .integration(string -> StringUtils.replaceEach(
                        string,
                        new String[]{"<name>", "<channel>"},
                        new String[]{nickname, channel}
                ))
                .build()
        );
    }

    @Override
    public void onEnable() {}

    @Override
    public Integration.Twitch config() {
        return fileResolver.getIntegration().getTwitch();
    }

    @Override
    public Permission.Integration.Twitch permission() {
        return fileResolver.getPermission().getIntegration().getTwitch();
    }

    @Override
    public Localization.Integration.Twitch localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getIntegration().getTwitch();
    }
}
