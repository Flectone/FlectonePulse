package net.flectone.pulse.module.message.mark;

import com.github.retrooper.packetevents.util.Vector3i;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.Getter;
import net.flectone.pulse.annotation.Async;
import net.flectone.pulse.configuration.Localization;
import net.flectone.pulse.configuration.Message;
import net.flectone.pulse.configuration.Permission;
import net.flectone.pulse.manager.FileManager;
import net.flectone.pulse.model.FPlayer;
import net.flectone.pulse.module.message.mark.listener.MarkListener;
import net.flectone.pulse.module.message.mark.manager.MarkManager;
import net.flectone.pulse.module.message.mark.model.FMark;
import net.flectone.pulse.sender.SoundPlayer;
import net.flectone.pulse.registry.BukkitListenerRegistry;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

@Singleton
public class BukkitMarkModule extends MarkModule {

    @Getter private final Message.Mark message;
    private final Permission.Message.Mark permission;

    private final MarkManager markManager;
    private final BukkitListenerRegistry bukkitListenerManager;
    private final SoundPlayer soundPlayer;

    @Inject
    public BukkitMarkModule(FileManager fileManager,
                            MarkManager markManager,
                            BukkitListenerRegistry bukkitListenerManager,
                            SoundPlayer soundPlayer) {
        super(Localization::getMessage);

        this.markManager = markManager;
        this.bukkitListenerManager = bukkitListenerManager;
        this.soundPlayer = soundPlayer;

        message = fileManager.getMessage().getMark();
        permission = fileManager.getPermission().getMessage().getMark();

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
    protected boolean isConfigEnable() {
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
