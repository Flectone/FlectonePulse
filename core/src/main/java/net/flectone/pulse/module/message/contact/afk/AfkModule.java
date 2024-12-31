package net.flectone.pulse.module.message.contact.afk;

import com.google.inject.Inject;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.manager.ThreadManager;
import net.flectone.pulse.model.FEntity;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.integration.IntegrationModule;
import net.flectone.pulse.module.message.contact.afk.ticker.AfkTicker;
import net.flectone.pulse.util.MessageTag;
import net.flectone.pulse.util.Range;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public abstract class AfkModule extends AbstractModuleMessage<Localization.Message.Contact.Afk> {

    private final Message.Contact.Afk message;
    private final Permission.Message.Contact.Afk permission;

    private final ThreadManager threadManager;

    @Inject
    private IntegrationModule integrationModule;

    @Inject
    private AfkTicker afkTicker;

    public AfkModule(FileManager fileManager,
                     ThreadManager threadManager) {
        super(localization -> localization.getMessage().getContact().getAfk());

        this.threadManager = threadManager;

        message = fileManager.getMessage().getContact().getAfk();
        permission = fileManager.getPermission().getMessage().getContact().getAfk();
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        if (message.getTicker().isEnable()) {
            afkTicker.runTaskTimerAsync(message.getTicker().getPeriod(), message.getTicker().getPeriod());
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

        fPlayer.setAfkSuffix(resolveLocalization(fPlayer).getSuffix());
        send(fPlayer);

        threadManager.runDatabase(database -> database.updateFPlayer(fPlayer));
    }

    @Async
    public void send(FPlayer fPlayer) {
        if (checkModulePredicates(fPlayer)) return;

        int range = message.getRange();
        boolean isAfk = fPlayer.getAfkSuffix() == null;

        if (range == Range.PLAYER) {
            if (!fPlayer.is(FPlayer.Setting.AFK)) return;

            builder(fPlayer)
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
                .tag(MessageTag.AFK)
                .filter(fReceiver -> fReceiver.is(FPlayer.Setting.AFK))
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
            if (fPlayer.getAfkSuffix() == null) return Tag.selfClosingInserting(Component.empty());

            return Tag.preProcessParsed(fPlayer.getAfkSuffix());
        });
    }
}
