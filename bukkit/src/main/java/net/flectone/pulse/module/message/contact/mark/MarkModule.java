package net.flectone.pulse.module.message.contact.mark;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.manager.BukkitListenerManager;
import net.flectone.pulse.manager.FPlayerManager;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.contact.mark.listener.MarkListener;
import net.flectone.pulse.module.message.contact.mark.manager.MarkManager;
import net.flectone.pulse.module.message.contact.mark.model.FMark;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

@Singleton
public class MarkModule extends AbstractModuleMessage<Localization.Message.Contact> {

    @Getter
    private final Message.Contact.Mark message;
    private final Permission.Message.Contact.Mark permission;

    private final MarkManager markManager;
    private final BukkitListenerManager bukkitListenerManager;
    private final FPlayerManager fPlayerManager;

    @Inject
    public MarkModule(FileManager fileManager,
                      MarkManager markManager,
                      BukkitListenerManager bukkitListenerManager,
                      FPlayerManager fPlayerManager) {
        super(localization -> localization.getMessage().getContact());

        this.markManager = markManager;
        this.bukkitListenerManager = bukkitListenerManager;
        this.fPlayerManager = fPlayerManager;

        message = fileManager.getMessage().getContact().getMark();
        permission = fileManager.getPermission().getMessage().getContact().getMark();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        createCooldown(message.getCooldown(), permission.getCooldownBypass());

        bukkitListenerManager.register(MarkListener.class, EventPriority.NORMAL);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void mark(@NotNull FPlayer fPlayer, @NotNull String color) {
        if (checkModulePredicates(fPlayer)) return;
        if (message.isLimit() && markManager.contains(fPlayer)) return;

        NamedTextColor namedTextColor = NamedTextColor.WHITE;

        if (message.isColor()) {
            namedTextColor = NamedTextColor.NAMES.valueOr(color, namedTextColor);
        }

        FMark fMark = new FMark(message.getRange(), message.getDuration(), message.getEntity());

        markManager.create(fPlayer, fMark, namedTextColor);
        fPlayerManager.playSound(getSound(), fPlayer, Bukkit.getPlayer(fPlayer.getUuid()).getLocation());
    }
}
