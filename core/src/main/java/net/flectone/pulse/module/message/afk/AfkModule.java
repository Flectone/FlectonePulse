package net.flectone.pulse.module.message.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.localization.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.execution.pipeline.MessagePipeline;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.model.entity.FEntity;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.afk.listener.AfkPulseListener;
import net.flectone.pulse.module.message.afk.model.AFKMetadata;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.context.MessageContext;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.constant.MessageFlag;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.util.constant.SettingText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.apache.commons.lang3.StringUtils;
import org.incendo.cloud.type.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class AfkModule extends AbstractModuleLocalization<Localization.Message.Afk> {

    private final Map<UUID, Pair<Integer, PlatformPlayerAdapter.Coordinates>> playersCoordinates = new HashMap<>();

    private final FileResolver fileResolver;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final IntegrationModule integrationModule;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final ListenerRegistry listenerRegistry;

    @Inject
    public AfkModule(FileResolver fileResolver,
                     FPlayerService fPlayerService,
                     TaskScheduler taskScheduler,
                     IntegrationModule integrationModule,
                     PlatformPlayerAdapter platformPlayerAdapter,
                     ListenerRegistry listenerRegistry) {
        super(MessageType.AFK);

        this.fileResolver = fileResolver;
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.integrationModule = integrationModule;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (config().getTicker().isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getOnlineFPlayers().forEach(this::check), config().getTicker().getPeriod());
        }

        listenerRegistry.register(AfkPulseListener.class);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        playersCoordinates.clear();
    }

    @Override
    public Message.Afk config() {
        return fileResolver.getMessage().getAfk();
    }

    @Override
    public Permission.Message.Afk permission() {
        return fileResolver.getPermission().getMessage().getAfk();
    }

    @Override
    public Localization.Message.Afk localization(FEntity sender) {
        return fileResolver.getLocalization(sender).getMessage().getAfk();
    }

    public void addTag(MessageContext messageContext) {
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE)) return;

        FEntity sender = messageContext.getSender();
        if (isModuleDisabledFor(sender)) return;
        if (!(sender instanceof FPlayer fPlayer)) return;

        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.AFK_SUFFIX, (argumentQueue, context) -> {
            String afkSuffix = fPlayer.getSetting(SettingText.AFK_SUFFIX);
            if (StringUtils.isEmpty(afkSuffix)) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(afkSuffix);
        });
    }

    @Async
    public void remove(@NotNull String action, FPlayer fPlayer) {
        if (action.isEmpty()) {
            fPlayer.removeSetting(SettingText.AFK_SUFFIX);
            fPlayerService.saveOrUpdateSetting(fPlayer, SettingText.AFK_SUFFIX);

            playersCoordinates.remove(fPlayer.getUuid());
            return;
        }

        if (isModuleDisabledFor(fPlayer)) return;
        if (config().getIgnore().contains(action)) return;

        playersCoordinates.put(fPlayer.getUuid(), Pair.of(0, new PlatformPlayerAdapter.Coordinates(0, -1000, 0)));
        check(fPlayer);
    }

    private void check(@NotNull FPlayer fPlayer) {
        if (!fPlayer.isOnline()) {
            String afkSuffix = fPlayer.getSetting(SettingText.AFK_SUFFIX);

            fPlayer.removeSetting(SettingText.AFK_SUFFIX);
            playersCoordinates.remove(fPlayer.getUuid());

            if (afkSuffix != null) {
                send(fPlayer);
            }

            return;
        }

        if (isModuleDisabledFor(fPlayer)) return;

        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(fPlayer);
        if (coordinates == null) return;

        int time = (int) (System.currentTimeMillis()/1000);

        Pair<Integer, PlatformPlayerAdapter.Coordinates> timeVector = playersCoordinates.get(fPlayer.getUuid());
        if (timeVector == null || !timeVector.second().equals(coordinates)) {

            if (fPlayer.getSetting(SettingText.AFK_SUFFIX) != null) {
                fPlayer.removeSetting(SettingText.AFK_SUFFIX);
                fPlayerService.saveOrUpdateSetting(fPlayer, SettingText.AFK_SUFFIX);

                playersCoordinates.remove(fPlayer.getUuid());

                send(fPlayer);
            }

            playersCoordinates.put(fPlayer.getUuid(), Pair.of(time, coordinates));
            return;
        }

        if (fPlayer.getSetting(SettingText.AFK_SUFFIX) != null) return;
        if (time - timeVector.first() < config().getDelay()) return;

        setAfk(fPlayer);
    }

    public void setAfk(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        fPlayer.setSetting(SettingText.AFK_SUFFIX, localization().getSuffix());
        fPlayerService.saveOrUpdateSetting(fPlayer, SettingText.AFK_SUFFIX);

        send(fPlayer);
    }

    private void send(FPlayer fPlayer) {
        if (isModuleDisabledFor(fPlayer)) return;

        Range range = config().getRange();
        boolean isAfk = fPlayer.getSetting(SettingText.AFK_SUFFIX) == null;

        if (range.is(Range.Type.PLAYER)) {
            sendMessage(AFKMetadata.<Localization.Message.Afk>builder()
                    .sender(fPlayer)
                    .format(s -> isAfk
                            ? s.getFormatFalse().getLocal()
                            : s.getFormatTrue().getLocal()
                    )
                    .newStatus(isAfk)
                    .destination(config().getDestination())
                    .sound(getModuleSound())
                    .build()
            );

            return;
        }

        sendMessage(AFKMetadata.<Localization.Message.Afk>builder()
                .sender(fPlayer)
                .format(s -> isAfk
                        ? s.getFormatFalse().getGlobal()
                        : s.getFormatTrue().getGlobal()
                )
                .newStatus(isAfk)
                .range(range)
                .destination(config().getDestination())
                .sound(getModuleSound())
                .filter(fReceiver -> integrationModule.canSeeVanished(fPlayer, fReceiver))
                .proxy(dataOutputStream -> dataOutputStream.writeBoolean(isAfk))
                .integration()
                .build()
        );
    }
}
