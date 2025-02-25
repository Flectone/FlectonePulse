package net.flectone.pulse.module.message.afk;

import com.google.inject.Inject;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.config.Localization;
import net.flectone.pulse.config.Message;
import net.flectone.pulse.config.Permission;
import net.flectone.pulse.database.dao.SettingDAO;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.scheduler.TaskScheduler;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AfkModule extends AbstractModuleMessage<Localization.Message.Afk> {

    private final Message.Afk message;
    private final Permission.Message.Afk permission;

    private final SettingDAO settingDAO;
    private final TaskScheduler taskScheduler;

    @Inject private IntegrationModule integrationModule;

    public AfkModule(FileManager fileManager,
                     SettingDAO settingDAO,
                     TaskScheduler taskScheduler) {
        super(localization -> localization.getMessage().getAfk());

        this.settingDAO = settingDAO;
        this.taskScheduler = taskScheduler;

        message = fileManager.getMessage().getAfk();
        permission = fileManager.getPermission().getMessage().getAfk();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        if (message.getTicker().isEnable()) {
            taskScheduler.runAsyncTicker(this::check, message.getTicker().getPeriod());
        }
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    public abstract void remove(@NotNull String action, FPlayer fPlayer);
    public abstract void check(@NotNull FPlayer fPlayer);

    @Async
    public void setAfk(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        fPlayer.setSetting(FPlayer.Setting.AFK_SUFFIX, resolveLocalization().getSuffix());
        send(fPlayer);

        settingDAO.insertOrUpdate(fPlayer, FPlayer.Setting.AFK_SUFFIX);
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

    public TagResolver afkTag(@NotNull FEntity sender) {
        if (checkModulePredicates(sender)) return TagResolver.empty();
        if (!(sender instanceof FPlayer fPlayer)) return TagResolver.empty();

        return TagResolver.resolver("afk_suffix", (argumentQueue, context) -> {
            if (!fPlayer.isSetting(FPlayer.Setting.AFK_SUFFIX)) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(Objects.requireNonNull(fPlayer.getSettingValue(FPlayer.Setting.AFK_SUFFIX)));
        });
    }
}
