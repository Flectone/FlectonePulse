package net.flectone.pulse.module.message.contact.mark;

import com.github.retrooper.packetevents.util.Vector3i;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.file.Localization;
import net.flectone.pulse.file.Message;
import net.flectone.pulse.file.Permission;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.AbstractModuleMessage;
import net.flectone.pulse.module.message.contact.mark.listener.MarkListener;
import net.flectone.pulse.module.message.contact.mark.manager.MarkManager;
import net.flectone.pulse.module.message.contact.mark.model.FMark;
import net.flectone.pulse.platform.SoundPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

@Singleton
public class MarkModule extends AbstractModuleMessage<Localization.Message.Contact> {

    @Getter private final Message.Contact.Mark message;
    private final Permission.Message.Contact.Mark permission;

    private final MarkManager markManager;
    private final BukkitListenerRegistry bukkitListenerManager;
    private final SoundPlayer soundPlayer;

    @Inject
    public MarkModule(FileManager fileManager,
                      MarkManager markManager,
                      BukkitListenerRegistry bukkitListenerManager,
                      SoundPlayer soundPlayer) {
        super(localization -> localization.getMessage().getContact());

        this.markManager = markManager;
        this.bukkitListenerManager = bukkitListenerManager;
        this.soundPlayer = soundPlayer;

        message = fileManager.getMessage().getContact().getMark();
        permission = fileManager.getPermission().getMessage().getContact().getMark();

        addPredicate(this::checkCooldown);
    }

    @Override
    public void reload() {
        registerModulePermission(permission);

        createSound(message.getSound(), permission.getSound());
        createCooldown(message.getCooldown(), permission.getCooldownBypass());

        markManager.reload();

        bukkitListenerManager.register(MarkListener.class, EventPriority.NORMAL);
    }

    @Override
    public boolean isConfigEnable() {
        return message.isEnable();
    }

    @Async
    public void mark(@NotNull FPlayer fPlayer, @NotNull NamedTextColor textColor) {
        if (checkModulePredicates(fPlayer)) return;
        if (message.isLimit() && markManager.contains(fPlayer)) return;

        NamedTextColor namedTextColor = message.isColor() ? textColor : NamedTextColor.WHITE;

        FMark fMark = new FMark(message.getRange(), message.getDuration(), message.getLegacy(), message.getModern());

        markManager.create(fPlayer, fMark, namedTextColor);

        Player player = Bukkit.getPlayer(fPlayer.getUuid());
        if (player == null) return;

        Location location = player.getLocation();

        soundPlayer.play(getSound(), fPlayer, new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}
