package net.flectone.pulse.module.message.afk;

import lombok.Getter;
import net.flectone.pulse.adapter.PlatformPlayerAdapter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.checker.PermissionChecker;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.context.MessageContext;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.processor.MessageProcessor;
import net.flectone.pulse.registry.MessageProcessRegistry;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.service.FPlayerService;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Pair;
import net.flectone.pulse.util.Range;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.flectone.pulse.util.TagResolverUtil.emptyTagResolver;

public abstract class AfkModule extends AbstractModuleMessage<Localization.Message.Afk> implements MessageProcessor {

    private final Map<UUID, Pair<Integer, PlatformPlayerAdapter.Coordinates>> playersCoordinates = new HashMap<>();

    @Getter private final Message.Afk message;
    private final Permission.Message.Afk permission;
    private final Permission.Message.Format formatPermission;

    private final FPlayerService fPlayerService;
    private final TaskScheduler taskScheduler;
    private final IntegrationModule integrationModule;
    private final PermissionChecker permissionChecker;
    private final PlatformPlayerAdapter platformPlayerAdapter;

    public AfkModule(FileManager fileManager,
                     MessageProcessRegistry messageProcessRegistry,
                     FPlayerService fPlayerService,
                     TaskScheduler taskScheduler,
                     IntegrationModule integrationModule,
                     PermissionChecker permissionChecker,
                     PlatformPlayerAdapter platformPlayerAdapter) {
        super(localization -> localization.getMessage().getAfk());

        message = fileManager.getMessage().getAfk();
        permission = fileManager.getPermission().getMessage().getAfk();
        formatPermission = fileManager.getPermission().getMessage().getFormat();

        this.fPlayerService = fPlayerService;
        this.taskScheduler = taskScheduler;
        this.integrationModule = integrationModule;
        this.permissionChecker = permissionChecker;
        this.platformPlayerAdapter = platformPlayerAdapter;

        messageProcessRegistry.register(150, this);
    }

    @Override
    public void reload() {
        playersCoordinates.clear();

        registerModulePermission(permission);

        if (message.getTicker().isEnable()) {
            taskScheduler.runAsyncTimer(() -> fPlayerService.getFPlayers().forEach(this::check), message.getTicker().getPeriod());
        }
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Override
    public void process(MessageContext messageContext) {
        FEntity sender = messageContext.getSender();
        if (messageContext.isUserMessage() && !permissionChecker.check(sender, formatPermission.getAll())) return;

        messageContext.addTagResolvers(afkTag(messageContext.getSender()));
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

    @Async
    public void check(@NotNull FPlayer fPlayer) {
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

    @Async
    public void setAfk(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        fPlayerService.saveOrUpdateSetting(fPlayer, FPlayer.Setting.AFK_SUFFIX, resolveLocalization().getSuffix());
        send(fPlayer);
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        int range = message.getRange();
        boolean isAfk = !fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX);

        if (range == Range.PLAYER) {
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

        if (integrationModule.isVanished(fPlayer)) return;

        builder(fPlayer)
                .range(range)
                .destination(message.getDestination())
                .tag(MessageTag.AFK)
                .filter(fReceiver -> fReceiver.isSetting(FPlayer.Setting.AFK))
                .format(s -> isAfk
                        ? s.getFormatFalse().getGlobal()
                        : s.getFormatTrue().getGlobal()
                )
                .integration()
                .proxy(byteArrayDataOutput -> byteArrayDataOutput.writeBoolean(isAfk))
                .sound(getSound())
                .sendBuilt();
    }

    private TagResolver afkTag(@NotNull FEntity sender) {
        String tag = "afk_suffix";
        if (checkModulePredicates(sender)) return emptyTagResolver(tag);
        if (!(sender instanceof FPlayer fPlayer)) return emptyTagResolver(tag);

        return TagResolver.resolver(tag, (argumentQueue, context) -> {
            String afkSuffix = fPlayer.getSettingValue(FPlayer.Setting.AFK_SUFFIX);
            if (afkSuffix == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(afkSuffix);
        });
    }
}
