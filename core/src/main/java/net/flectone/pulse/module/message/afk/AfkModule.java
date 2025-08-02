package net.flectone.pulse.module.message.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.flectone.pulse.platform.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.util.constant.MessageType;
import net.flectone.pulse.model.entity.FPlayer;
import net.flectone.pulse.model.util.Range;
import net.flectone.pulse.module.AbstractModuleLocalization;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.afk.listener.AfkPulseListener;
import net.flectone.pulse.platform.registry.ListenerRegistry;
import net.flectone.pulse.processing.resolver.FileResolver;
import net.flectone.pulse.execution.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import org.incendo.cloud.type.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class AfkModule extends AbstractModuleLocalization<Localization.Message.Afk> {

    private final Map<UUID, Pair<Integer, PlatformPlayerAdapter.Coordinates>> playersCoordinates = new HashMap<>();

    private final Message.Afk message;
    private final Permission.Message.Afk permission;
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
        super(localization -> localization.getMessage().getAfk());

        this.message = fileResolver.getMessage().getAfk();
        this.permission = fileResolver.getPermission().getMessage().getAfk();
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.integrationModule = integrationModule;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.listenerRegistry = listenerRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        if (message.getTicker().isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::check), message.getTicker().getPeriod());
        }

        listenerRegistry.register(AfkPulseListener.class);
    }

    @Override
    public void onDisable() {
        playersCoordinates.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void remove(@NotNull String action, FPlayer fPlayer) {
        if (action.isEmpty()) {
            fPlayer.removeSetting(FPlayer.Setting.AFK_SUFFIX);
            playersCoordinates.remove(fPlayer.getUuid());
            fPlayerService.deleteSetting(fPlayer, FPlayer.Setting.AFK_SUFFIX);
            return;
        }

        if (checkModulePredicates(fPlayer)) return;
        if (message.getIgnore().contains(action)) return;

        playersCoordinates.put(fPlayer.getUuid(), Pair.of(0, new PlatformPlayerAdapter.Coordinates(0, -1000, 0)));
        check(fPlayer);
    }

    private void check(@NotNull FPlayer fPlayer) {
        if (!fPlayer.isOnline()) {
            String afkSuffix = fPlayer.getSettingValue(FPlayer.Setting.AFK_SUFFIX);

            fPlayer.removeSetting(FPlayer.Setting.AFK_SUFFIX);
            playersCoordinates.remove(fPlayer.getUuid());

            if (afkSuffix != null) {
                send(fPlayer);
            }

            return;
        }

        if (checkModulePredicates(fPlayer)) return;

        PlatformPlayerAdapter.Coordinates coordinates = platformPlayerAdapter.getCoordinates(fPlayer);
        if (coordinates == null) return;

        int time = (int) (System.currentTimeMillis()/1000);

        Pair<Integer, PlatformPlayerAdapter.Coordinates> timeVector = playersCoordinates.get(fPlayer.getUuid());
        if (timeVector == null || !timeVector.second().equals(coordinates)) {

            if (fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX)) {
                fPlayer.removeSetting(FPlayer.Setting.AFK_SUFFIX);
                playersCoordinates.remove(fPlayer.getUuid());
                fPlayerService.deleteSetting(fPlayer, FPlayer.Setting.AFK_SUFFIX);
                send(fPlayer);
            }

            playersCoordinates.put(fPlayer.getUuid(), Pair.of(time, coordinates));
            return;
        }

        if (fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX)) return;
        if (time - timeVector.first() < message.getDelay()) return;

        setAfk(fPlayer);
    }

    public void setAfk(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.AFK_SUFFIX, resolveLocalization().getSuffix());
        send(fPlayer);
    }

    private void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        Range range = message.getRange();
        boolean isAfk = !fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX);

        if (range.is(Range.Type.PLAYER)) {
            if (!fPlayer.isSetting(FPlayer.Setting.AFK)) return;

            builder(fPlayer)
                    .destination(message.getDestination())
                    .format(s -> isAfk
                            ? s.getFormatFalse().getLocal()
                            : s.getFormatTrue().getLocal()
                    )
                    .sound(getSound())
                    .sendBuilt();

            return;
        }

        builder(fPlayer)
                .range(range)
                .destination(message.getDestination())
                .tag(MessageType.AFK)
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.AFK))
                .filter(fReceiver -> integrationModule.isVanishedVisible(fPlayer, fReceiver))
                .format(s -> isAfk
                        ? s.getFormatFalse().getGlobal()
                        : s.getFormatTrue().getGlobal()
                )
                .integration()
                .proxy(dataOutputStream -> dataOutputStream.writeBoolean(isAfk))
                .sound(getSound())
                .sendBuilt();
    }
}
