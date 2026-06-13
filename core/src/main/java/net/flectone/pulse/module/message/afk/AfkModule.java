package net.flectone.pulse.module.message.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.dispatcher.MessageDispatcher;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.event.EventMetadata;
import net.flectone.pulse.model.event.IntegrationMetadata;
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.message.afk.listener.AfkProxyMessageListener;
import net.flectone.pulse.module.message.afk.listener.PulseAfkListener;
import net.flectone.pulse.module.message.afk.model.AFKMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.platform.registry.ProxyRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.service.PlaytimeService;
import net.flectone.pulse.service.SocialService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AfkModule implements ModuleLocalization<Localization.Message.Afk> {

    private final Map<UUID, Pair<Long, PlatformPlayerAdapter.Coordinates>> playersCoordinates = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final TimeFormatter timeFormatter;
    private final PlaytimeService playtimeService;
    private final SocialService socialService;
    private final ProxyRegistry proxyRegistry;

    @Override
    public void onEnable() {
        if (config().ticker().enable()) {
            taskScheduler.runPlayerAsyncTimer(this::updateCoordinates, config().ticker().period());
        }

        if (proxyRegistry.hasEnabledProxy()) {
            listenerRegistry.register(AfkProxyMessageListener.class);
        }

        listenerRegistry.register(PulseAfkListener.class);
    }

    @Override
    public void onDisable() {
        playersCoordinates.clear();
    }

    @Override
    public ModuleName name() {
        return ModuleName.MESSAGE_AFK;
    }

    @Override
    public Message.Afk config() {
        return fileFacade.message().afk();
    }

    @Override
    public Permission.Message.Afk permission() {
        return fileFacade.permission().message().afk();
    }

    @Override
    public Localization.Message.Afk localization(FPlayer fPlayer) {
        return fileFacade.localization(socialService.getSetting(fPlayer, SettingText.LOCALE)).message().afk();
    }

    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;
        if (!(sender instanceof FPlayer)) return messageContext;

        return messageContext.addTagResolver(messagePipeline.resolver(Set.of(MessagePipeline.ReplacementTag.AFK.getTagName(), "afk_suffix"), (_, _) -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(sender.uuid());

            String afkSuffix = socialService.getSetting(fPlayer, SettingText.AFK_SUFFIX);
            if (StringUtils.isEmpty(afkSuffix)) return MessagePipeline.ReplacementTag.emptyTag();

            afkSuffix = Strings.CS.replace(afkSuffix, "<time>", getAfkDurationFormatted(fPlayer, messageContext.receiver()));
            if (!afkSuffix.contains("%")) return Tag.preProcessParsed(afkSuffix);

            return Tag.inserting(messagePipeline.build(MessageContext.builder()
                    .sender(fPlayer)
                    .receiver(messageContext.receiver())
                    .message(afkSuffix)
                    .flags(messageContext.flags())
                    .flag(MessageFlag.PLAYER_MESSAGE, false)
                    .build()
            ));
        }));
    }

    public void removeAllAfkPlayers(String action) {
        playersCoordinates.keySet().forEach(uuid -> removeAfk(action, fPlayerService.getFPlayer(uuid)));
    }

    public void removeAfk(@NonNull String action, @NonNull FPlayer fPlayer) {
        // skip empty afk suffix
        if (StringUtils.isEmpty(socialService.getSetting(fPlayer, SettingText.AFK_SUFFIX))) {
            playersCoordinates.remove(fPlayer.uuid());
            return;
        }

        // always delete afk suffix if action is empty
        if (action.isEmpty()) {
            removeAfkSetting(fPlayer);
            return;
        }

        // base module checks
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        // skip ignored action
        if (config().ignore().contains(action)) return;

        // just remove afk suffix
        removeAfkSetting(fPlayer);

        // send message
        sendAfkMessage(fPlayer, false);
    }

    public void addAfk(FPlayer fPlayer) {
        addAfkSetting(fPlayer);
        sendAfkMessage(fPlayer, true);
    }

    public int getAfkDuration(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return 0;
        if (StringUtils.isEmpty(socialService.getSetting(fPlayer, SettingText.AFK_SUFFIX))) return 0;

        Pair<Long, PlatformPlayerAdapter.Coordinates> timeCoordinates = playersCoordinates.get(fPlayer.uuid());
        return timeCoordinates != null ? (int) (System.currentTimeMillis() - timeCoordinates.getLeft()) / 1000 : 0;
    }

    @NonNull
    public String getAfkDurationFormatted(FPlayer fPlayer, FPlayer fReceiver) {
        int afkDuration = getAfkDuration(fPlayer);
        if (afkDuration == 0) return "";

        return timeFormatter.format(fReceiver, afkDuration * 1000L);
    }

    private void removeAfkSetting(FPlayer fPlayer) {
        socialService.saveSetting(fPlayer, SettingText.AFK_SUFFIX, null);

        playersCoordinates.remove(fPlayer.uuid());

        if (!config().trackPlaytime()) {
            playtimeService.saveAfkSession(fPlayer, false);
        }
    }

    private void addAfkSetting(FPlayer fPlayer) {
        socialService.saveSetting(fPlayer, SettingText.AFK_SUFFIX, localization().suffix());

        playersCoordinates.put(fPlayer.uuid(), Pair.of(System.currentTimeMillis(), platformPlayerAdapter.getCoordinates(fPlayer)));

        if (!config().trackPlaytime()) {
            playtimeService.saveAfkSession(fPlayer, true);
        }
    }

    private void updateCoordinates(@NonNull FPlayer fPlayer) {
        // remove offline afk suffix
        if (!fPlayer.isOnline()) {
            removeAfkSetting(fPlayer);
            return;
        }

        // base module checks
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        // get current player coordinates
        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(fPlayer);
        if (coordinates == null) return;

        // get current time
        long currentTime = System.currentTimeMillis();

        // synchronize FPlayer
        fPlayer = fPlayerService.getFPlayer(fPlayer);

        // compare last and current coordinates
        Pair<Long, PlatformPlayerAdapter.Coordinates> timeCoordinates = playersCoordinates.get(fPlayer.uuid());
        if (timeCoordinates == null || !isSameCoordinates(timeCoordinates.getRight(), coordinates)) {
            // remove afk suffix if present
            if (socialService.getSetting(fPlayer, SettingText.AFK_SUFFIX) != null) {
                removeAfkSetting(fPlayer);
                sendAfkMessage(fPlayer, false);
                return;
            }

            // update last coordinates
            playersCoordinates.put(fPlayer.uuid(), Pair.of(currentTime, coordinates));
            return;
        }

        if (!timeCoordinates.getRight().equals(coordinates)) {
            playersCoordinates.put(fPlayer.uuid(), Pair.of(timeCoordinates.getLeft(), platformPlayerAdapter.getCoordinates(fPlayer)));
        }

        // skip afk players
        if (socialService.getSetting(fPlayer, SettingText.AFK_SUFFIX) != null) return;

        // skip not full afk players
        if (currentTime - timeCoordinates.getLeft() < config().delay() * TimeFormatter.MULTIPLIER) return;

        // update afk suffix
        addAfkSetting(fPlayer);
        sendAfkMessage(fPlayer, true);
    }

    private boolean isSameCoordinates(PlatformPlayerAdapter.Coordinates first, PlatformPlayerAdapter.Coordinates second) {
        return first.equals(second) || first.distance(second) <= config().radius();
    }

    private void sendAfkMessage(FPlayer fPlayer, boolean isAfk) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        boolean afkSuffixEmpty = socialService.getSetting(fPlayer, SettingText.AFK_SUFFIX) == null;
        if (isAfk && afkSuffixEmpty) return;
        if (!isAfk && !afkSuffixEmpty) return;

        Range range = config().range();
        if (range.is(Range.Type.PLAYER)) {
            messageDispatcher.dispatch(this, AFKMetadata.<Localization.Message.Afk>builder()
                    .base(EventMetadata.<Localization.Message.Afk>builder()
                            .sender(fPlayer)
                            .format(localization -> isAfk
                                    ? localization.formatTrue().local()
                                    : localization.formatFalse().local()
                            )
                            .destination(config().destination())
                            .sound(soundOrThrow())
                            .build()
                    )
                    .newStatus(isAfk)
                    .build()
            );

            return;
        }

        boolean vanished = socialService.isVanished(fPlayer);
        messageDispatcher.dispatch(this, AFKMetadata.<Localization.Message.Afk>builder()
                .base(EventMetadata.<Localization.Message.Afk>builder()
                        .sender(fPlayer)
                        .format(localization -> isAfk
                                ? localization.formatTrue().global()
                                : localization.formatFalse().global()
                        )
                        .range(range)
                        .destination(config().destination())
                        .sound(soundOrThrow())
                        .filter(fReceiver -> socialService.canSeeVanished(fPlayer, fReceiver))
                        .proxy(dataOutputStream -> {
                            dataOutputStream.writeBoolean(isAfk);
                            dataOutputStream.writeBoolean(vanished);
                        })
                        .integration(IntegrationMetadata.builder()
                                .messageNames(List.of(name().name() + "_" + String.valueOf(isAfk).toUpperCase()))
                                .build()
                        )
                        .build()
                )
                .newStatus(isAfk)
                .fakeMessage(false)
                .vanished(vanished)
                .build()
        );
    }
}
