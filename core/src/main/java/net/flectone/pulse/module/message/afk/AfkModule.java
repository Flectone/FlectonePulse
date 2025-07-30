package net.flectone.pulse.module.message.afk;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.constant.MessageFlag;
import net.flectone.pulse.constant.MessageType;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.model.Range;
import net.flectone.pulse.model.event.Event;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.pipeline.MessagePipeline;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.EventProcessRegistry;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.resolver.FileResolver;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Singleton
public class AfkModule extends AbstractModuleMessage<Localization.Message.Afk> implements MessageProcessor {

    private final Map<UUID, Pair<Integer, PlatformPlayerAdapter.Coordinates>> playersCoordinates = new HashMap<>();

    @Getter private final Message.Afk message;
    private final Permission.Message.Afk permission;
    private final Permission.Message.Format formatPermission;
    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final IntegrationModule integrationModule;
    private final PermissionChecker permissionChecker;
    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final MessageProcessRegistry messageProcessRegistry;
    private final EventProcessRegistry eventProcessRegistry;

    @Inject
    public AfkModule(FileResolver fileResolver,
                     FPlayerService fPlayerService,
                     TaskScheduler taskScheduler,
                     IntegrationModule integrationModule,
                     PermissionChecker permissionChecker,
                     PlatformPlayerAdapter platformPlayerAdapter,
                     MessageProcessRegistry messageProcessRegistry,
                     EventProcessRegistry eventProcessRegistry) {
        super(localization -> localization.getMessage().getAfk());

        this.message = fileResolver.getMessage().getAfk();
        this.permission = fileResolver.getPermission().getMessage().getAfk();
        this.formatPermission = fileResolver.getPermission().getMessage().getFormat();
        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.integrationModule = integrationModule;
        this.permissionChecker = permissionChecker;
        this.platformPlayerAdapter = platformPlayerAdapter;
        this.messageProcessRegistry = messageProcessRegistry;
        this.eventProcessRegistry = eventProcessRegistry;
    }

    @Override
    public void onEnable() {
        registerModulePermission(permission);

        if (message.getTicker().isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::check), message.getTicker().getPeriod());
        }

        messageProcessRegistry.register(150, this);
        eventProcessRegistry.registerPlayerHandler(Event.Type.PLAYER_LOAD, fPlayer -> remove("", fPlayer));
    }

    @Override
    public void onDisable() {
        playersCoordinates.clear();
    }

    @Override
    protected boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isFlag(MessageFlag.USER_MESSAGE) && !permissionChecker.check(sender, formatPermission.getAll())) return;

        if (checkModulePredicates(sender)) return;
        if (!(sender instanceof FPlayer fPlayer)) return;
        messageContext.addReplacementTag(MessagePipeline.ReplacementTag.AFK_SUFFIX, (argumentQueue, context) -> {
            String afkSuffix = fPlayer.getSettingValue(FPlayer.Setting.AFK_SUFFIX);
            if (afkSuffix == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(afkSuffix);
        });
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

        playersCoordinates.put(fPlayer.getUuid(), new Pair<>(0, new PlatformPlayerAdapter.Coordinates(0, -1000, 0)));
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
        if (timeVector == null || !timeVector.getValue().equals(coordinates)) {

            if (fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX)) {
                fPlayer.removeSetting(FPlayer.Setting.AFK_SUFFIX);
                playersCoordinates.remove(fPlayer.getUuid());
                fPlayerService.deleteSetting(fPlayer, FPlayer.Setting.AFK_SUFFIX);
                send(fPlayer);
            }

            playersCoordinates.put(fPlayer.getUuid(), new Pair<>(time, coordinates));
            return;
        }

        if (fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX)) return;
        if (time - timeVector.getKey() < message.getDelay()) return;

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
