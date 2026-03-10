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
import net.flectone.pulse.model.event.message.context.MessageContext;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.ModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.afk.listener.AfkPulseListener;
import net.flectone.pulse.module.message.afk.model.AFKMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.controller.ModuleController;
import net.flectone.pulse.platform.formatter.TimeFormatter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.ModuleName;
import net.flectone.pulse.util.constant.SettingText;
import net.flectone.pulse.util.file.FileFacade;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.incendo.cloud.type.tuple.Pair;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AfkModule implements ModuleLocalization<Localization.Message.Afk> {

    private final Map<UUID, Pair<Integer, PlatformPlayerAdapter.Coordinates>> playersCoordinates = new ConcurrentHashMap<>();

    private final FileFacade fileFacade;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final IntegrationModule integrationModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ListenerRegistry listenerRegistry;
    private final MessagePipeline messagePipeline;
    private final MessageDispatcher messageDispatcher;
    private final ModuleController moduleController;
    private final TimeFormatter timeFormatter;

    @Override
    public void onEnable() {
        if (config().ticker().enable()) {
            taskScheduler.runPlayerRegionTimer(this::updateCoordinates, config().ticker().period());
        }

        listenerRegistry.register(AfkPulseListener.class);
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
    public Localization.Message.Afk localization(FEntity sender) {
        return fileFacade.localization(sender).message().afk();
    }

    public MessageContext addTag(MessageContext messageContext) {
        FEntity sender = messageContext.sender();
        if (moduleController.isDisabledFor(this, sender)) return messageContext;
        if (!(sender instanceof FPlayer)) return messageContext;

        return messageContext.addTagResolver(TagResolver.resolver(Set.of(MessagePipeline.ReplacementTag.AFK.getTagName(), "afk_suffix"), (argumentQueue, context) -> {
            FPlayer fPlayer = fPlayerService.getFPlayer(sender.uuid());

            String afkSuffix = fPlayer.getSetting(SettingText.AFK_SUFFIX);
            if (StringUtils.isEmpty(afkSuffix)) return MessagePipeline.ReplacementTag.emptyTag();

            afkSuffix = Strings.CS.replace(afkSuffix, "<time>", getAfkDurationFormatted(fPlayer, messageContext.receiver()));
            if (!afkSuffix.contains("%")) return Tag.preProcessParsed(afkSuffix);

            MessageContext afkContext = messagePipeline.createContext(fPlayer, messageContext.receiver(), afkSuffix)
                    .withFlags(messageContext.flags())
                    .addFlag(MessageFlag.PLAYER_MESSAGE, false);

            return Tag.inserting(messagePipeline.build(afkContext));
        }));
    }

    public void remove(@NonNull String action, @NonNull FPlayer fPlayer) {
        taskScheduler.runRegion(fPlayer, () -> {
            // sync fPlayer
            FPlayer syncFPlayer = fPlayerService.getFPlayer(fPlayer);

            // skip empty afk suffix
            if (StringUtils.isEmpty(syncFPlayer.getSetting(SettingText.AFK_SUFFIX))) {
                playersCoordinates.remove(syncFPlayer.uuid());
                return;
            }

            // always delete afk suffix if action is empty
            if (action.isEmpty()) {
                removeAfkSuffix(syncFPlayer);
                return;
            }

            // base module checks
            if (moduleController.isDisabledFor(this, syncFPlayer)) return;

            // skip ignored action
            if (config().ignore().contains(action)) return;

            // just remove afk suffix and send message
            removeAfkSuffix(syncFPlayer);
            sendAfkMessage(syncFPlayer.uuid(), false);
        });
    }

    public void removeAfkSuffix(FPlayer fPlayer) {
        fPlayerService.saveOrUpdateSetting(fPlayer.withoutSetting(SettingText.AFK_SUFFIX), SettingText.AFK_SUFFIX);
        playersCoordinates.remove(fPlayer.uuid());
    }

    public void setAfkSuffix(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        int time = (int) (System.currentTimeMillis() / 1000);
        playersCoordinates.put(fPlayer.uuid(), Pair.of(time, platformPlayerAdapter.getCoordinates(fPlayer)));

        fPlayerService.saveOrUpdateSetting(fPlayer.withSetting(SettingText.AFK_SUFFIX, localization().suffix()), SettingText.AFK_SUFFIX);
    }

    public int getAfkDuration(FPlayer fPlayer) {
        if (moduleController.isDisabledFor(this, fPlayer)) return 0;
        if (StringUtils.isEmpty(fPlayer.getSetting(SettingText.AFK_SUFFIX))) return 0;

        Pair<Integer, PlatformPlayerAdapter.Coordinates> timeCoordinates = playersCoordinates.get(fPlayer.uuid());
        return timeCoordinates != null ? (int) (System.currentTimeMillis() / 1000) - timeCoordinates.first() : 0;
    }

    @NonNull
    public String getAfkDurationFormatted(FPlayer fPlayer, FPlayer fReceiver) {
        int afkDuration = getAfkDuration(fPlayer);
        if (afkDuration == 0) return "";

        return timeFormatter.format(fReceiver, afkDuration * 1000L);
    }

    private void updateCoordinates(@NonNull FPlayer fPlayer) {
        // remove offline afk suffix
        if (!fPlayer.isOnline()) {
            removeAfkSuffix(fPlayer);
            return;
        }

        // base module checks
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        // get current player coordinates
        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(fPlayer);
        if (coordinates == null) return;

        // get current time
        int time = (int) (System.currentTimeMillis() / 1000);

        // synchronize FPlayer
        fPlayer = fPlayerService.getFPlayer(fPlayer);

        // compare last and current coordinates
        Pair<Integer, PlatformPlayerAdapter.Coordinates> timeCoordinates = playersCoordinates.get(fPlayer.uuid());
        if (timeCoordinates == null || !timeCoordinates.second().equals(coordinates)) {
            // remove afk suffix if present
            if (fPlayer.getSetting(SettingText.AFK_SUFFIX) != null) {
                removeAfkSuffix(fPlayer);
                sendAfkMessage(fPlayer.uuid(), false);
                return;
            }

            // update last coordinates
            playersCoordinates.put(fPlayer.uuid(), Pair.of(time, coordinates));
            return;
        }

        // skip afk players
        if (fPlayer.getSetting(SettingText.AFK_SUFFIX) != null) return;

        // skip not full afk players
        if (time - timeCoordinates.first() < config().delay()) return;

        // update afk suffix
        setAfkSuffix(fPlayer);
        sendAfkMessage(fPlayer.uuid(), true);
    }

    public void sendAfkMessage(UUID fPlayerUUID, boolean isAfk) {
        FPlayer fPlayer = fPlayerService.getFPlayer(fPlayerUUID);
        if (moduleController.isDisabledFor(this, fPlayer)) return;

        if (isAfk && fPlayer.getSetting(SettingText.AFK_SUFFIX) == null
                || !isAfk && fPlayer.getSetting(SettingText.AFK_SUFFIX) != null) return;

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
                        .filter(fReceiver -> integrationModule.canSeeVanished(fPlayer, fReceiver))
                        .proxy(dataOutputStream -> dataOutputStream.writeBoolean(isAfk))
                        .integration()
                        .build()
                )
                .newStatus(isAfk)
                .ignoreVanish(false)
                .build()
        );
    }
}
