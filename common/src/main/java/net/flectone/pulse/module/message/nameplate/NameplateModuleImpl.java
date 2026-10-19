package net.flectone.pulse.module.message.nameplate;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.ModuleName;
import net.flectone.pulse.constant.SettingText;
import net.flectone.pulse.file.FileFacade;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.value.Destination;
import net.flectone.pulse.model.value.HeadText;
import net.flectone.pulse.model.value.Ticker;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.render.HeadTextRender;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.NearbyService;
import net.flectone.pulse.service.SocialService;

import java.util.List;
import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NameplateModuleImpl implements NameplateModule {

    private final FileFacade fileFacade;
    private final SocialService socialService;
    private final TaskScheduler taskScheduler;
    private final HeadTextRender headTextRender;
    private final MessagePipeline messagePipeline;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final NearbyService nearbyService;
    private final FPlayerService fPlayerService;

    @Override
    public void onEnable() {
        Ticker ticker = config().ticker();
        if (ticker.enable()) {
            taskScheduler.runPlayerAsyncTimer(this::show, ticker.period());
        }
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_NAMEPLATE;
    }

    @Override
    public Message.Nameplate config() {
        return fileFacade.message().nameplate();
    }

    @Override
    public Permission.Message.Nameplate permission() {
        return fileFacade.permission().message().nameplate();
    }

    @Override
    public Localization.Message.Nameplate localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().nameplate();
    }

    @Override
    public void show(FPlayer fPlayer) {
        if (config().destination().type() != Destination.Type.HEAD_TEXT) return;

        HeadText headText = config().destination().headText();
        HeadText.HeadTextBuilder headTextBuilder = headText.toBuilder();

        if (!headText.isPermanent()) {
            headTextBuilder = headTextBuilder.lifeTime(HeadText.PERMANENT);
        }

        PlatformPlayerAdapter.Coordinates holderCoordinates = platformPlayerAdapter.getCoordinates(fPlayer);
        if (holderCoordinates == null) return;

        String holderWorld = platformPlayerAdapter.getWorldName(fPlayer);

        for (UUID player : nearbyService.getNearbyPlayers(fPlayer.uuid(), headText.distance())) {
            FPlayer viewer = fPlayerService.getFPlayer(player);
            if (viewer.isUnknown()) continue;

            List<String> lines = localization(viewer).values().lines();
            if (headText.maxCount() != lines.size()) {
                headTextBuilder.maxCount(lines.size());
            }

            MessageFlag[] flags = {MessageFlag.MENTION_MODULE, MessageFlag.INTERACTIVE_CHAT_COMPAT, MessageFlag.QUESTIONANSWER_MODULE};
            boolean[] states = {false, false, false};

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(lines.size() - i - 1);
                headTextRender.render(this.name(), fPlayer, viewer,receiver -> messagePipeline.build(MessageContext.builder()
                        .module(this.name())
                        .sender(fPlayer)
                        .receiver(receiver)
                        .message(line)
                        .flags(flags, states)
                        .build()
                ), headTextBuilder.row(headText.row() + i).build(), holderCoordinates, holderWorld);
            }
        }
    }

}
